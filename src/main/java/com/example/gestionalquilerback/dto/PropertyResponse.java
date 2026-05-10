package com.example.gestionalquilerback.dto;

import com.example.gestionalquilerback.model.enums.PropertyCondition;
import com.example.gestionalquilerback.model.enums.PropertyType;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PropertyResponse {
    private Long id;
    private String name;
    private String address;
    private String city;
    private PropertyType propertyType;
    private BigDecimal areaM2;
    private Integer bedrooms;
    private Integer bathrooms;
    private PropertyCondition condition;
    private Boolean hasElevator;
    private Boolean hasParking;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
