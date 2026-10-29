package com.example.gestionalquilerback.service;
import org.junit.jupiter.api.Test;
import java.nio.charset.StandardCharsets;
import static org.junit.jupiter.api.Assertions.*;
class DocumentValidationTest {
    @Test void detectsContentInsteadOfTrustingAnUploadedExtension() {
        assertEquals("application/pdf",DocumentService.detectType("%PDF-1.7".getBytes(StandardCharsets.US_ASCII)));
        assertThrows(IllegalArgumentException.class,()->DocumentService.detectType("<script>alert(1)</script>".getBytes(StandardCharsets.US_ASCII)));
    }
}
