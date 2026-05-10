package com.example.gestionalquilerback.repository;

import com.example.gestionalquilerback.model.entity.Property;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PropertyRepository extends JpaRepository<Property, Long> {
}
