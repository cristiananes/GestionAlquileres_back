package com.example.gestionalquilerback.repository;
import com.example.gestionalquilerback.model.entity.PropertyDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface PropertyDocumentRepository extends JpaRepository<PropertyDocument, Long> { List<PropertyDocument> findByPropertyId(Long propertyId); }
