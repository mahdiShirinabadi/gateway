package com.eureka.service1;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
@RestController
public class Service1Application {

	public static void main(String[] args) {
		SpringApplication.run(Service1Application.class, args);
	}

	@Bean
	public RestTemplate restTemplate() {
		return new RestTemplate();
	}

	@GetMapping("/app1/hello")
	public String hello(@RequestHeader(value = "X-Authenticated-User", required = false) String user) {
		return "Hello From Service 1 - Authenticated User: " + (user != null ? user : "Unknown");
	}

	@GetMapping("/app1/admin")
	public String admin(@RequestHeader(value = "X-Authenticated-User", required = false) String user) {
		return "Admin Panel From Service 1 - Authenticated User: " + (user != null ? user : "Unknown");
	}
}

