package com.todaypoor.expense.dto.request;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import com.todaypoor.expense.entity.ExpenseCategory;
import com.todaypoor.expense.entity.ExpenseVisibility;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ExpenseUpdateRequest {

    @NotNull(message = "카테고리는 필수입니다.")
    private ExpenseCategory category;

    @NotNull(message = "금액은 필수입니다.")
    @Positive(message = "금액은 0보다 커야 합니다.")
    private Integer amount;

    @NotBlank(message = "상호명은 필수입니다.")
    private String merchant;

    private String memo;

    @NotNull(message = "공개 범위는 필수입니다.")
    private ExpenseVisibility visibility;

    @NotNull(message = "결제 일시는 필수입니다.")
    private LocalDateTime spentAt;
}