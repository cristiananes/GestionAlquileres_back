package com.example.gestionalquilerback.dto;
import lombok.*;
import java.math.BigDecimal;
import jakarta.validation.constraints.*;
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class PropertyInvestmentRequest {
    @PositiveOrZero @Digits(integer = 12, fraction = 2) private BigDecimal purchasePrice;
    @PositiveOrZero @Digits(integer = 12, fraction = 2) private BigDecimal purchaseCosts;
    @PositiveOrZero @Digits(integer = 12, fraction = 2) private BigDecimal renovationCosts;
    @PositiveOrZero @Digits(integer = 12, fraction = 2) private BigDecimal furnitureCosts;
    @PositiveOrZero @Digits(integer = 12, fraction = 2) private BigDecimal cashInvested;
}
