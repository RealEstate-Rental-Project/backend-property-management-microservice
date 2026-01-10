package com.lsiproject.app.propertymanagementmicroservice.ResponseDTOs;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.lsiproject.app.propertymanagementmicroservice.DTOs.HeatmapPointDTO;
import com.lsiproject.app.propertymanagementmicroservice.Enums.TypeOfRental;

import java.util.List;

public record HeatmapResponseDTO(
        @JsonProperty("rental_type") TypeOfRental rentalType,
        List<HeatmapPointDTO> data
) {
}
