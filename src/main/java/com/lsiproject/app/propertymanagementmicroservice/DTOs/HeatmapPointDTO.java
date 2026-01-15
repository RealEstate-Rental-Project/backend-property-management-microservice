package com.lsiproject.app.propertymanagementmicroservice.DTOs;

import com.fasterxml.jackson.annotation.JsonProperty;

public record HeatmapPointDTO(
        String neighborhood,
        Double latitude,
        Double longitude,
        @JsonProperty("current_avg_price") Double currentAvgPrice,
        @JsonProperty("trend_status") String trendStatus,
        @JsonProperty("trend_description") String trendDescription
) {}