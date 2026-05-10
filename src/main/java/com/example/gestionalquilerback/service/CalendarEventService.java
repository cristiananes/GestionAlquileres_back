package com.example.gestionalquilerback.service;

import com.example.gestionalquilerback.dto.CalendarEventRequest;
import com.example.gestionalquilerback.dto.CalendarEventResponse;
import com.example.gestionalquilerback.exception.ResourceNotFoundException;
import com.example.gestionalquilerback.model.entity.CalendarEvent;
import com.example.gestionalquilerback.repository.CalendarEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CalendarEventService {

    private final CalendarEventRepository repository;
    private final PropertyService propertyService;

    public List<CalendarEventResponse> findAll(Long propertyId, LocalDateTime from, LocalDateTime to) {
        if (propertyId != null) {
            return repository.findByPropertyId(propertyId).stream().map(this::toResponse).toList();
        }
        if (from != null && to != null) {
            return repository.findByStartDateTimeBetween(from, to).stream().map(this::toResponse).toList();
        }
        return repository.findAll().stream().map(this::toResponse).toList();
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
        if (!repository.existsById(id)) throw new ResourceNotFoundException("CalendarEvent", id);
        repository.deleteById(id);
    }

    public List<CalendarEventResponse> findUpcoming() {
        return repository.findByStartDateTimeAfter(LocalDateTime.now()).stream()
                .map(this::toResponse)
                .toList();
    }

    public CalendarEvent findEntity(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CalendarEvent", id));
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
