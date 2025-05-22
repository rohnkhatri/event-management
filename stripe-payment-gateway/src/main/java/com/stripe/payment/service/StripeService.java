package com.stripe.payment.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import com.stripe.payment.dto.EventTicket;
import com.stripe.payment.dto.StripeResponse;

@Service
public class StripeService {
	
	@Value("${stripe.secretKey}")
	private String secretKey;
	
	public StripeResponse checkoutEventTicketPayment(EventTicket eventTicket) {
		
		Stripe.apiKey=secretKey;
		
		SessionCreateParams.LineItem.PriceData.ProductData productData = SessionCreateParams.LineItem.PriceData.ProductData.builder()
							.setName(eventTicket.getEventName())
							.build();
		
		SessionCreateParams.LineItem.PriceData priceData = SessionCreateParams.LineItem.PriceData.builder()
							.setCurrency(eventTicket.getCurrency()==null?"USD":eventTicket.getCurrency())
							.setUnitAmount(eventTicket.getPrice())
							.setProductData(productData)
							.build();
		
		SessionCreateParams.LineItem lineItem = SessionCreateParams.LineItem.builder()
							.setQuantity(eventTicket.getNoOfTicket())
							.setPriceData(priceData)
							.build();
		
		SessionCreateParams params = SessionCreateParams.builder()
							.addPaymentMethodType(SessionCreateParams.PaymentMethodType.CARD)
							.addPaymentMethodType(SessionCreateParams.PaymentMethodType.ALIPAY)
							.addPaymentMethodType(SessionCreateParams.PaymentMethodType.AMAZON_PAY)
							.setMode(SessionCreateParams.Mode.PAYMENT)
							.setSuccessUrl("http://localhost:8080/success")
							.setCancelUrl("http://localhost:8080/cancel")
							.addLineItem(lineItem)
							.build();
		
		Session session = null;
		
		try {
			session = Session.create(params);
		} catch (StripeException e) {
			// TODO Auto-generated catch block
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
		
		return StripeResponse.builder()
						.status("Success")
						.message("Payment Session Created")
						.sessionId(session.getId())
						.sessionUrl(session.getUrl())
						.build();
		
	}
}
