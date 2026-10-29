package com.example.gestionalquilerback.service;
import org.junit.jupiter.api.Test;
import java.time.*;
import com.example.gestionalquilerback.model.enums.RecurrenceFrequency;
import static org.junit.jupiter.api.Assertions.*;
class RecurrenceDatesTest {
    @Test void monthEndDoesNotDriftAfterFebruary() {
        var dates = RecurrenceDates.between(LocalDate.of(2026,1,31), null, RecurrenceFrequency.MONTHLY, LocalDate.of(2026,1,1), LocalDate.of(2026,3,31));
        assertEquals(java.util.List.of(LocalDate.of(2026,1,31),LocalDate.of(2026,2,28),LocalDate.of(2026,3,31)), dates);
    }
    @Test void leaseDay31StartingInFebruaryReturnsTo31InMarch() {
        var dates = RecurrenceDates.between(LocalDate.of(2026,2,28), null, RecurrenceFrequency.MONTHLY, LocalDate.of(2026,2,1), LocalDate.of(2026,3,31), 31);
        assertEquals(java.util.List.of(LocalDate.of(2026,2,28),LocalDate.of(2026,3,31)),dates);
    }
    @Test void quarterlyAndEndDatesAreInclusive() {
        assertEquals(2, RecurrenceDates.between(LocalDate.of(2026,1,1),LocalDate.of(2026,4,1),RecurrenceFrequency.QUARTERLY,LocalDate.of(2026,1,1),LocalDate.of(2026,12,31)).size());
    }
    @Test void leapYearAnnualDatesDoNotDrift() {
        var dates = RecurrenceDates.between(LocalDate.of(2024,2,29),null,RecurrenceFrequency.YEARLY,LocalDate.of(2025,1,1),LocalDate.of(2028,12,31));
        assertEquals(LocalDate.of(2028,2,29),dates.getLast());
    }
    @Test void weeklyAndRangeLimits() {
        assertEquals(5,RecurrenceDates.between(LocalDate.of(2026,1,1),null,RecurrenceFrequency.WEEKLY,LocalDate.of(2026,1,1),LocalDate.of(2026,1,31)).size());
        assertThrows(IllegalArgumentException.class,()->RecurrenceDates.between(LocalDate.of(2026,1,1),null,RecurrenceFrequency.WEEKLY,LocalDate.of(2026,1,1),LocalDate.of(2040,1,1)));
    }
}
