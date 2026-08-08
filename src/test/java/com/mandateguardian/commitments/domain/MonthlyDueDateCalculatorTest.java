package com.mandateguardian.commitments.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class MonthlyDueDateCalculatorTest {

    private final MonthlyDueDateCalculator calculator = new MonthlyDueDateCalculator();

    @Test
    void retainsDueDateWhenItIsToday() {
        assertThat(calculator.nextDueDate(LocalDate.of(2026, 1, 15), 15))
                .isEqualTo(LocalDate.of(2026, 1, 15));
    }

    @Test
    void movesToNextMonthWhenThisMonthsDueDateHasPassed() {
        assertThat(calculator.nextDueDate(LocalDate.of(2026, 1, 16), 15))
                .isEqualTo(LocalDate.of(2026, 2, 15));
    }

    @Test
    void usesLastCalendarDayForMonthlyThirtyFirstInFebruary() {
        assertThat(calculator.nextDueDate(LocalDate.of(2026, 2, 1), 31))
                .isEqualTo(LocalDate.of(2026, 2, 28));
    }

    @Test
    void handlesLeapDayForMonthlyTwentyNinth() {
        assertThat(calculator.nextDueDate(LocalDate.of(2028, 2, 1), 29))
                .isEqualTo(LocalDate.of(2028, 2, 29));
    }

    @Test
    void rejectsAnInvalidDueDay() {
        assertThatThrownBy(() -> calculator.nextDueDate(LocalDate.of(2026, 1, 1), 32))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
