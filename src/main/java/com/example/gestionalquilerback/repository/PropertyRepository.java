package com.example.gestionalquilerback.repository;

import com.example.gestionalquilerback.model.entity.Property;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PropertyRepository extends JpaRepository<Property, Long> {
 @org.springframework.data.jpa.repository.Query(value="select * from property where id=:id for update",nativeQuery=true)
 java.util.Optional<Property> lockProperty(@org.springframework.data.repository.query.Param("id") Long id);

    List<Property> findByUserId(Long userId);
    long countByUserId(Long userId);
}
