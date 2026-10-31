package com.example.gestionalquilerback.controller;
import com.example.gestionalquilerback.dto.*;
import com.example.gestionalquilerback.service.RoomService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;
@RestController @RequiredArgsConstructor @RequestMapping("/api/properties/{propertyId}/rooms")
public class RoomController {
 private final RoomService service;
 @GetMapping public List<RoomResponse> list(@PathVariable Long propertyId,@RequestParam(defaultValue="false") boolean archived,@RequestParam(required=false) LocalDate on) {return service.list(propertyId,archived,on==null?LocalDate.now():on);}
 @PostMapping public RoomResponse create(@PathVariable Long propertyId,@Valid @RequestBody RoomRequest req) {return service.create(propertyId,req);}
 @PostMapping("/{id}/archive") public void archive(@PathVariable Long propertyId,@PathVariable Long id) {service.archive(propertyId,id,true);}
 @PostMapping("/{id}/restore") public void restore(@PathVariable Long propertyId,@PathVariable Long id) {service.archive(propertyId,id,false);}
}
