package com.todaypoor.ranking.dto.response;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import com.todaypoor.crew.entity.AiMode;
import com.todaypoor.ranking.entity.AiRankingRun;
import com.todaypoor.ranking.entity.AiResult;
import com.todaypoor.ranking.entity.DailyRankingEvent;
import com.todaypoor.ranking.entity.RankingEventStatus;
import com.todaypoor.ranking.entity.RankingResult;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class RankingResponse {

    private UUID rankingEventId;
    private UUID crewId;
    private String crewName;
    private LocalDate rankingDate;
    private RankingEventStatus status;
    private String topic;
    private String rankingCriteria;
    private List<RankedEntry> rankings;  // 1~3위
    private List<OtherEntry> others;     // 4위 이하, 순서 없음

    public static RankingResponse of(
            DailyRankingEvent event,
            String crewName,
            AiRankingRun run,
            List<RankedEntry> rankings,
            List<OtherEntry> others
    ) {
        return new RankingResponse(
                event.getId(),
                event.getCrewId(),
                crewName,
                event.getRankingDate(),
                event.getStatus(),
                run.getGeneratedTopic(),
                run.getRankingCriteria(),
                rankings,
                others
        );
    }

    /** Mock 전용 팩토리 — 엔티티 없이 원시값으로 직접 생성한다. */
    public static RankingResponse ofMock(
            UUID rankingEventId,
            UUID crewId,
            String crewName,
            LocalDate rankingDate,
            RankingEventStatus status,
            String topic,
            String rankingCriteria,
            List<RankedEntry> rankings,
            List<OtherEntry> others
    ) {
        return new RankingResponse(
                rankingEventId, crewId, crewName, rankingDate,
                status, topic, rankingCriteria, rankings, others
        );
    }

    @Getter
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class RankedEntry {

        private UUID rankingResultId;
        private int rankNo;
        private int totalAmount;
        private UserInfo user;
        private AiResultInfo aiResult;

        public static RankedEntry of(RankingResult result, AiResult aiResult) {
            // TODO: User 도메인 연동 후 실제 사용자 정보로 교체
            return new RankedEntry(
                    result.getId(),
                    result.getRankNo(),
                    result.getTotalAmount(),
                    UserInfo.placeholder(result.getUserId()),
                    AiResultInfo.from(aiResult)
            );
        }

        /** Mock 전용 팩토리 */
        public static RankedEntry mock(
                UUID rankingResultId, int rankNo, int totalAmount,
                UserInfo user, AiResultInfo aiResult
        ) {
            return new RankedEntry(rankingResultId, rankNo, totalAmount, user, aiResult);
        }
    }

    @Getter
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class OtherEntry {

        private UUID userId;
        private String nickname;
        private String profileImageUrl;
        private int totalAmount;

        public static OtherEntry from(RankingResult result) {
            // TODO: User 도메인 연동 후 실제 사용자 정보로 교체
            return new OtherEntry(result.getUserId(), null, null, result.getTotalAmount());
        }

        /** Mock 전용 팩토리 */
        public static OtherEntry mock(UUID userId, String nickname, String profileImageUrl, int totalAmount) {
            return new OtherEntry(userId, nickname, profileImageUrl, totalAmount);
        }
    }

    @Getter
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class UserInfo {

        private UUID userId;
        private String nickname;
        private String profileImageUrl;

        static UserInfo placeholder(UUID userId) {
            // TODO: User 도메인 연동 후 실제 사용자 정보로 교체
            return new UserInfo(userId, null, null);
        }

        /** Mock 전용 팩토리 — 닉네임과 프로필 이미지를 직접 지정한다. */
        public static UserInfo of(UUID userId, String nickname, String profileImageUrl) {
            return new UserInfo(userId, nickname, profileImageUrl);
        }
    }

    @Getter
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class AiResultInfo {

        private UUID aiResultId;
        private String title;
        private String roastMessage;
        private AiMode mode;

        static AiResultInfo from(AiResult aiResult) {
            return new AiResultInfo(
                    aiResult.getId(),
                    aiResult.getTitle(),
                    aiResult.getRoastMessage(),
                    aiResult.getMode()
            );
        }

        /** Mock 전용 팩토리 */
        public static AiResultInfo mock(UUID aiResultId, String title, String roastMessage, AiMode mode) {
            return new AiResultInfo(aiResultId, title, roastMessage, mode);
        }
    }
}
