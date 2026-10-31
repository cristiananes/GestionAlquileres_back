package com.example.gestionalquilerback.dto;
import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDate;
import java.math.BigDecimal;
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class LeaseRequest {
    private Long roomId;
    private com.example.gestionalquilerback.model.enums.RentalScope rentalScope;
    private LocalDate occupancyFrom;
    private LocalDate occupancyTo;
    @NotBlank @Size(max = 200) private String tenantName;
    @Email private String tenantEmail;
    @Size(max = 40) private String tenantPhone;
    @NotNull private LocalDate startDate;
    private LocalDate endDate;
    @NotNull @Positive @Digits(integer = 8, fraction = 2) private BigDecimal monthlyRent;
    @PositiveOrZero private BigDecimal depositAmount;
    @Min(1) @Max(31) private int paymentDay;
    private boolean generateRecurrence;
    @NotBlank @Size(max = 70) private String clientRequestId;
}
