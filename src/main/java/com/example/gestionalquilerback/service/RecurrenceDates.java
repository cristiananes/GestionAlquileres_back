package com.example.gestionalquilerback.service;
import com.example.gestionalquilerback.model.enums.RecurrenceFrequency;
import java.time.*;
import java.util.*;
public final class RecurrenceDates {
    private RecurrenceDates() {}
    public static List<LocalDate> between(LocalDate anchor, LocalDate end, RecurrenceFrequency frequency, LocalDate from, LocalDate to) {
        return between(anchor, end, frequency, from, to, null);
    }
    public static List<LocalDate> between(LocalDate anchor, LocalDate end, RecurrenceFrequency frequency, LocalDate from, LocalDate to, Integer dayOfMonth) {
        DateRangeValidator.validate(from, to);
        if (anchor == null || frequency == null || from == null || to == null) throw new IllegalArgumentException("Indica las fechas y frecuencia");
        if (to.isAfter(from.plusYears(10))) throw new IllegalArgumentException("El intervalo máximo es de diez años");
        var dates = new ArrayList<LocalDate>();
        // Always calculate from the original anchor: Jan 31 -> Feb 28 -> Mar 31.
        long index = Math.max(0, java.time.temporal.ChronoUnit.MONTHS.between(anchor.withDayOfMonth(1), from.withDayOfMonth(1)) - 1);
        index = switch (frequency) { case WEEKLY -> Math.max(0, java.time.temporal.ChronoUnit.WEEKS.between(anchor, from) - 1); case MONTHLY -> index; case QUARTERLY -> index / 3; case YEARLY -> index / 12; };
        for (int count = 0; count < 4000; count++, index++) {
            LocalDate date = switch (frequency) {
                case WEEKLY -> anchor.plusWeeks(index);
                case MONTHLY -> anchor.plusMonths(index);
                case QUARTERLY -> anchor.plusMonths(index * 3);
                case YEARLY -> anchor.plusYears(index);
            };
            if (dayOfMonth != null && frequency != RecurrenceFrequency.WEEKLY) date = date.withDayOfMonth(Math.min(dayOfMonth, date.lengthOfMonth()));
            if (date.isAfter(to) || (end != null && date.isAfter(end))) break;
            if (!date.isBefore(from)) dates.add(date);
        }
        return dates;
    }
}
