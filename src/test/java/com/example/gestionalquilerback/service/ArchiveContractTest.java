package com.example.gestionalquilerback.service;

import com.example.gestionalquilerback.controller.*;
import com.example.gestionalquilerback.model.entity.*;
import com.example.gestionalquilerback.model.enums.*;
import com.example.gestionalquilerback.repository.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import java.math.BigDecimal;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest @Transactional
class ArchiveContractTest {
    @Autowired IncomeService service;
    @Autowired UserRepository users;
    @Autowired IncomeRepository incomes;
    @BeforeEach void login() {
        var user = users.save(User.builder().name("Archive owner").email("archive-contract@test.example").password("test").role(Role.USER).build());
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(user,null,java.util.List.of()));
        incomes.saveAndFlush(Income.builder().user(user).amount(BigDecimal.TEN).description("Active income").incomeDate(LocalDate.now()).incomeType(IncomeType.RENT).build());
    }
    @AfterEach void logout() { SecurityContextHolder.clearContext(); }
    @Test void activeIncomeIsNeverReturnedAsArchivedAndRestorePreservesIt() throws Exception {
        var active = service.findAll(null,null,null).getFirst();
        assertFalse(active.isArchived());
        assertTrue(service.findAll(null,null,null,true).isEmpty());
        service.delete(active.getId());
        assertTrue(service.findAll(null,null,null).isEmpty());
        assertTrue(service.findAll(null,null,null,true).getFirst().isArchived());
        var mvc = MockMvcBuilders.standaloneSetup(new IncomeController(service)).build();
        mvc.perform(post("/api/incomes/" + active.getId() + "/restore")).andExpect(status().isNoContent());
        assertTrue(service.findAll(null,null,null,true).isEmpty());
        assertFalse(service.findAll(null,null,null).getFirst().isArchived());
        assertEquals(1,incomes.count());
    }
    @Test void capabilitiesIdentifyTheArchiveContract() throws Exception {
        var mvc = MockMvcBuilders.standaloneSetup(new CapabilitiesController("test-commit")).build();
        mvc.perform(get("/api/capabilities")).andExpect(status().isOk())
                .andExpect(jsonPath("$.apiVersion").value(2)).andExpect(jsonPath("$.commit").value("test-commit"));
        assertTrue(new CapabilitiesController("test").capabilities().features().contains("soft-archive"));
    }
}
