package com.example.gestionalquilerback.service;

import java.time.LocalDate;

final class DateRangeValidator {
    private DateRangeValidator() {}

    static void validate(LocalDate from, LocalDate to) {
        if (from != null && to != null && from.isAfter(to)) {
            throw new IllegalArgumentException("La fecha de inicio debe ser anterior o igual a la final");
        }
    }
}
