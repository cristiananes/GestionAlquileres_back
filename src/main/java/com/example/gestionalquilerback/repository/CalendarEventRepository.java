package com.example.gestionalquilerback.repository;

import com.example.gestionalquilerback.model.entity.CalendarEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface CalendarEventRepository extends JpaRepository<CalendarEvent, Long> {

    List<CalendarEvent> findByPropertyId(Long propertyId);

    List<CalendarEvent> findByStartDateTimeBetween(LocalDateTime start, LocalDateTime end);

    List<CalendarEvent> findByStartDateTimeAfter(LocalDateTime dateTime);
}
