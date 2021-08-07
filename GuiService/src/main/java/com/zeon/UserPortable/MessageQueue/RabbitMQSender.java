package com.zeon.UserPortable.MessageQueue;

import java.util.List;

import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RabbitMQSender {

	@Autowired
	private AmqpTemplate rabbitTemplate;

	public void sendData(List<String> data) {

		String queueName = "BankReservation";
		String exchange = "direct-exchange";
		String routingKey = "BankReservationRoute";

		rabbitTemplate.convertAndSend(exchange, routingKey, data);
		System.out.println("The Send Message is : " + data);

	}

}
