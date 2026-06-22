package com.todaypoor.expense.dto.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import com.todaypoor.expense.entity.Expense;
import com.todaypoor.expense.entity.ExpenseCategory;
import com.todaypoor.expense.entity.ExpenseVisibility;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.Objects;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ExpenseDetailResponse {

    private UUID expenseId;
    private UUID crewId;
    private UserInfo user;
    private OcrResultInfo ocrResult;
    private ExpenseCategory category;
    private Integer amount;
    private String merchant;
    private String memo;
    private ExpenseVisibility visibility;
    private LocalDateTime spentAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static ExpenseDetailResponse of(Expense expense, UUID requestUserId, UserInfo user, OcrResultInfo ocrResult) {
        boolean isOwner = Objects.equals(expense.getUserId(), requestUserId);
        ExpenseVisibility visibility = expense.getVisibility();

        return new ExpenseDetailResponse(
                expense.getId(),
                expense.getCrewId(),
                user,
                ocrResult,
                isOwner ? expense.getCategory() : visibility.maskCategory(expense.getCategory()),
                isOwner ? expense.getAmount()   : visibility.maskAmount(expense.getAmount()),
                isOwner ? expense.getMerchant() : visibility.maskMerchant(expense.getMerchant()),
                isOwner ? expense.getMemo()     : visibility.maskMemo(expense.getMemo()),
                visibility,
                expense.getSpentAt(),
                expense.getCreatedAt(),
                expense.getUpdatedAt()
        );
    }

    @Getter
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class UserInfo {
        private UUID userId;
        private String nickname;
        private String profileImageUrl;

        public static UserInfo of(UUID userId, String nickname, String profileImageUrl) {
            return new UserInfo(userId, nickname, profileImageUrl);
        }
    }

    @Getter
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class OcrResultInfo {
        private UUID ocrResultId;
        private String rawText;

        // 임시로 리스트 처리 (나중에 OCR 도메인 구체화 시 DTO로 변경)
        private List<?> parsedData;

        public static OcrResultInfo of(UUID ocrResultId, String rawText, List<?> parsedData) {
            return new OcrResultInfo(ocrResultId, rawText, parsedData);
        }
    }
}