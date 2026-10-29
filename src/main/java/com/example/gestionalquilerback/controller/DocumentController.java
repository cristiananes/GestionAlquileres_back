package com.example.gestionalquilerback.controller;
import com.example.gestionalquilerback.dto.DocumentResponse;
import com.example.gestionalquilerback.service.DocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
@RestController @RequestMapping("/api") @RequiredArgsConstructor
public class DocumentController {
    private final DocumentService service;
    @GetMapping("/properties/{id}/documents") public List<DocumentResponse> list(@PathVariable Long id, @RequestParam(defaultValue = "false") boolean archived) { return service.list(id, archived); }
    @PostMapping(value = "/properties/{id}/documents", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public DocumentResponse upload(@PathVariable Long id, @RequestParam(required = false) Long incomeId, @RequestParam(required = false) Long expenseId, @RequestParam MultipartFile file) throws IOException { return service.upload(id, incomeId, expenseId, file); }
    @GetMapping("/documents/{id}/download") public ResponseEntity<byte[]> download(@PathVariable Long id) {
        var d = service.entity(id);
        return ResponseEntity.ok().contentType(MediaType.parseMediaType(d.getContentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename(d.getFilename(), StandardCharsets.UTF_8).build().toString())
                .header("X-Content-Type-Options", "nosniff").header(HttpHeaders.CACHE_CONTROL, "no-store").body(d.getData());
    }
    @DeleteMapping("/documents/{id}") public void archive(@PathVariable Long id) { service.archive(id, true); }
    @PostMapping("/documents/{id}/restore") public void restore(@PathVariable Long id) { service.archive(id, false); }
}
