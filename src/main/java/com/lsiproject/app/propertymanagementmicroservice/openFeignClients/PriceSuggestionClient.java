package com.lsiproject.app.propertymanagementmicroservice.openFeignClients;

import com.lsiproject.app.propertymanagementmicroservice.DTOs.PricePredictionRequestDTO;
import com.lsiproject.app.propertymanagementmicroservice.ResponseDTOs.PricePredictionResponseDTO;
import com.lsiproject.app.propertymanagementmicroservice.configuration.FeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Feign client for the ML-PriceSuggestion service.
 * Provides price prediction for properties based on monthly or daily rental types.
 */
@FeignClient(name = "ml-price-suggestion-service", url = "${ml-price-suggestion.service.url}", configuration = FeignConfig.class)
public interface PriceSuggestionClient {
    
    /**
     * Predicts monthly rental price for a property.
     * @param request Property details for prediction
     * @return Price prediction response
     */
    @PostMapping("/predict/monthly")
    PricePredictionResponseDTO predictMonthlyPrice(@RequestBody PricePredictionRequestDTO request);
    
    /**
     * Predicts daily rental price for a property.
     * @param request Property details for prediction
     * @return Price prediction response
     */
    @PostMapping("/predict/daily")
    PricePredictionResponseDTO predictDailyPrice(@RequestBody PricePredictionRequestDTO request);
}
