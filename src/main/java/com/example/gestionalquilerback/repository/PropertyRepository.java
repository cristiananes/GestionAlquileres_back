package com.example.gestionalquilerback.repository;

import com.example.gestionalquilerback.model.entity.Property;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PropertyRepository extends JpaRepository<Property, Long> {
    List<Property> findByUserId(Long userId);
    long countByUserId(Long userId);
}
