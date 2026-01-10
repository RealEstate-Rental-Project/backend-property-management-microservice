package com.lsiproject.app.propertymanagementmicroservice.openFeignClients;


import com.lsiproject.app.propertymanagementmicroservice.DTOs.PropertyRecommendationRequestDTO;
import com.lsiproject.app.propertymanagementmicroservice.configuration.FeignConfig;
import com.lsiproject.app.propertymanagementmicroservice.wrappers.PropertyRecommendationResponseWrapper;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@FeignClient(
        name = "PropertyRecommendationModel",
        url = "${microservice.recommendation-ai.url}",
        configuration = FeignConfig.class
)
public interface PropertyRecommendationModel {
    @PostMapping("/recommend")
    PropertyRecommendationResponseWrapper recommend_properties(@RequestBody PropertyRecommendationRequestDTO request);
}
