package com.lsiproject.app.propertymanagementmicroservice.openFeignClients;

import com.lsiproject.app.propertymanagementmicroservice.Enums.TypeOfRental;
import com.lsiproject.app.propertymanagementmicroservice.ResponseDTOs.HeatmapResponseDTO;
import com.lsiproject.app.propertymanagementmicroservice.configuration.FeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


@FeignClient(name = "ai-prediction-service", url ="${heatmap.service.url}" , configuration = FeignConfig.class)
public interface HeatMapPredictionClient {
    @GetMapping("/api/v1/market/heatmap")
    HeatmapResponseDTO getMarketHeatmap(@RequestParam("type") TypeOfRental type);
}
