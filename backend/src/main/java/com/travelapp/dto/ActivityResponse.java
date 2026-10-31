package com.travelapp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityResponse {
    private Long id;
    private String name;
    private String description;
    private String priceRange;
    private String category;
    private BigDecimal rating;
    private Double latitude;
    private Double longitude;
    private String address;
    private String imageUrl;
    private List<String> interests;
}
