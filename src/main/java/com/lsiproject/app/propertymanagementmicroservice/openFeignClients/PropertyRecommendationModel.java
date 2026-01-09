package com.lsiproject.app.propertymanagementmicroservice.openFeignClients;


import com.lsiproject.app.propertymanagementmicroservice.DTOs.PropertyRecommendationRequestDTO;
import com.lsiproject.app.propertymanagementmicroservice.DTOs.PropertyRecommendationResponseDTO;
import com.lsiproject.app.propertymanagementmicroservice.configuration.FeignConfig;
import com.lsiproject.app.propertymanagementmicroservice.wrappers.PropertyRecommendationResponseWrapper;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(
        name = "PropertyRecommendationModel",
        url = "http://localhost:8001",
        configuration = FeignConfig.class
)
public interface PropertyRecommendationModel {
    @PostMapping("/recommend")
    PropertyRecommendationResponseWrapper recommend_properties(@RequestBody PropertyRecommendationRequestDTO request);
}
