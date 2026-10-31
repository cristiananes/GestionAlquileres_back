package com.example.gestionalquilerback.model.entity;
import jakarta.persistence.*;
import lombok.*;
@Entity @Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
@Table(uniqueConstraints=@UniqueConstraint(columnNames={"property_id","room_number"}))
public class Room {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @ManyToOne(optional=false,fetch=FetchType.LAZY) @JoinColumn(name="property_id") private Property property;
 @Column(nullable=false,length=100) private String name;
 @Column(name="room_number",nullable=false) private int number;
 @Column(nullable=false) private boolean archived;
}
