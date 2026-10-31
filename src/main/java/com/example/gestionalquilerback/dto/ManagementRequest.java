package com.example.gestionalquilerback.dto;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
public record ManagementRequest(@NotBlank @Size(max=160) String ownerName,@Email @Size(max=180) String ownerEmail,
 @NotNull FeeMode feeMode,@NotNull @DecimalMin("0") @DecimalMax("9999999.99") @Digits(integer=7,fraction=2) BigDecimal feeRate,
 @NotNull @DecimalMin("0") @DecimalMax("100") @Digits(integer=3,fraction=2) BigDecimal taxRate,@NotNull CollectionMode collectionMode) {
 public enum FeeMode { PERCENT_RENT, FIXED }
 public enum CollectionMode { MANAGER, OWNER }
}
