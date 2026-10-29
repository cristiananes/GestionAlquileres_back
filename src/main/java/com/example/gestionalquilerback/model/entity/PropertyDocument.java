package com.example.gestionalquilerback.model.entity;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
@Entity @Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class PropertyDocument {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(optional = false) private Property property;
    @ManyToOne(optional = false) private User user;
    @ManyToOne private Income income;
    @ManyToOne private Expense expense;
    private String filename;
    private String contentType;
    private long size;
    @Lob @Column(length = 5242880, nullable = false) private byte[] data;
    @Column(nullable = false, columnDefinition = "boolean default false") private boolean archived;
    private LocalDateTime createdAt;
    @PrePersist void created() { createdAt = LocalDateTime.now(); }
}
