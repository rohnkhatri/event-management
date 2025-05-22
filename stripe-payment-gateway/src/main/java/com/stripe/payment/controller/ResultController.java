package com.stripe.payment.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
@RestController
@RequestMapping("/api/payment")
public class ResultController {
	
	@GetMapping("/success")
	public String success() {
		return "Payment was successful";
	}
	
	@GetMapping("/cancel") 
	public String cancel() {
		return "Payment cancelled";
	}
	
}
