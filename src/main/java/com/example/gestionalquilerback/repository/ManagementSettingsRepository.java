package com.example.gestionalquilerback.repository;
import com.example.gestionalquilerback.model.entity.ManagementSettings;
import org.springframework.data.jpa.repository.JpaRepository;
public interface ManagementSettingsRepository extends JpaRepository<ManagementSettings,Long> {
 @org.springframework.data.jpa.repository.Query(value="select * from management_settings where property_id=:propertyId for update",nativeQuery=true)
 java.util.Optional<ManagementSettings> lockSettings(@org.springframework.data.repository.query.Param("propertyId") Long propertyId);
}
