package com.travelapp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class SavedTripRequest {

    @NotBlank
    private String cityName;

    @NotNull
    private LocalDate startDate;

    @NotNull
    private LocalDate endDate;

    private BigDecimal budget;
}
