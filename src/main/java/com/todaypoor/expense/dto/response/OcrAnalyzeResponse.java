package com.todaypoor.expense.dto.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import com.todaypoor.expense.entity.ExpenseCategory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class OcrAnalyzeResponse {

    private UUID ocrResultId;
    private String rawText;
    private List<ParsedExpense> parsedExpenses;

    public static OcrAnalyzeResponse of(UUID ocrResultId, String rawText, List<ParsedExpense> parsedExpenses) {
        return new OcrAnalyzeResponse(ocrResultId, rawText, parsedExpenses);
    }

    @Getter
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class ParsedExpense {
        private String tempId;
        private ExpenseCategory category;
        private Integer amount;
        private String merchant;
        private LocalDateTime spentAt;

        public static ParsedExpense of(String tempId, ExpenseCategory category, Integer amount, String merchant, LocalDateTime spentAt) {
            return new ParsedExpense(tempId, category, amount, merchant, spentAt);
        }
    }
}