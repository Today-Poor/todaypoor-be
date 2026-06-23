package com.todaypoor.ai.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.todaypoor.global.exception.BusinessException;
import com.todaypoor.global.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

// 현재 OCR.space API를 임시로 사용 중.
// Google Cloud Vision API 결제 활성화 후 구현체를 교체할 것.
@Slf4j
@Component
public class GoogleVisionClient {

    private final RestClient restClient;
    private final String apiKey;

    public GoogleVisionClient(@Value("${ocr.ocrspace.api-key:helloworld}") String apiKey) {
        this.apiKey = apiKey;

        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(10_000);
        factory.setReadTimeout(30_000);

        this.restClient = RestClient.builder()
                .baseUrl("https://api.ocr.space")
                .requestFactory(factory)
                .build();
    }

    public String extractText(MultipartFile image) {
        try {
            byte[] bytes = image.getBytes();
            String filename = image.getOriginalFilename() != null ? image.getOriginalFilename() : "image.jpg";

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("apikey", apiKey);
            body.add("language", "kor");
            body.add("isOverlayRequired", "false");
            body.add("file", new ByteArrayResource(bytes) {
                @Override
                public String getFilename() {
                    return filename;
                }
            });

            OcrSpaceResponse response = restClient.post()
                    .uri("/parse/image")
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(body)
                    .retrieve()
                    .body(OcrSpaceResponse.class);

            if (response == null || response.isErroredOnProcessing()
                    || response.parsedResults() == null || response.parsedResults().isEmpty()) {
                throw new BusinessException(ErrorCode.OCR_FAILED);
            }

            String text = response.parsedResults().get(0).parsedText();
            if (text == null || text.isBlank()) {
                throw new BusinessException(ErrorCode.OCR_FAILED);
            }

            return text;

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("OCR.space API 호출 실패", e);
            throw new BusinessException(ErrorCode.OCR_FAILED);
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record OcrSpaceResponse(
            @JsonProperty("ParsedResults") List<ParsedResult> parsedResults,
            @JsonProperty("IsErroredOnProcessing") boolean isErroredOnProcessing
    ) {
        @JsonIgnoreProperties(ignoreUnknown = true)
        record ParsedResult(
                @JsonProperty("ParsedText") String parsedText
        ) {}
    }
}
