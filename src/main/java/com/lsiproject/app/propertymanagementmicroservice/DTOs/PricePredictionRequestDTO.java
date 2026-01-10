package com.lsiproject.app.propertymanagementmicroservice.DTOs;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO for sending property data to the ML-PriceSuggestion service.
 * Maps to the Input schema expected by the ML model.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PricePredictionRequestDTO {
    private String city;
    private String country;
    private Double longitude;
    private Double latitude;
    private Integer sqm;
    private Integer total_rooms;
    private Integer nombre_etoiles;
}
