package com.mandateguardian.commitments.domain;

import java.time.LocalDate;
import java.time.YearMonth;

public final class MonthlyDueDateCalculator {

    public LocalDate nextDueDate(LocalDate referenceDate, int dueDay) {
        if (dueDay < 1 || dueDay > 31) {
            throw new IllegalArgumentException("A monthly due day must be between 1 and 31");
        }

        YearMonth month = YearMonth.from(referenceDate);
        LocalDate candidate = dateInMonth(month, dueDay);
        return candidate.isBefore(referenceDate)
                ? dateInMonth(month.plusMonths(1), dueDay)
                : candidate;
    }

    private LocalDate dateInMonth(YearMonth month, int dueDay) {
        return month.atDay(Math.min(dueDay, month.lengthOfMonth()));
    }
}
