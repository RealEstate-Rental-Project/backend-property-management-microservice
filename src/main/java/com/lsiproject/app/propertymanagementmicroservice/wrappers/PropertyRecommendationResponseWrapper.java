package com.lsiproject.app.propertymanagementmicroservice.wrappers;

import com.lsiproject.app.propertymanagementmicroservice.DTOs.PropertyRecommendationResponseDTO;

import java.util.List;

public class PropertyRecommendationResponseWrapper {
    private List<PropertyRecommendationResponseDTO> recommendations;

    public List<PropertyRecommendationResponseDTO> getRecommendations() {
        return recommendations;
    }

    public void setRecommendations(List<PropertyRecommendationResponseDTO> recommendations) {
        this.recommendations = recommendations;
    }
}
