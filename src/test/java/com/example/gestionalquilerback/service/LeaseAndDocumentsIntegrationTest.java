package com.example.gestionalquilerback.service;
import com.example.gestionalquilerback.dto.*;
import com.example.gestionalquilerback.model.entity.*;
import com.example.gestionalquilerback.model.enums.*;
import com.example.gestionalquilerback.repository.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.mock.web.MockMultipartFile;
import java.math.*;
import java.time.*;
import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest @Transactional
class LeaseAndDocumentsIntegrationTest {
    @Autowired LeaseService leases;
    @Autowired DocumentService documents;
    @Autowired RecurrenceService recurrences;
    @Autowired CalendarEventRepository events;
    @Autowired UserRepository users;
    @Autowired PropertyRepository properties;
    @Autowired PropertyService propertyService;
    private Property property;
    @BeforeEach void user() {
        var user = users.save(User.builder().name("Owner").email("lease-doc-owner@test.example").password("test").role(Role.USER).build());
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(user,null,java.util.List.of()));
        property = properties.save(Property.builder().name("Piso").address("Calle 1").user(user).build());
    }
    @AfterEach void cleanup() { SecurityContextHolder.clearContext(); }
    @Test void contractCreatesOneRuleAndOneEventEvenOnRetryAndPreservesDay31() {
        var req = LeaseRequest.builder().tenantName("Tenant").startDate(LocalDate.of(2026,2,1)).endDate(LocalDate.of(2026,12,31)).monthlyRent(new BigDecimal("800")).paymentDay(31).generateRecurrence(true).clientRequestId("lease-retry").build();
        var first = leases.create(property.getId(),req);
        assertEquals(first.id(),leases.create(property.getId(),req).id());
        assertEquals(1,events.count());
        var due = recurrences.upcoming(property.getId(),LocalDate.of(2026,2,1),LocalDate.of(2026,3,31));
        assertEquals(LocalDate.of(2026,2,28),due.getFirst().dueDate());
        assertEquals(LocalDate.of(2026,3,31),due.getLast().dueDate());
    }
    @Test void attachmentsArePrivateAndRecoverable() throws Exception {
        var response = documents.upload(property.getId(),null,null,new MockMultipartFile("file","receipt.pdf","application/pdf","%PDF-1.7".getBytes(java.nio.charset.StandardCharsets.US_ASCII)));
        documents.archive(response.id(),true);
        assertEquals(0,documents.list(property.getId(),false).size());
        assertEquals(1,documents.list(property.getId(),true).size());
        documents.archive(response.id(),false);
        var other = users.save(User.builder().name("Other").email("lease-doc-other@test.example").password("test").role(Role.USER).build());
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(other,null,java.util.List.of()));
        assertThrows(com.example.gestionalquilerback.exception.ResourceNotFoundException.class,()->documents.entity(response.id()));
    }
    @Test void legacyPropertyEditsDoNotEraseInvestment() {
        property.setPurchasePrice(new BigDecimal("100000")); properties.saveAndFlush(property);
        propertyService.update(property.getId(),PropertyRequest.builder().name("Nuevo nombre").address("Calle 1").build());
        assertEquals(0,new BigDecimal("100000").compareTo(properties.findById(property.getId()).orElseThrow().getPurchasePrice()));
    }
}
