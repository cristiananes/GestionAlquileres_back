package com.example.gestionalquilerback.controller;

import com.example.gestionalquilerback.dto.PropertyRequest;
import com.example.gestionalquilerback.dto.PropertyResponse;
import com.example.gestionalquilerback.service.PropertyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/properties")
@RequiredArgsConstructor
public class PropertyController {

    private final PropertyService service;

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    @GetMapping
    public ResponseEntity<List<PropertyResponse>> findAll() {
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PropertyResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @PostMapping
    public ResponseEntity<PropertyResponse> create(@Valid @RequestBody PropertyRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PropertyResponse> update(@PathVariable Long id, @Valid @RequestBody PropertyRequest request) {
        return ResponseEntity.ok(service.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping(value = "/{id}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PropertyResponse> uploadImage(@PathVariable Long id, @RequestParam("file") MultipartFile file) throws IOException {
        String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();
        Path uploadPath = Paths.get(uploadDir, "properties");
        Files.createDirectories(uploadPath);
        Path filePath = uploadPath.resolve(filename);
        file.transferTo(filePath.toFile());

        PropertyResponse property = service.findById(id);
        PropertyRequest request = PropertyRequest.builder()
                .name(property.getName())
                .address(property.getAddress())
                .city(property.getCity())
                .propertyType(property.getPropertyType())
                .areaM2(property.getAreaM2())
                .bedrooms(property.getBedrooms())
                .bathrooms(property.getBathrooms())
                .condition(property.getCondition())
                .hasElevator(property.getHasElevator())
                .hasParking(property.getHasParking())
                .description(property.getDescription())
                .imageUrl("/uploads/properties/" + filename)
                .build();
        return ResponseEntity.ok(service.update(id, request));
    }
}
