package com.example.gestionalquilerback.repository;

import com.example.gestionalquilerback.model.entity.ContactMessage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContactMessageRepository extends JpaRepository<ContactMessage, Long> {
}
