package com.example.gestionalquilerback.service;

import com.example.gestionalquilerback.dto.PropertyRequest;
import com.example.gestionalquilerback.dto.PropertyResponse;
import com.example.gestionalquilerback.exception.ResourceNotFoundException;
import com.example.gestionalquilerback.model.entity.Property;
import com.example.gestionalquilerback.repository.PropertyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PropertyService {

    private final PropertyRepository repository;

    public List<PropertyResponse> findAll() {
        return repository.findAll().stream().map(this::toResponse).toList();
    }

    public PropertyResponse findById(Long id) {
        return toResponse(findEntity(id));
    }

    public PropertyResponse create(PropertyRequest request) {
        Property entity = Property.builder()
                .name(request.getName())
                .address(request.getAddress())
                .city(request.getCity())
                .propertyType(request.getPropertyType())
                .areaM2(request.getAreaM2())
                .bedrooms(request.getBedrooms())
                .bathrooms(request.getBathrooms())
                .condition(request.getCondition())
                .hasElevator(request.getHasElevator())
                .hasParking(request.getHasParking())
                .description(request.getDescription())
                .build();
        return toResponse(repository.save(entity));
    }

    public PropertyResponse update(Long id, PropertyRequest request) {
        Property entity = findEntity(id);
        entity.setName(request.getName());
        entity.setAddress(request.getAddress());
        entity.setCity(request.getCity());
        entity.setPropertyType(request.getPropertyType());
        entity.setAreaM2(request.getAreaM2());
        entity.setBedrooms(request.getBedrooms());
        entity.setBathrooms(request.getBathrooms());
        entity.setCondition(request.getCondition());
        entity.setHasElevator(request.getHasElevator());
        entity.setHasParking(request.getHasParking());
        entity.setDescription(request.getDescription());
        return toResponse(repository.save(entity));
    }

    public void delete(Long id) {
        if (!repository.existsById(id)) throw new ResourceNotFoundException("Property", id);
        repository.deleteById(id);
    }

    public Property findEntity(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Property", id));
    }

    public long count() {
        return repository.count();
    }

    private PropertyResponse toResponse(Property entity) {
        return PropertyResponse.builder()
                .id(entity.getId())
                .name(entity.getName())
                .address(entity.getAddress())
                .city(entity.getCity())
                .propertyType(entity.getPropertyType())
                .areaM2(entity.getAreaM2())
                .bedrooms(entity.getBedrooms())
                .bathrooms(entity.getBathrooms())
                .condition(entity.getCondition())
                .hasElevator(entity.getHasElevator())
                .hasParking(entity.getHasParking())
                .description(entity.getDescription())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
