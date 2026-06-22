package com.todaypoor.ranking.batch;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.todaypoor.expense.repository.ExpenseRepository;
import com.todaypoor.ranking.service.InternalRankingService;

/**
 * 매일 23시에 오늘 지출이 발생한 전체 크루의 랭킹을 자동 생성하는 배치 서비스.
 *
 * 트랜잭션 전파 전략:
 *   - 스케줄러 메서드 자체는 @Transactional 없음 (루프 컨텍스트)
 *   - ensureEventExists / processCrewRanking / markEventFailed 각각 REQUIRES_NEW
 *   → 크루 하나의 실패가 다른 크루 처리에 영향을 주지 않음
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiRankingBatchService {

    private final ExpenseRepository expenseRepository;
    private final InternalRankingService internalRankingService;

    @Scheduled(cron = "0 0 23 * * ?")
    public void runDailyRankingForAllCrews() {
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.atTime(LocalTime.MAX);

        List<UUID> crewIds = expenseRepository.findDistinctCrewIdsBySpentAtBetween(
                startOfDay, endOfDay
        );

        log.info("[배치] 일일 랭킹 시작 — 대상 크루 수: {}, 날짜: {}", crewIds.size(), today);

        for (UUID crewId : crewIds) {
            // 이벤트 레코드 선생성 (독립 트랜잭션 — 이후 실패해도 레코드 보존)
            try {
                internalRankingService.ensureEventExists(crewId, today);
            } catch (Exception e) {
                log.error("[배치] 크루 {} 이벤트 생성 실패, 건너뜀: {}", crewId, e.getMessage(), e);
                continue;
            }

            try {
                internalRankingService.processCrewRanking(crewId, today);
            } catch (Exception e) {
                log.error("[배치] 크루 {} 랭킹 생성 실패: {}", crewId, e.getMessage(), e);
                // 실패 상태를 별도 트랜잭션으로 기록 (processCrewRanking 롤백과 무관)
                internalRankingService.markEventFailed(crewId, today);
            }
        }

        log.info("[배치] 일일 랭킹 완료 — 날짜: {}", today);
    }
}
