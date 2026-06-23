package com.todaypoor.ai.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import com.todaypoor.expense.dto.response.OcrAnalyzeResponse;
import com.todaypoor.expense.entity.ExpenseCategory;
import com.todaypoor.global.exception.BusinessException;
import com.todaypoor.global.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
public class ClaudeOcrParserClient {

    private static final String ANTHROPIC_VERSION = "2023-06-01";
    private static final String SYSTEM_PROMPT = """
            당신은 영수증 OCR 원문에서 소비 내역을 추출하는 파서입니다.
            주어진 텍스트에서 소비 내역을 추출하여 JSON 배열만 반환하세요. 다른 설명이나 마크다운 코드 블록 없이 JSON 배열만 반환하세요.

            지원 카테고리: CAFE, DELIVERY, FOOD, CONVENIENCE, SHOPPING, BEAUTY, TRANSPORT, ENTERTAINMENT, SUBSCRIPTION, GIFT, STUDY, HEALTH, ETC

            응답 형식 (예시):
            [{"tempId":"temp-1","category":"FOOD","amount":6500,"merchant":"맘스터치 대구점","spentAt":"2026-05-20T14:30:00"}]

            규칙:
            - tempId는 "temp-1", "temp-2" 순서로 부여하세요.
            - amount는 정수(원 단위)입니다.
            - 날짜 정보가 없으면 오늘 날짜 현재 시각을 ISO 8601 형식으로 사용하세요.
            - 카테고리를 판단할 수 없으면 ETC를 사용하세요.
            """;

    private final RestClient restClient;
    private final String model;
    private final ObjectMapper objectMapper;

    public ClaudeOcrParserClient(
            @Value("${ocr.claude.api-key}") String apiKey,
            @Value("${ocr.claude.model}") String model,
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

    public List<OcrAnalyzeResponse.ParsedExpense> parseExpenses(String rawText) {
        try {
            ClaudeRequest request = new ClaudeRequest(
                    model,
                    SYSTEM_PROMPT,
                    1024,
                    List.of(new ClaudeRequest.Message("user", "다음 OCR 원문에서 소비 내역을 추출해주세요:\n\n" + rawText))
            );

            ClaudeResponse response = restClient.post()
                    .uri("/v1/messages")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(ClaudeResponse.class);

            if (response == null || response.content() == null || response.content().isEmpty()) {
                throw new BusinessException(ErrorCode.AI_PARSING_FAILED);
            }

            String responseText = response.content().get(0).text();
            return parseToParsedExpenses(responseText);

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Claude API 호출 실패", e);
            throw new BusinessException(ErrorCode.AI_PARSING_FAILED);
        }
    }

    private List<OcrAnalyzeResponse.ParsedExpense> parseToParsedExpenses(String text) {
        try {
            String json = extractJsonArray(text);
            List<ParsedExpenseDto> dtos = objectMapper.readValue(json, new TypeReference<>() {});
            return dtos.stream().map(ParsedExpenseDto::toResponse).toList();
        } catch (Exception e) {
            log.error("Claude 응답 파싱 실패: {}", text, e);
            throw new BusinessException(ErrorCode.AI_PARSING_FAILED);
        }
    }

    private String extractJsonArray(String text) {
        int start = text.indexOf('[');
        int end = text.lastIndexOf(']');
        if (start == -1 || end == -1 || start >= end) {
            throw new BusinessException(ErrorCode.AI_PARSING_FAILED);
        }
        return text.substring(start, end + 1);
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

    @JsonIgnoreProperties(ignoreUnknown = true)
    record ParsedExpenseDto(
            String tempId,
            String category,
            Integer amount,
            String merchant,
            String spentAt
    ) {
        OcrAnalyzeResponse.ParsedExpense toResponse() {
            ExpenseCategory expenseCategory;
            try {
                expenseCategory = ExpenseCategory.valueOf(category);
            } catch (IllegalArgumentException e) {
                expenseCategory = ExpenseCategory.ETC;
            }
            LocalDateTime dateTime = (spentAt != null && !spentAt.isBlank())
                    ? LocalDateTime.parse(spentAt)
                    : LocalDateTime.now();
            return OcrAnalyzeResponse.ParsedExpense.of(tempId, expenseCategory, amount, merchant, dateTime);
        }
    }
}
