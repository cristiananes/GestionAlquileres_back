package com.example.gestionalquilerback.dto;

import com.example.gestionalquilerback.model.enums.PropertyCondition;
import com.example.gestionalquilerback.model.enums.PropertyType;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.math.BigDecimal;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PropertyRequest {
    @NotBlank
    private String name;

    @NotBlank
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
}
