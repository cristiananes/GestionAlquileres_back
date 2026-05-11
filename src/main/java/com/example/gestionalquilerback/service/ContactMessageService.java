package com.example.gestionalquilerback.service;

import com.example.gestionalquilerback.dto.ContactMessageRequest;
import com.example.gestionalquilerback.dto.ContactMessageResponse;
import com.example.gestionalquilerback.model.entity.ContactMessage;
import com.example.gestionalquilerback.repository.ContactMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ContactMessageService {

    private final ContactMessageRepository repository;

    public ContactMessageResponse create(ContactMessageRequest request) {
        ContactMessage entity = ContactMessage.builder()
                .name(request.getName())
                .email(request.getEmail())
                .subject(request.getSubject())
                .message(request.getMessage())
                .type(request.getType())
                .build();
        return toResponse(repository.save(entity));
    }

    private ContactMessageResponse toResponse(ContactMessage entity) {
        return ContactMessageResponse.builder()
                .id(entity.getId())
                .name(entity.getName())
                .email(entity.getEmail())
                .subject(entity.getSubject())
                .message(entity.getMessage())
                .type(entity.getType())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
