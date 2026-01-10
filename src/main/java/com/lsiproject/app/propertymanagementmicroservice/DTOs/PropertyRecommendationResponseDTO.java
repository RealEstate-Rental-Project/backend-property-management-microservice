package com.lsiproject.app.propertymanagementmicroservice.DTOs;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class PropertyRecommendationResponseDTO {
    Long property_id;
    Float similarity_score;
}
