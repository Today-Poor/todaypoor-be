package com.todaypoor.expense.entity;

public enum ExpenseVisibility {
    PUBLIC, AMOUNT_ONLY, CATEGORY_ONLY, HIDE_MERCHANT, PRIVATE;

    private static final String MASKED = "***";

    /**
     * visibility 정책에 따라 amount를 마스킹한다.
     * CATEGORY_ONLY, PRIVATE → null / 나머지 → 원본 반환
     */
    public Integer maskAmount(Integer amount) {
        return switch (this) {
            case PUBLIC, AMOUNT_ONLY, HIDE_MERCHANT -> amount;
            case CATEGORY_ONLY, PRIVATE -> null;
        };
    }

    /**
     * visibility 정책에 따라 category를 마스킹한다.
     * AMOUNT_ONLY, PRIVATE → null / 나머지 → 원본 반환
     */
    public ExpenseCategory maskCategory(ExpenseCategory category) {
        return switch (this) {
            case PUBLIC, CATEGORY_ONLY, HIDE_MERCHANT -> category;
            case AMOUNT_ONLY, PRIVATE -> null;
        };
    }

    /**
     * visibility 정책에 따라 merchant를 마스킹한다.
     * PUBLIC 외 전부 → "***"
     */
    public String maskMerchant(String merchant) {
        return (this == PUBLIC) ? merchant : MASKED;
    }

    /**
     * visibility 정책에 따라 memo를 마스킹한다.
     * PUBLIC 외 전부 → null
     */
    public String maskMemo(String memo) {
        return (this == PUBLIC) ? memo : null;
    }
}