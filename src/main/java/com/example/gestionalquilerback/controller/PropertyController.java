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
    private final com.example.gestionalquilerback.service.InvestmentService investmentService;

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    @GetMapping
    public ResponseEntity<List<PropertyResponse>> findAll(@RequestParam(defaultValue = "false") boolean archived) {
        return ResponseEntity.ok(service.findAll(archived));
    }

    @PutMapping("/{id}/investment")
    public PropertyResponse updateInvestment(@PathVariable Long id, @Valid @RequestBody com.example.gestionalquilerback.dto.PropertyInvestmentRequest request) {
        return service.updateInvestment(id, request);
    }

    @GetMapping("/{id}/investment")
    public com.example.gestionalquilerback.dto.InvestmentSummary investment(@PathVariable Long id,
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate from,
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate to) {
        return investmentService.summary(id, from, to);
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

    @PostMapping("/{id}/restore")
    public ResponseEntity<Void> restore(@PathVariable Long id) {
        service.restore(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping(value = "/{id}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PropertyResponse> uploadImage(@PathVariable Long id, @RequestParam("file") MultipartFile file) throws IOException {
        service.findEntity(id);
        byte[] bytes = file.getBytes();
        if (bytes.length == 0 || bytes.length > 5 * 1024 * 1024) throw new IllegalArgumentException("La imagen debe tener entre 1 byte y 5 MB");
        String extension;
        if (bytes.length >= 3 && (bytes[0] & 255) == 255 && (bytes[1] & 255) == 216 && (bytes[2] & 255) == 255) extension = ".jpg";
        else if (bytes.length >= 8 && (bytes[0] & 255) == 137 && bytes[1] == 'P' && bytes[2] == 'N' && bytes[3] == 'G') extension = ".png";
        else throw new IllegalArgumentException("Solo se admiten imágenes PNG y JPEG");
        String filename = UUID.randomUUID() + extension;
        Path uploadPath = Paths.get(uploadDir, "properties");
        Files.createDirectories(uploadPath);
        Path filePath = uploadPath.resolve(filename);
        file.transferTo(filePath.toFile());
        return ResponseEntity.ok(service.addImage(id, "/uploads/properties/" + filename));
    }

    @PostMapping("/{id}/images/delete")
    public ResponseEntity<PropertyResponse> deleteImage(@PathVariable Long id, @RequestBody java.util.Map<String, String> body) {
        return ResponseEntity.ok(service.deleteImage(id, body.get("imageUrl")));
    }
}
