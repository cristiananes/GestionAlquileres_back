package com.example.gestionalquilerback.controller;
import com.example.gestionalquilerback.dto.*;
import com.example.gestionalquilerback.service.LeaseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController @RequestMapping("/api") @RequiredArgsConstructor
public class LeaseController {
    private final LeaseService service;
    @GetMapping("/properties/{id}/leases") public List<LeaseResponse> list(@PathVariable Long id, @RequestParam(defaultValue = "false") boolean archived) { return service.list(id, archived); }
    @PostMapping("/properties/{id}/leases") public LeaseResponse create(@PathVariable Long id, @Valid @RequestBody LeaseRequest req) { return service.create(id, req); }
    @PutMapping("/leases/{id}/occupancy") public LeaseResponse assign(@PathVariable Long id,@Valid @RequestBody OccupancyRequest req) {return service.assign(id,req);}
    @PostMapping("/leases/{id}/archive") public void archiveRecoverably(@PathVariable Long id) { service.archive(id, true); }
    @DeleteMapping("/leases/{id}") public void archive(@PathVariable Long id) { service.archive(id, true); }
    @PostMapping("/leases/{id}/restore") public void restore(@PathVariable Long id) { service.archive(id, false); }
}
