package com.example.gestionalquilerback.repository;
import com.example.gestionalquilerback.model.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface RoomRepository extends JpaRepository<Room,Long> { List<Room> findByPropertyIdOrderByNumberAsc(Long propertyId); }
