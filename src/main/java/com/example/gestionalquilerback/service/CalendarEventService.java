package com.example.gestionalquilerback.service;

import com.example.gestionalquilerback.dto.CalendarEventRequest;
import com.example.gestionalquilerback.dto.CalendarEventResponse;
import com.example.gestionalquilerback.exception.ResourceNotFoundException;
import com.example.gestionalquilerback.model.entity.CalendarEvent;
import com.example.gestionalquilerback.repository.CalendarEventRepository;
import com.example.gestionalquilerback.security.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CalendarEventService {

    private final CalendarEventRepository repository;
    private final PropertyService propertyService;
    private final SecurityUtil securityUtil;

    public List<CalendarEventResponse> findAll(Long propertyId, LocalDateTime from, LocalDateTime to) {
        List<CalendarEvent> events;
        if (securityUtil.isAdmin()) {
            events = repository.findAll();
        } else {
            events = repository.findByUserId(securityUtil.getCurrentUserId());
        }
        return events.stream()
                .filter(e -> propertyId == null || (e.getProperty() != null && e.getProperty().getId().equals(propertyId)))
                .filter(e -> from == null || to == null || (!e.getStartDateTime().isBefore(from) && !e.getStartDateTime().isAfter(to)))
                .map(this::toResponse)
                .toList();
    }

    public CalendarEventResponse findById(Long id) {
        return toResponse(findEntity(id));
    }

    public CalendarEventResponse create(CalendarEventRequest request) {
        CalendarEvent entity = CalendarEvent.builder()
                .property(request.getPropertyId() != null ? propertyService.findEntity(request.getPropertyId()) : null)
                .title(request.getTitle())
                .description(request.getDescription())
                .startDateTime(request.getStartDateTime())
                .endDateTime(request.getEndDateTime())
                .eventType(request.getEventType())
                .color(request.getColor())
                .allDay(request.isAllDay())
                .user(securityUtil.getCurrentUser())
                .build();
        return toResponse(repository.save(entity));
    }

    public CalendarEventResponse update(Long id, CalendarEventRequest request) {
        CalendarEvent entity = findEntity(id);
        entity.setProperty(request.getPropertyId() != null ? propertyService.findEntity(request.getPropertyId()) : null);
        entity.setTitle(request.getTitle());
        entity.setDescription(request.getDescription());
        entity.setStartDateTime(request.getStartDateTime());
        entity.setEndDateTime(request.getEndDateTime());
        entity.setEventType(request.getEventType());
        entity.setColor(request.getColor());
        entity.setAllDay(request.isAllDay());
        return toResponse(repository.save(entity));
    }

    public void delete(Long id) {
        CalendarEvent entity = findEntity(id);
        repository.deleteById(entity.getId());
    }

    public List<CalendarEventResponse> findUpcoming() {
        if (securityUtil.isAdmin()) {
            return repository.findByStartDateTimeAfter(LocalDateTime.now()).stream()
                    .map(this::toResponse)
                    .toList();
        }
        return repository.findByUserIdAndStartDateTimeAfter(securityUtil.getCurrentUserId(), LocalDateTime.now()).stream()
                .map(this::toResponse)
                .toList();
    }

    public CalendarEvent findEntity(Long id) {
        CalendarEvent entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CalendarEvent", id));
        if (!securityUtil.isAdmin() && !entity.getUser().getId().equals(securityUtil.getCurrentUserId())) {
            throw new ResourceNotFoundException("CalendarEvent", id);
        }
        return entity;
    }

    private CalendarEventResponse toResponse(CalendarEvent entity) {
        return CalendarEventResponse.builder()
                .id(entity.getId())
                .propertyId(entity.getProperty() != null ? entity.getProperty().getId() : null)
                .propertyName(entity.getProperty() != null ? entity.getProperty().getName() : null)
                .title(entity.getTitle())
                .description(entity.getDescription())
                .startDateTime(entity.getStartDateTime())
                .endDateTime(entity.getEndDateTime())
                .eventType(entity.getEventType())
                .color(entity.getColor())
                .allDay(entity.isAllDay())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
