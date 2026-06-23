package com.todaypoor.expense.entity;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ExpenseVisibilityMaskingTest {

    // ────────────────────────────────────────────────────────────
    // PUBLIC: 모든 필드 원본 반환
    // ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("PUBLIC - amount, category, merchant, memo 모두 원본을 반환한다.")
    void public_returnsAllFieldsAsIs() {
        assertThat(ExpenseVisibility.PUBLIC.maskAmount(5000)).isEqualTo(5000);
        assertThat(ExpenseVisibility.PUBLIC.maskCategory(ExpenseCategory.FOOD)).isEqualTo(ExpenseCategory.FOOD);
        assertThat(ExpenseVisibility.PUBLIC.maskMerchant("맥도날드")).isEqualTo("맥도날드");
        assertThat(ExpenseVisibility.PUBLIC.maskMemo("점심")).isEqualTo("점심");
    }

    // ────────────────────────────────────────────────────────────
    // HIDE_MERCHANT: merchant만 마스킹
    // ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("HIDE_MERCHANT - amount, category는 공개하고 merchant는 ***, memo는 null로 마스킹한다.")
    void hideMerchant_masksMerchantAndMemo() {
        assertThat(ExpenseVisibility.HIDE_MERCHANT.maskAmount(5000)).isEqualTo(5000);
        assertThat(ExpenseVisibility.HIDE_MERCHANT.maskCategory(ExpenseCategory.FOOD)).isEqualTo(ExpenseCategory.FOOD);
        assertThat(ExpenseVisibility.HIDE_MERCHANT.maskMerchant("맥도날드")).isEqualTo("***");
        assertThat(ExpenseVisibility.HIDE_MERCHANT.maskMemo("점심")).isNull();
    }

    // ────────────────────────────────────────────────────────────
    // AMOUNT_ONLY: amount만 공개
    // ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("AMOUNT_ONLY - amount만 공개하고 category는 null, merchant는 ***, memo는 null로 마스킹한다.")
    void amountOnly_exposesOnlyAmount() {
        assertThat(ExpenseVisibility.AMOUNT_ONLY.maskAmount(5000)).isEqualTo(5000);
        assertThat(ExpenseVisibility.AMOUNT_ONLY.maskCategory(ExpenseCategory.FOOD)).isNull();
        assertThat(ExpenseVisibility.AMOUNT_ONLY.maskMerchant("맥도날드")).isEqualTo("***");
        assertThat(ExpenseVisibility.AMOUNT_ONLY.maskMemo("점심")).isNull();
    }

    // ────────────────────────────────────────────────────────────
    // CATEGORY_ONLY: category만 공개
    // ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("CATEGORY_ONLY - category만 공개하고 amount는 null, merchant는 ***, memo는 null로 마스킹한다.")
    void categoryOnly_exposesOnlyCategory() {
        assertThat(ExpenseVisibility.CATEGORY_ONLY.maskAmount(5000)).isNull();
        assertThat(ExpenseVisibility.CATEGORY_ONLY.maskCategory(ExpenseCategory.FOOD)).isEqualTo(ExpenseCategory.FOOD);
        assertThat(ExpenseVisibility.CATEGORY_ONLY.maskMerchant("맥도날드")).isEqualTo("***");
        assertThat(ExpenseVisibility.CATEGORY_ONLY.maskMemo("점심")).isNull();
    }

    // ────────────────────────────────────────────────────────────
    // PRIVATE: 전체 마스킹
    // ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("PRIVATE - amount, category는 null, merchant는 ***, memo는 null로 전체 마스킹한다.")
    void private_masksAllFields() {
        assertThat(ExpenseVisibility.PRIVATE.maskAmount(5000)).isNull();
        assertThat(ExpenseVisibility.PRIVATE.maskCategory(ExpenseCategory.FOOD)).isNull();
        assertThat(ExpenseVisibility.PRIVATE.maskMerchant("맥도날드")).isEqualTo("***");
        assertThat(ExpenseVisibility.PRIVATE.maskMemo("점심")).isNull();
    }

    // ────────────────────────────────────────────────────────────
    // 엣지 케이스
    // ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("마스킹 대상 필드가 null이어도 예외 없이 처리된다.")
    void maskingNullFieldsDoesNotThrow() {
        assertThat(ExpenseVisibility.HIDE_MERCHANT.maskMerchant(null)).isEqualTo("***");
        assertThat(ExpenseVisibility.AMOUNT_ONLY.maskMemo(null)).isNull();
        assertThat(ExpenseVisibility.PUBLIC.maskMemo(null)).isNull();
    }
}
