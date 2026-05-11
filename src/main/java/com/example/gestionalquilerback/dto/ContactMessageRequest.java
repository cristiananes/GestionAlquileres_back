package com.example.gestionalquilerback.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ContactMessageRequest {

    @NotBlank
    private String name;

    @NotBlank @Email
    private String email;

    private String subject;

    @NotBlank
    private String message;

    @NotBlank
    private String type;
}
