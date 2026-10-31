package com.example.gestionalquilerback.service;
import com.example.gestionalquilerback.model.entity.*;
import com.example.gestionalquilerback.model.enums.RentalScope;
import com.example.gestionalquilerback.repository.*;
import com.example.gestionalquilerback.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
@Service @RequiredArgsConstructor @Transactional
public class OccupancyService {
 @jakarta.persistence.PersistenceContext private jakarta.persistence.EntityManager entityManager;
 private final PropertyService properties;
 private final PropertyRepository propertyRepository;
 private final RoomRepository rooms;
 private final LeaseRepository leases;
 public void lock(Long propertyId) { properties.findEntity(propertyId); propertyRepository.lockProperty(propertyId).orElseThrow(); }
 public Room room(Long id,Long propertyId) {return rooms.findById(id).filter(r->r.getProperty().getId().equals(propertyId)).orElseThrow(()->new ResourceNotFoundException("Habitación",id));}
 public Lease assign(Lease l,RentalScope scope,Long roomId,LocalDate from,LocalDate to) {
  lock(l.getProperty().getId());
  l=current(l);
  if(scope==null) scope=RentalScope.UNASSIGNED;
  if(from==null) from=l.getStartDate();
  if(to==null) to=l.getEndDate();
  DateRangeValidator.validate(from,to);
  if(from.isBefore(l.getStartDate()) || l.getEndDate()!=null && (to==null || to.isAfter(l.getEndDate()))) throw new IllegalArgumentException("La estancia debe estar dentro de las fechas del contrato");
  Room room=null;
  if(scope==RentalScope.ROOM) {if(roomId==null) throw new IllegalArgumentException("Selecciona la habitación"); room=room(roomId,l.getProperty().getId()); if(room.isArchived()) throw new IllegalArgumentException("Restaura la habitación antes de asignarla");}
  else if(roomId!=null) throw new IllegalArgumentException("Solo los contratos por habitación pueden vincular una habitación");
  l.setRentalScope(scope);l.setRoom(room);l.setOccupancyFrom(from);l.setOccupancyTo(to);
  if(!l.isArchived()) check(l);
  return l;
 }
 public Lease current(Lease l) {if(l.getId()==null)return l;entityManager.detach(l);return leases.lockLease(l.getId()).orElseThrow();}
 public void check(Lease l) {
  if(l.getRentalScope()==null || l.getRentalScope()==RentalScope.UNASSIGNED) return;
  if(l.getRoom()!=null && l.getRoom().isArchived()) throw new IllegalArgumentException("Restaura la habitación antes de recuperar el contrato");
  for(var other:leases.lockPropertyLeases(l.getProperty().getId())) {
   if(other.isArchived() || other.getId().equals(l.getId()) || other.getRentalScope()==null || other.getRentalScope()==RentalScope.UNASSIGNED) continue;
   boolean sameSpace=l.getRentalScope()==RentalScope.ENTIRE_PROPERTY || other.getRentalScope()==RentalScope.ENTIRE_PROPERTY || l.getRoom()!=null && other.getRoom()!=null && l.getRoom().getId().equals(other.getRoom().getId());
   var a=l.getOccupancyFrom()==null?l.getStartDate():l.getOccupancyFrom();var z=l.getOccupancyTo();
   var b=other.getOccupancyFrom()==null?other.getStartDate():other.getOccupancyFrom();var y=other.getOccupancyTo();
   if(sameSpace && (z==null||!b.isAfter(z)) && (y==null||!a.isAfter(y))) throw new IllegalArgumentException("La estancia se solapa con el contrato #"+other.getId()+" de "+other.getTenantName());
  }
 }
}
