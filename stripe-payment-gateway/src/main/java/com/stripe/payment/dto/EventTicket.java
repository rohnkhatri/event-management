package com.stripe.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EventTicket {
	
	private String eventName;
	private String currency;
	private Long noOfTicket;
	private Long price;
	
}
