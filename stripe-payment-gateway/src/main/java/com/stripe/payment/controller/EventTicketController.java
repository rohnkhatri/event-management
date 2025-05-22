package com.stripe.payment.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.stripe.payment.dto.EventTicket;
import com.stripe.payment.dto.StripeResponse;
import com.stripe.payment.service.StripeService;

@RestController
@RequestMapping("/api/payment")
public class EventTicketController {
	
	@Autowired
	private StripeService stripeService;
	
	@PostMapping("/checkout")
	public ResponseEntity<StripeResponse> checkoutEventTicketPayment(@RequestBody EventTicket eventTicket) {
		StripeResponse stripeResponse = stripeService.checkoutEventTicketPayment(eventTicket);
		return ResponseEntity
				.status(HttpStatus.OK)
				.body(stripeResponse);
	}
	
}
