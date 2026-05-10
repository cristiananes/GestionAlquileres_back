package com.example.gestionalquilerback.dto;

import com.example.gestionalquilerback.model.enums.Priority;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TaskResponse {
    private Long id;
    private Long propertyId;
    private String propertyName;
    private String title;
    private String description;
    private boolean completed;
    private LocalDate dueDate;
    private Priority priority;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
