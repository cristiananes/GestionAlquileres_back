package com.example.gestionalquilerback.repository;

import com.example.gestionalquilerback.model.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findByPropertyId(Long propertyId);

    List<Task> findByCompleted(boolean completed);

    long countByCompleted(boolean completed);
}
