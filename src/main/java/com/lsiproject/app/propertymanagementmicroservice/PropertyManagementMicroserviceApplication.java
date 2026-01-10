package com.lsiproject.app.propertymanagementmicroservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients(basePackages = "com.lsiproject.app.propertymanagementmicroservice.openFeignClients")
public class PropertyManagementMicroserviceApplication {

    public static void main(String[] args) {
        SpringApplication.run(PropertyManagementMicroserviceApplication.class, args);
    }

}
