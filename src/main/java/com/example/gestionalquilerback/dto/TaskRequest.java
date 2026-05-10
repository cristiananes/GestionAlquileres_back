package com.example.gestionalquilerback.dto;

import com.example.gestionalquilerback.model.enums.Priority;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalDate;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TaskRequest {
    private Long propertyId;

    @NotBlank
    private String title;

    private String description;

    private boolean completed;

    private LocalDate dueDate;

    private Priority priority;
}
