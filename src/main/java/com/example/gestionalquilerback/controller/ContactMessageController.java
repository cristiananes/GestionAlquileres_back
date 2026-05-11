package com.example.gestionalquilerback.controller;

import com.example.gestionalquilerback.dto.ContactMessageRequest;
import com.example.gestionalquilerback.dto.ContactMessageResponse;
import com.example.gestionalquilerback.service.ContactMessageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class ContactMessageController {

    private final ContactMessageService service;

    @PostMapping("/api/contact")
    public ResponseEntity<ContactMessageResponse> contact(@Valid @RequestBody ContactMessageRequest request) {
        request.setType("CONTACT");
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }

    @PostMapping("/api/feedback")
    public ResponseEntity<ContactMessageResponse> feedback(@Valid @RequestBody ContactMessageRequest request) {
        request.setType("FEEDBACK");
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }
}
