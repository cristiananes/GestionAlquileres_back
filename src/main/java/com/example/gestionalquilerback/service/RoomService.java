package com.example.gestionalquilerback.service;
import com.example.gestionalquilerback.dto.*;
import com.example.gestionalquilerback.model.entity.*;
import com.example.gestionalquilerback.model.enums.RentalScope;
import com.example.gestionalquilerback.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.List;
@Service @RequiredArgsConstructor @Transactional
public class RoomService {
 private final RoomRepository rooms;private final LeaseRepository leases;private final PropertyService properties;private final OccupancyService occupancy;
 public List<RoomResponse> list(Long propertyId,boolean archived,LocalDate on) { properties.findEntity(propertyId);return rooms.findByPropertyIdOrderByNumberAsc(propertyId).stream().filter(r->r.isArchived()==archived).map(r->response(r,on)).toList(); }
 public RoomResponse create(Long propertyId,RoomRequest req) {
  occupancy.lock(propertyId);var p=properties.findEntity(propertyId);if(p.isArchived()) throw new IllegalArgumentException("Restaura primero la propiedad");
  if(rooms.findByPropertyIdOrderByNumberAsc(propertyId).stream().anyMatch(r->r.getNumber()==req.number())) throw new IllegalArgumentException("Ese número de habitación ya existe, también puede estar archivado");
  return response(rooms.save(Room.builder().property(p).number(req.number()).name(req.name().trim()).build()),LocalDate.now());
 }
 public void archive(Long propertyId,Long roomId,boolean archived) {
  occupancy.lock(propertyId);var r=occupancy.room(roomId,propertyId);
  if(archived && leases.findByPropertyId(propertyId).stream().anyMatch(l->!l.isArchived() && l.getRoom()!=null && l.getRoom().getId().equals(roomId) && (l.getOccupancyTo()==null || !l.getOccupancyTo().isBefore(LocalDate.now())))) throw new IllegalArgumentException("La habitación tiene estancias vigentes o futuras. Finaliza o reasigna los contratos antes de archivarla");
  r.setArchived(archived);rooms.save(r);
 }
 private RoomResponse response(Room r,LocalDate on) {
  var stays=leases.findByPropertyId(r.getProperty().getId()).stream().filter(l->!l.isArchived() && (l.getRentalScope()==RentalScope.ENTIRE_PROPERTY || l.getRoom()!=null && l.getRoom().getId().equals(r.getId()))).map(l->new RoomResponse.Stay(l.getId(),l.getTenantName(),l.getOccupancyFrom()==null?l.getStartDate():l.getOccupancyFrom(),l.getOccupancyTo(),l.getRentalScope()==RentalScope.ENTIRE_PROPERTY)).toList();
  boolean occupied=stays.stream().anyMatch(s->!on.isBefore(s.from())&&(s.to()==null||!on.isAfter(s.to())));
  return new RoomResponse(r.getId(),r.getProperty().getId(),r.getName(),r.getNumber(),r.isArchived(),on,r.isArchived()?"ARCHIVED":occupied?"OCCUPIED":"AVAILABLE",stays);
 }
}
