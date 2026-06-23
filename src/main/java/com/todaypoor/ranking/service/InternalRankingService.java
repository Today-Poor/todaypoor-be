package com.todaypoor.ranking.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Comparator;
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
import com.todaypoor.ranking.entity.AiRankingRunStatus;
import com.todaypoor.ranking.entity.AiResult;
import com.todaypoor.ranking.entity.AiResultStatus;
import com.todaypoor.ranking.entity.DailyRankingEvent;
import com.todaypoor.ranking.entity.RankingEventStatus;
import com.todaypoor.ranking.entity.RankingResult;
import com.todaypoor.ranking.mock.MockAiClient;
import com.todaypoor.ranking.mock.dto.AiRankingOutput;
import com.todaypoor.ranking.mock.dto.UserAmountItem;
import com.todaypoor.ranking.repository.AiRankingRunRepository;
import com.todaypoor.ranking.repository.AiResultRepository;
import com.todaypoor.ranking.repository.DailyRankingEventRepository;
import com.todaypoor.ranking.repository.RankingResultRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class InternalRankingService {

    private static final int TOP_RANK_LIMIT = 3;

    private final DailyRankingEventRepository dailyRankingEventRepository;
    private final AiRankingRunRepository aiRankingRunRepository;
    private final RankingResultRepository rankingResultRepository;
    private final AiResultRepository aiResultRepository;
    private final ExpenseRepository expenseRepository;
    private final MockAiClient mockAiClient;

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
     * REQUIRES_NEW 전파로 크루별로 독립된 트랜잭션에서 실행되어,
     * 한 크루의 실패가 다른 크루에 영향을 주지 않는다.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public DailyRankingEvent processCrewRanking(UUID crewId, LocalDate date) {
        DailyRankingEvent event = dailyRankingEventRepository
                .findByCrewIdAndRankingDate(crewId, date)
                .orElseThrow(() -> new BusinessException(ErrorCode.RANKING_NOT_FOUND));

        if (event.getStatus() == RankingEventStatus.SUCCESS) {
            log.info("크루 {} 의 {} 랭킹이 이미 완료됨, 건너뜀", crewId, date);
            return event;
        }

        // 1. 오늘 지출 데이터 수집
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);

        List<Expense> expenses = expenseRepository.findByCrewIdAndSpentAtBetween(
                crewId, startOfDay, endOfDay
        );

        // 2. 유저별 지출 합산 및 정렬
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

        // 3. 입력 데이터 직렬화 (감사 목적)
        String inputData = buildInputDataJson(crewId, date, userAmounts);

        // 4. Mock AI 호출
        AiRankingOutput output = mockAiClient.generateRanking(userAmounts, inputData);

        // 5. AiRankingRun 저장
        AiRankingRun run = aiRankingRunRepository.save(AiRankingRun.create(
                event.getId(),
                output.getInputData(),
                output.getGeneratedTopic(),
                output.getRankingCriteria(),
                output.getModel(),
                output.getInputToken(),
                output.getOutputToken(),
                output.getPromptVersion(),
                AiRankingRunStatus.SUCCESS,
                null
        ));

        // 6. RankingResult + AiResult(1~3위) 저장
        for (AiRankingOutput.UserRankingItem item : output.getUserRankings()) {
            RankingResult result = rankingResultRepository.save(RankingResult.create(
                    event.getId(),
                    run.getId(),
                    item.getUserId(),
                    item.getRankNo(),
                    item.getTotalAmount()
            ));

            if (item.getRankNo() <= TOP_RANK_LIMIT && item.getTitle() != null) {
                aiResultRepository.save(AiResult.create(
                        result.getId(),
                        item.getTitle(),
                        item.getRoastMessage(),
                        item.getMode(),
                        output.getInputData(),
                        output.getModel(),
                        output.getInputToken(),
                        output.getOutputToken(),
                        output.getPromptVersion(),
                        AiResultStatus.SUCCESS,
                        null
                ));
            }
        }

        // 7. 이벤트 상태 SUCCESS로 업데이트
        event.updateStatus(RankingEventStatus.SUCCESS);

        log.info("크루 {} 의 {} 랭킹 생성 완료 (eventId={})", crewId, date, event.getId());
        return event;
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
