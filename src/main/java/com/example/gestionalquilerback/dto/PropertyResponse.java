package com.example.gestionalquilerback.dto;

import com.example.gestionalquilerback.model.enums.PropertyCondition;
import com.example.gestionalquilerback.model.enums.PropertyType;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PropertyResponse {
    private Long id;
    private boolean archived;
    private String name;
    private String address;
    private String city;
    private java.math.BigDecimal purchasePrice;
    private java.math.BigDecimal purchaseCosts;
    private java.math.BigDecimal renovationCosts;
    private java.math.BigDecimal furnitureCosts;
    private java.math.BigDecimal cashInvested;

    private PropertyType propertyType;
    private BigDecimal areaM2;
    private Integer bedrooms;
    private Integer bathrooms;
    private PropertyCondition condition;
    private Boolean hasElevator;
    private Boolean hasParking;
    private String description;
    private String imageUrl;
    private List<String> images;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
