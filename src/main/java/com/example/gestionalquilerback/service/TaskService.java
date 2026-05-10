package com.example.gestionalquilerback.service;

import com.example.gestionalquilerback.dto.TaskRequest;
import com.example.gestionalquilerback.dto.TaskResponse;
import com.example.gestionalquilerback.exception.ResourceNotFoundException;
import com.example.gestionalquilerback.model.entity.Task;
import com.example.gestionalquilerback.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository repository;
    private final PropertyService propertyService;

    public List<TaskResponse> findAll(Long propertyId, Boolean completed) {
        if (propertyId != null) {
            return repository.findByPropertyId(propertyId).stream().map(this::toResponse).toList();
        }
        if (completed != null) {
            return repository.findByCompleted(completed).stream().map(this::toResponse).toList();
        }
        return repository.findAll().stream().map(this::toResponse).toList();
    }

    public TaskResponse findById(Long id) {
        return toResponse(findEntity(id));
    }

    public TaskResponse create(TaskRequest request) {
        Task entity = Task.builder()
                .property(request.getPropertyId() != null ? propertyService.findEntity(request.getPropertyId()) : null)
                .title(request.getTitle())
                .description(request.getDescription())
                .completed(request.isCompleted())
                .dueDate(request.getDueDate())
                .priority(request.getPriority() != null ? request.getPriority() : com.example.gestionalquilerback.model.enums.Priority.MEDIUM)
                .build();
        return toResponse(repository.save(entity));
    }

    public TaskResponse update(Long id, TaskRequest request) {
        Task entity = findEntity(id);
        entity.setProperty(request.getPropertyId() != null ? propertyService.findEntity(request.getPropertyId()) : null);
        entity.setTitle(request.getTitle());
        entity.setDescription(request.getDescription());
        entity.setCompleted(request.isCompleted());
        entity.setDueDate(request.getDueDate());
        entity.setPriority(request.getPriority());
        return toResponse(repository.save(entity));
    }

    public TaskResponse toggleCompleted(Long id) {
        Task entity = findEntity(id);
        entity.setCompleted(!entity.isCompleted());
        return toResponse(repository.save(entity));
    }

    public void delete(Long id) {
        if (!repository.existsById(id)) throw new ResourceNotFoundException("Task", id);
        repository.deleteById(id);
    }

    public long countPending() {
        return repository.countByCompleted(false);
    }

    public Task findEntity(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task", id));
    }

    private TaskResponse toResponse(Task entity) {
        return TaskResponse.builder()
                .id(entity.getId())
                .propertyId(entity.getProperty() != null ? entity.getProperty().getId() : null)
                .propertyName(entity.getProperty() != null ? entity.getProperty().getName() : null)
                .title(entity.getTitle())
                .description(entity.getDescription())
                .completed(entity.isCompleted())
                .dueDate(entity.getDueDate())
                .priority(entity.getPriority())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
