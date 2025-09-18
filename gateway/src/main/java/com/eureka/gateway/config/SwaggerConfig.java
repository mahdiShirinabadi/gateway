package com.eureka.gateway.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("API Gateway Service")
                        .description("API Gateway - مدیریت و routing درخواست‌ها به سرویس‌های مختلف")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Gateway Team")
                                .email("gateway@eureka.com")
                                .url("https://eureka.com"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("Development Gateway Server"),
                        new Server()
                                .url("https://gateway.eureka.com")
                                .description("Production Gateway Server")
                ));
    }
}

