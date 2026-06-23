package com.todaypoor.ranking.mock;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.todaypoor.crew.entity.AiMode;
import com.todaypoor.ranking.mock.dto.AiRankingOutput;
import com.todaypoor.ranking.mock.dto.UserAmountItem;

/**
 * TODO: [MOCK] 실제 AI 클라이언트(OpenAI / Claude API) 구현 후 교체 예정.
 * 입력 데이터를 받아 하드코딩된 더미 랭킹 결과를 반환한다.
 */
@Profile("mock")
@Component
public class MockAiClient {

    private static final String MOCK_TOPIC = "오늘의 월급 암살자";
    private static final String MOCK_CRITERIA = "금액, 소비 맥락, 불필요성을 종합해 선정";
    private static final String MOCK_MODEL = "mock-gpt-4o";
    private static final String MOCK_PROMPT_VERSION = "v1.0.0-mock";
    private static final int MOCK_INPUT_TOKEN = 512;
    private static final int MOCK_OUTPUT_TOKEN = 256;

    // 1~3위 고정 피드백 (실제 연동 시 AI 응답으로 대체)
    private static final String[][] TOP3_FEEDBACK = {
        {"탕진왕 등극", "오늘도 지갑이 눈물을 흘리고 있습니다. 내일은 반성하실 건가요?", "ROAST"},
        {"은빛 낭비러", "2등도 대단합니다. 카드사가 당신께 감사 인사를 전하고 싶어 합니다.", "ROAST"},
        {"동메달 소비자", "3등이지만 지구의 지갑도 같이 아팠을 겁니다. 그래도 최선을 다했군요.", "ROAST"},
    };

    /**
     * 유저별 지출 금액을 받아 Mock 랭킹 결과를 생성한다.
     *
     * @param userAmounts 유저별 지출 합계 목록 (정렬 불필요)
     * @param inputData   AI에 전달한 입력 데이터 JSON (감사 목적 저장)
     * @return Mock AI 랭킹 결과
     */
    public AiRankingOutput generateRanking(List<UserAmountItem> userAmounts, String inputData) {
        // 금액 내림차순 정렬
        List<UserAmountItem> sorted = userAmounts.stream()
                .sorted(Comparator.comparingInt(UserAmountItem::getTotalAmount).reversed())
                .toList();

        List<AiRankingOutput.UserRankingItem> items = new ArrayList<>();
        for (int i = 0; i < sorted.size(); i++) {
            int rankNo = i + 1;
            UserAmountItem user = sorted.get(i);

            if (rankNo <= 3) {
                String[] feedback = TOP3_FEEDBACK[i];
                items.add(new AiRankingOutput.UserRankingItem(
                        user.getUserId(),
                        user.getTotalAmount(),
                        rankNo,
                        feedback[0],
                        feedback[1],
                        AiMode.valueOf(feedback[2])
                ));
            } else {
                items.add(new AiRankingOutput.UserRankingItem(
                        user.getUserId(),
                        user.getTotalAmount(),
                        rankNo,
                        null, null, null
                ));
            }
        }

        return new AiRankingOutput(
                MOCK_TOPIC,
                MOCK_CRITERIA,
                MOCK_MODEL,
                MOCK_INPUT_TOKEN,
                MOCK_OUTPUT_TOKEN,
                MOCK_PROMPT_VERSION,
                inputData,
                items
        );
    }
}
