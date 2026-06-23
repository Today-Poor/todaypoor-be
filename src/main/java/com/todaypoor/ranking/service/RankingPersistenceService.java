package com.todaypoor.ranking.service;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import com.todaypoor.global.exception.BusinessException;
import com.todaypoor.global.exception.ErrorCode;
import com.todaypoor.ranking.entity.AiRankingRun;
import com.todaypoor.ranking.entity.AiRankingRunStatus;
import com.todaypoor.ranking.entity.AiResult;
import com.todaypoor.ranking.entity.AiResultStatus;
import com.todaypoor.ranking.entity.DailyRankingEvent;
import com.todaypoor.ranking.entity.RankingEventStatus;
import com.todaypoor.ranking.entity.RankingResult;
import com.todaypoor.ranking.mock.dto.AiRankingOutput;
import com.todaypoor.ranking.repository.AiRankingRunRepository;
import com.todaypoor.ranking.repository.AiResultRepository;
import com.todaypoor.ranking.repository.DailyRankingEventRepository;
import com.todaypoor.ranking.repository.RankingResultRepository;

/**
 * Claude AI 호출 후 랭킹 결과를 DB에 저장하는 역할만 담당한다.
 *
 * processCrewRanking이 외부 빈으로 이 메서드를 호출해야 Spring 프록시를 통해
 * REQUIRES_NEW 트랜잭션이 정상 적용된다. (같은 클래스 내 self-invocation 방지)
 */
@Service
@RequiredArgsConstructor
public class RankingPersistenceService {

    private static final int TOP_RANK_LIMIT = 3;

    private final DailyRankingEventRepository dailyRankingEventRepository;
    private final AiRankingRunRepository aiRankingRunRepository;
    private final RankingResultRepository rankingResultRepository;
    private final AiResultRepository aiResultRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public DailyRankingEvent persistResults(UUID eventId, AiRankingOutput output) {
        DailyRankingEvent event = dailyRankingEventRepository.findById(eventId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RANKING_NOT_FOUND));

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

        event.updateStatus(RankingEventStatus.SUCCESS);
        return event;
    }
}
