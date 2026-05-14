package com.example.gestionalquilerback.service;

import com.example.gestionalquilerback.dto.TaskRequest;
import com.example.gestionalquilerback.dto.TaskResponse;
import com.example.gestionalquilerback.exception.ResourceNotFoundException;
import com.example.gestionalquilerback.model.entity.Task;
import com.example.gestionalquilerback.repository.TaskRepository;
import com.example.gestionalquilerback.security.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository repository;
    private final PropertyService propertyService;
    private final SecurityUtil securityUtil;

    public List<TaskResponse> findAll(Long propertyId, Boolean completed) {
        List<Task> tasks;
        if (securityUtil.isAdmin()) {
            tasks = repository.findAll();
        } else {
            tasks = repository.findByUserId(securityUtil.getCurrentUserId());
        }
        return tasks.stream()
                .filter(t -> propertyId == null || (t.getProperty() != null && t.getProperty().getId().equals(propertyId)))
                .filter(t -> completed == null || t.isCompleted() == completed)
                .map(this::toResponse)
                .toList();
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
                .user(securityUtil.getCurrentUser())
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
        Task entity = findEntity(id);
        repository.deleteById(entity.getId());
    }

    public long countPending() {
        if (securityUtil.isAdmin()) {
            return repository.countByCompleted(false);
        }
        return repository.countByUserIdAndCompleted(securityUtil.getCurrentUserId(), false);
    }

    public Task findEntity(Long id) {
        Task entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task", id));
        if (!securityUtil.isAdmin() && !entity.getUser().getId().equals(securityUtil.getCurrentUserId())) {
            throw new ResourceNotFoundException("Task", id);
        }
        return entity;
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
