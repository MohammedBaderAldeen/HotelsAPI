package com.zeon.UserPortable.MessageQueue;

import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQDirectConfig {

	@Bean
	DirectExchange exchange() {
		return new DirectExchange("direct-exchange");
	}
	
	@Bean
	Queue userRequestDelete() {
		return new Queue("userRequestDelete", false);
	}

	@Bean
	Queue userHotelQueue() {
		return new Queue("userHotelQueue", false);
	}

	@Bean
	Queue userHotelPostQueue() {
		return new Queue("userHotelPostQueue", false);
	}
	
	@Bean
	Queue userRegisterQueue() {
		return new Queue("userRegisterQueue", false);
	}
	
	@Bean
	Binding userRequestDeleteBinding(Queue userRequestDelete, DirectExchange exchange) {
		return BindingBuilder.bind(userRequestDelete).to(exchange).with("userDelete");
	}

	@Bean
	Binding userHotelBinding(Queue userHotelQueue, DirectExchange exchange) {
		return BindingBuilder.bind(userHotelQueue).to(exchange).with("userHotel");
	}

	@Bean
	Binding userHotelPostBinding(Queue userHotelPostQueue, DirectExchange exchange) {
		return BindingBuilder.bind(userHotelPostQueue).to(exchange).with("userHotelPost");
	}
	
	@Bean
	Binding userRegisterBinding(Queue userRegisterQueue, DirectExchange exchange) {
		return BindingBuilder.bind(userRegisterQueue).to(exchange).with("userRegister");
	}

	@Bean
	Queue BankReservationQueue() {
		return new Queue("BankReservation", false);
	}

	@Bean
	Binding BankReservationQueueBinding(Queue BankReservationQueue, DirectExchange exchange) {
		return BindingBuilder.bind(BankReservationQueue).to(exchange).with("BankReservationRoute");
	}

	@Bean
	public MessageConverter jsonMessageConverter() {
		return new Jackson2JsonMessageConverter();
	}

	@Bean
	public AmqpTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
		final RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
		rabbitTemplate.setMessageConverter(jsonMessageConverter());
		return rabbitTemplate;
	}

}