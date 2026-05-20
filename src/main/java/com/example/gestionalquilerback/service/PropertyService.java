package com.example.gestionalquilerback.service;

import com.example.gestionalquilerback.dto.PropertyRequest;
import com.example.gestionalquilerback.dto.PropertyResponse;
import com.example.gestionalquilerback.exception.ResourceNotFoundException;
import com.example.gestionalquilerback.model.entity.Property;
import com.example.gestionalquilerback.model.entity.PropertyImage;
import com.example.gestionalquilerback.repository.PropertyRepository;
import com.example.gestionalquilerback.security.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PropertyService {

    private final PropertyRepository repository;
    private final SecurityUtil securityUtil;

    public List<PropertyResponse> findAll() {
        List<Property> properties;
        if (securityUtil.isAdmin()) {
            properties = repository.findAll();
        } else {
            properties = repository.findByUserId(securityUtil.getCurrentUserId());
        }
        return properties.stream().map(this::toResponse).toList();
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
                .imageUrl(request.getImageUrl() != null && !request.getImageUrl().isBlank() ? request.getImageUrl() : null)
                .user(securityUtil.getCurrentUser())
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
        entity.setImageUrl(request.getImageUrl() != null && !request.getImageUrl().isBlank() ? request.getImageUrl() : null);
        return toResponse(repository.save(entity));
    }

    public void delete(Long id) {
        Property entity = findEntity(id);
        repository.deleteById(entity.getId());
    }

    public Property findEntity(Long id) {
        Property entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Property", id));
        if (!securityUtil.isAdmin() && !entity.getUser().getId().equals(securityUtil.getCurrentUserId())) {
            throw new ResourceNotFoundException("Property", id);
        }
        return entity;
    }

    public long count() {
        if (securityUtil.isAdmin()) {
            return repository.count();
        }
        return repository.countByUserId(securityUtil.getCurrentUserId());
    }

    @Transactional
    public PropertyResponse addImage(Long propertyId, String imageUrl) {
        Property entity = findEntity(propertyId);
        PropertyImage image = PropertyImage.builder()
                .property(entity)
                .imageUrl(imageUrl)
                .build();
        entity.getImages().add(image);
        if (entity.getImageUrl() == null) {
            entity.setImageUrl(imageUrl);
        }
        return toResponse(repository.save(entity));
    }

    @Transactional
    public PropertyResponse deleteImage(Long propertyId, String imageUrl) {
        Property entity = findEntity(propertyId);
        boolean removed = entity.getImages().removeIf(img -> img.getImageUrl().equals(imageUrl));
        if (!removed) throw new ResourceNotFoundException("Image not found: " + imageUrl);
        if (entity.getImages().isEmpty()) {
            entity.setImageUrl(null);
        } else {
            entity.setImageUrl(entity.getImages().getFirst().getImageUrl());
        }
        return toResponse(repository.save(entity));
    }

    private PropertyResponse toResponse(Property entity) {
        List<String> imageUrls = entity.getImages().stream()
                .map(PropertyImage::getImageUrl)
                .toList();
        if (imageUrls.isEmpty() && entity.getImageUrl() != null && !entity.getImageUrl().isBlank()) {
            imageUrls = List.of(entity.getImageUrl());
        }
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
                .imageUrl(entity.getImageUrl())
                .images(imageUrls)
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
