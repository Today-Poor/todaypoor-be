package com.todaypoor.ranking.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.todaypoor.expense.entity.Expense;
import com.todaypoor.expense.repository.ExpenseRepository;
import com.todaypoor.global.exception.BusinessException;
import com.todaypoor.global.exception.ErrorCode;
import com.todaypoor.ranking.entity.AiRankingRun;
import com.todaypoor.ranking.entity.DailyRankingEvent;
import com.todaypoor.ranking.entity.RankingEventStatus;
import com.todaypoor.ranking.client.ClaudeRankingClient;
import com.todaypoor.ranking.mock.dto.AiRankingOutput;
import com.todaypoor.ranking.mock.dto.UserAmountItem;
import com.todaypoor.ranking.repository.AiRankingRunRepository;
import com.todaypoor.ranking.repository.DailyRankingEventRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class InternalRankingService {

    private static final int TOP_RANK_LIMIT = 3;

    private final DailyRankingEventRepository dailyRankingEventRepository;
    private final AiRankingRunRepository aiRankingRunRepository;
    private final ExpenseRepository expenseRepository;
    private final ClaudeRankingClient claudeRankingClient;
    private final RankingPersistenceService rankingPersistenceService;

    /**
     * 크루의 랭킹 이벤트가 없으면 PENDING 상태로 생성한다.
     * 별도 트랜잭션으로 실행되어 즉시 커밋되므로,
     * 이후 processCrewRanking()이 실패해도 이벤트 레코드는 보존된다.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public DailyRankingEvent ensureEventExists(UUID crewId, LocalDate date) {
        return dailyRankingEventRepository
                .findByCrewIdAndRankingDate(crewId, date)
                .orElseGet(() -> dailyRankingEventRepository.save(
                        DailyRankingEvent.create(crewId, date)
                ));
    }

    /**
     * 단일 크루의 일일 랭킹을 생성한다.
     *
     * 트랜잭션 전략:
     *   - 이 메서드 자체는 @Transactional 없음 — Claude API 호출(최대 60초) 동안
     *     DB 커넥션을 점유하지 않도록 오케스트레이션 레이어만 담당한다.
     *   - DB 읽기: JPA Repository 자체 트랜잭션으로 처리 (짧은 커넥션 점유)
     *   - DB 쓰기: rankingPersistenceService.persistResults()가 REQUIRES_NEW 트랜잭션으로 처리
     */
    public DailyRankingEvent processCrewRanking(UUID crewId, LocalDate date) {
        // 1. 이벤트 조회 (Repository 자체 트랜잭션)
        DailyRankingEvent event = dailyRankingEventRepository
                .findByCrewIdAndRankingDate(crewId, date)
                .orElseThrow(() -> new BusinessException(ErrorCode.RANKING_NOT_FOUND));

        if (event.getStatus() == RankingEventStatus.SUCCESS) {
            log.info("크루 {} 의 {} 랭킹이 이미 완료됨, 건너뜀", crewId, date);
            return event;
        }

        // 2. 오늘 지출 데이터 수집 (Repository 자체 트랜잭션)
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);

        List<Expense> expenses = expenseRepository.findByCrewIdAndSpentAtBetween(
                crewId, startOfDay, endOfDay
        );

        // 3. 유저별 지출 합산 및 정렬
        Map<UUID, Integer> amountByUser = expenses.stream()
                .collect(Collectors.groupingBy(
                        Expense::getUserId,
                        Collectors.summingInt(Expense::getAmount)
                ));

        List<UserAmountItem> userAmounts = amountByUser.entrySet().stream()
                .sorted(Map.Entry.<UUID, Integer>comparingByValue().reversed())
                .map(e -> new UserAmountItem(e.getKey(), e.getValue()))
                .toList();

        if (userAmounts.isEmpty()) {
            log.warn("크루 {} 의 {} 지출 내역이 없어 랭킹 생성을 건너뜁니다.", crewId, date);
            throw new BusinessException(ErrorCode.INVALID_REQUEST);
        }

        // 4. 입력 데이터 직렬화 (감사 목적)
        String inputData = buildInputDataJson(crewId, date, userAmounts);

        // 5. Claude AI 호출 — 트랜잭션 없음, DB 커넥션 미점유 (최대 60초 대기)
        AiRankingOutput output = claudeRankingClient.generateRanking(userAmounts, inputData);

        // 6. 결과 저장 — REQUIRES_NEW 트랜잭션으로 짧게 처리
        DailyRankingEvent saved = rankingPersistenceService.persistResults(event.getId(), output);

        log.info("크루 {} 의 {} 랭킹 생성 완료 (eventId={})", crewId, date, event.getId());
        return saved;
    }

    /**
     * 랭킹 이벤트를 FAILED 상태로 마킹한다.
     * processCrewRanking() 실패 후 별도 트랜잭션으로 호출되어,
     * 롤백된 이벤트 없이 FAILED 상태가 정상 기록된다.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markEventFailed(UUID crewId, LocalDate date) {
        dailyRankingEventRepository
                .findByCrewIdAndRankingDate(crewId, date)
                .ifPresent(e -> e.updateStatus(RankingEventStatus.FAILED));
    }

    /**
     * 특정 랭킹 이벤트의 최신 AI 실행 기록을 조회한다.
     */
    @Transactional(readOnly = true)
    public AiRankingRun getLatestRun(UUID rankingEventId) {
        dailyRankingEventRepository.findById(rankingEventId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RANKING_NOT_FOUND));

        return aiRankingRunRepository
                .findTopByDailyRankingEventIdOrderByCreatedAtDesc(rankingEventId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RANKING_NOT_FOUND));
    }

    private String buildInputDataJson(UUID crewId, LocalDate date, List<UserAmountItem> userAmounts) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\"crewId\":\"").append(crewId).append("\"");
        sb.append(",\"date\":\"").append(date).append("\"");
        sb.append(",\"users\":[");
        for (int i = 0; i < userAmounts.size(); i++) {
            UserAmountItem item = userAmounts.get(i);
            if (i > 0) sb.append(",");
            sb.append("{\"userId\":\"").append(item.getUserId())
              .append("\",\"totalAmount\":").append(item.getTotalAmount()).append("}");
        }
        sb.append("]}");
        return sb.toString();
    }
}
