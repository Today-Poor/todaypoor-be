package com.todaypoor.ranking.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import com.todaypoor.crew.entity.AiMode;
import com.todaypoor.global.exception.BusinessException;
import com.todaypoor.global.exception.ErrorCode;
import com.todaypoor.ranking.mock.dto.AiRankingOutput;
import com.todaypoor.ranking.mock.dto.UserAmountItem;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Component
public class ClaudeRankingClient {

    private static final String ANTHROPIC_VERSION = "2023-06-01";
    private static final String PROMPT_VERSION = "v1.0.0";

    private static final String SYSTEM_PROMPT = """
            당신은 크루(소모임) 멤버들의 하루 지출 데이터를 분석해서 재미있는 소비 랭킹을 생성하는 AI입니다.
            입력으로 유저별 총 지출 금액이 주어지면, 아래 JSON 형식만 반환하세요. 다른 설명이나 마크다운 없이 JSON만 반환하세요.

            출력 형식:
            {
              "topic": "오늘의 랭킹 주제 (예: 오늘의 월급 암살자)",
              "criteria": "랭킹 선정 기준 한 문장",
              "rankings": [
                {
                  "userId": "유저UUID",
                  "rankNo": 1,
                  "title": "재미있는 칭호 (1~3위만)",
                  "roastMessage": "해당 유저를 향한 재치있는 한마디 (1~3위만)",
                  "mode": "ROAST 또는 COMFORT 또는 MEME 중 하나 (1~3위만)"
                }
              ]
            }

            규칙:
            - rankings 배열은 금액 내림차순으로 정렬하세요 (1위 = 가장 많이 씀).
            - 4위 이하는 title, roastMessage, mode를 null로 설정하세요.
            - mode는 반드시 ROAST, COMFORT, MEME 중 하나여야 합니다.
            - topic과 title은 재치 있고 한국적인 표현으로 작성하세요.
            - roastMessage는 1~2문장, 유머러스하게 작성하세요.
            """;

    private final RestClient restClient;
    private final String model;
    private final ObjectMapper objectMapper;

    public ClaudeRankingClient(
            @Value("${ranking.claude.api-key}") String apiKey,
            @Value("${ranking.claude.model}") String model,
            ObjectMapper objectMapper
    ) {
        this.model = model;
        this.objectMapper = objectMapper;

        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(10_000);
        factory.setReadTimeout(60_000);

        this.restClient = RestClient.builder()
                .baseUrl("https://api.anthropic.com")
                .defaultHeader("x-api-key", apiKey)
                .defaultHeader("anthropic-version", ANTHROPIC_VERSION)
                .requestFactory(factory)
                .build();
    }

    public AiRankingOutput generateRanking(List<UserAmountItem> userAmounts, String inputData) {
        try {
            String userPrompt = buildUserPrompt(userAmounts);

            ClaudeRequest request = new ClaudeRequest(
                    model,
                    SYSTEM_PROMPT,
                    1024,
                    List.of(new ClaudeRequest.Message("user", userPrompt))
            );

            ClaudeResponse response = restClient.post()
                    .uri("/v1/messages")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(ClaudeResponse.class);

            if (response == null || response.content() == null || response.content().isEmpty()) {
                throw new BusinessException(ErrorCode.RANKING_GENERATION_FAILED);
            }

            String responseText = response.content().get(0).text();
            return parseResponse(responseText, userAmounts, inputData);

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Claude 랭킹 API 호출 실패", e);
            throw new BusinessException(ErrorCode.RANKING_GENERATION_FAILED);
        }
    }

    private String buildUserPrompt(List<UserAmountItem> userAmounts) {
        StringBuilder sb = new StringBuilder("다음 크루 멤버들의 오늘 지출 데이터입니다:\n\n");
        for (int i = 0; i < userAmounts.size(); i++) {
            UserAmountItem item = userAmounts.get(i);
            sb.append((i + 1)).append(". userId=").append(item.getUserId())
              .append(", 총지출=").append(item.getTotalAmount()).append("원\n");
        }
        sb.append("\n위 데이터를 기반으로 랭킹을 생성해 주세요.");
        return sb.toString();
    }

    private AiRankingOutput parseResponse(String text, List<UserAmountItem> userAmounts, String inputData) {
        try {
            String json = extractJsonObject(text);
            Map<String, Object> parsed = objectMapper.readValue(json, new TypeReference<>() {});

            String topic = (String) parsed.get("topic");
            String criteria = (String) parsed.get("criteria");

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> rankingsRaw = (List<Map<String, Object>>) parsed.get("rankings");

            Map<UUID, Integer> amountByUser = userAmounts.stream()
                    .collect(Collectors.toMap(UserAmountItem::getUserId, UserAmountItem::getTotalAmount));

            List<AiRankingOutput.UserRankingItem> items = rankingsRaw.stream()
                    .map(r -> {
                        UUID userId = UUID.fromString((String) r.get("userId"));
                        int rankNo = ((Number) r.get("rankNo")).intValue();
                        int totalAmount = amountByUser.getOrDefault(userId, 0);
                        String title = (String) r.get("title");
                        String roastMessage = (String) r.get("roastMessage");
                        String modeStr = (String) r.get("mode");
                        AiMode mode = (modeStr != null) ? parseMode(modeStr) : null;
                        return new AiRankingOutput.UserRankingItem(userId, totalAmount, rankNo, title, roastMessage, mode);
                    })
                    .toList();

            return new AiRankingOutput(
                    topic,
                    criteria,
                    model,
                    0, 0,
                    PROMPT_VERSION,
                    inputData,
                    items
            );

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Claude 랭킹 응답 파싱 실패: {}", text, e);
            throw new BusinessException(ErrorCode.RANKING_GENERATION_FAILED);
        }
    }

    private String extractJsonObject(String text) {
        int start = text.indexOf('{');
        int end = text.lastIndexOf('}');
        if (start == -1 || end == -1 || start >= end) {
            throw new BusinessException(ErrorCode.RANKING_GENERATION_FAILED);
        }
        return text.substring(start, end + 1);
    }

    private AiMode parseMode(String modeStr) {
        try {
            return AiMode.valueOf(modeStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            return AiMode.ROAST;
        }
    }

    record ClaudeRequest(
            String model,
            String system,
            @JsonProperty("max_tokens") int maxTokens,
            List<Message> messages
    ) {
        record Message(String role, String content) {}
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record ClaudeResponse(List<ContentBlock> content) {
        @JsonIgnoreProperties(ignoreUnknown = true)
        record ContentBlock(String type, String text) {}
    }
}
