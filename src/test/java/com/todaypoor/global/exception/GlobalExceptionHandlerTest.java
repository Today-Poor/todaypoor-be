package com.todaypoor.global.exception;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = TestExceptionController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void businessException_returnsMappedErrorResponse() throws Exception {
        mockMvc.perform(get("/test/business"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("CREW_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("크루를 찾을 수 없습니다."))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    void validationFailure_returnsInvalidRequestWithErrors() throws Exception {
        String invalidBody = """
                {
                  "amount": null,
                  "merchant": ""
                }
                """;

        mockMvc.perform(
                        post("/test/validation")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(invalidBody)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"))
                .andExpect(jsonPath("$.message").value("잘못된 요청입니다."))
                .andExpect(jsonPath("$.data.errors").isArray())
                .andExpect(jsonPath("$.data.errors.length()").value(2))
                .andExpect(jsonPath("$.data.errors[0].field").exists())
                .andExpect(jsonPath("$.data.errors[0].message").exists());
    }

    @Test
    void typeMismatch_returnsInvalidRequest() throws Exception {
        mockMvc.perform(get("/test/type-mismatch").param("count", "abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"))
                .andExpect(jsonPath("$.message").value("잘못된 요청입니다."))
                .andExpect(jsonPath("$.data").isEmpty());
    }
}
