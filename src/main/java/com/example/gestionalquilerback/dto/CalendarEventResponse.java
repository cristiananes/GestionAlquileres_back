package com.example.gestionalquilerback.dto;

import com.example.gestionalquilerback.model.enums.EventType;
import lombok.*;

import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CalendarEventResponse {
    private Long id;
    private Long propertyId;
    private String propertyName;
    private String title;
    private String description;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    private EventType eventType;
    private String color;
    private boolean allDay;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
