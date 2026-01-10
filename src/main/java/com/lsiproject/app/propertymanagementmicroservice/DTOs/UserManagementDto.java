package com.lsiproject.app.propertymanagementmicroservice.DTOs;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class UserManagementDto {

    private Long id;
    private String wallet;
    private String username;
    private String firstName;
    private String lastName;
    private String email;
    private String role;
    private String description;
    private Boolean enabled;
    private Instant createdAt;
    private Instant updatedAt;

    private Double targetRent;
    private Integer minTotalRooms;
    private Double targetSqft;
    private Double searchLatitude;
    private Double searchLongitude;
    private String preferredPropertyType;
    private String preferredRentalType;
}
