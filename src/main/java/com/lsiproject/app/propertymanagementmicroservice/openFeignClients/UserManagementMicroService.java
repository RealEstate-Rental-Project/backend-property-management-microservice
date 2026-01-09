package com.lsiproject.app.propertymanagementmicroservice.openFeignClients;


import com.lsiproject.app.propertymanagementmicroservice.DTOs.UserManagementDto;
import com.lsiproject.app.propertymanagementmicroservice.configuration.FeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(
        name = "UserManagementMicroService",
        url = "http://localhost:8081",
        configuration = FeignConfig.class
)
public interface UserManagementMicroService {
    @GetMapping("/api/users/id/{id}")
    UserManagementDto getUserById(@PathVariable Long id);
}
