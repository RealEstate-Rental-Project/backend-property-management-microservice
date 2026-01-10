package com.lsiproject.app.propertymanagementmicroservice.DTOs;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;


@ToString
@Getter
@Setter
@AllArgsConstructor
public class PropertyRecommendationRequestDTO {
    private BigDecimal targetRent;
    private Integer minTotalRooms;
    private Double targetSqft;
    private Double searchLatitude;
    private Double searchLongitude;
    private String preferredPropertyType;
    private String preferredRentalType;
    private Integer numberOfPeople;
    private Boolean isMarried;
}
