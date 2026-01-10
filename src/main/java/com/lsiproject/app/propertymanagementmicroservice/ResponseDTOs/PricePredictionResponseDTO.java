package com.lsiproject.app.propertymanagementmicroservice.ResponseDTOs;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO for receiving price prediction response from the ML-PriceSuggestion service.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PricePredictionResponseDTO {
    private String type;        // "MONTHLY" or "DAILY"
    private Long price_wei;     // Price in Wei
    private Double price_eth;   // Price in ETH
}
