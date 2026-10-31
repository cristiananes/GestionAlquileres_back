package com.example.gestionalquilerback.model.entity;
import jakarta.persistence.*;import lombok.*;import java.math.BigDecimal;
import com.example.gestionalquilerback.dto.ManagementRequest.*;
@Entity @Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class ManagementSettings {
 @Id private Long id;
 @OneToOne(optional=false) @MapsId @JoinColumn(name="property_id") private Property property;
 @Column(nullable=false,length=160) private String ownerName;
 @Column(length=180) private String ownerEmail;
 @Enumerated(EnumType.STRING) @Column(nullable=false) private FeeMode feeMode;
 @Column(nullable=false,precision=12,scale=2) private BigDecimal feeRate;
 @Column(nullable=false,precision=5,scale=2) private BigDecimal taxRate;
 @Enumerated(EnumType.STRING) @Column(nullable=false) private CollectionMode collectionMode;
}
