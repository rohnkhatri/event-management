package com.stripe.payment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class StripePaymentGatewayApplication {

	public static void main(String[] args) {
		SpringApplication.run(StripePaymentGatewayApplication.class, args);
	}

}
