package az.delivery.mscourier.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.ExchangeBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.amqp.autoconfigure.SimpleRabbitListenerContainerFactoryConfigurer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(name = "courier.rabbitmq.enabled", havingValue = "true")
public class RabbitMqConfig {

    public static final String ORDER_EVENTS_EXCHANGE = "order.events.exchange";
    public static final String ORDER_ASSIGNED_QUEUE = "courier.order-assigned.queue";
    public static final String ORDER_DELIVERED_QUEUE = "courier.order-delivered.queue";
    public static final String ORDER_ASSIGNED_DLQ = "courier.order-assigned.dlq";
    public static final String ORDER_DELIVERED_DLQ = "courier.order-delivered.dlq";
    public static final String ORDER_EVENTS_DEAD_LETTER_EXCHANGE = "order.events.dlx";
    public static final String ORDER_ASSIGNED_ROUTING_KEY = "order.assigned";
    public static final String ORDER_DELIVERED_ROUTING_KEY = "order.delivered";
    public static final String ORDER_ASSIGNED_DEAD_LETTER_ROUTING_KEY = "order.assigned.dead-letter";
    public static final String ORDER_DELIVERED_DEAD_LETTER_ROUTING_KEY = "order.delivered.dead-letter";

    @Bean
    public DirectExchange orderEventsExchange() {
        return new DirectExchange(ORDER_EVENTS_EXCHANGE);
    }

    @Bean
    public Queue orderAssignedQueue() {
        return QueueBuilder.durable(ORDER_ASSIGNED_QUEUE)
                .deadLetterExchange(ORDER_EVENTS_DEAD_LETTER_EXCHANGE)
                .deadLetterRoutingKey(ORDER_ASSIGNED_DEAD_LETTER_ROUTING_KEY)
                .build();
    }

    @Bean
    public Queue orderDeliveredQueue() {
        return QueueBuilder.durable(ORDER_DELIVERED_QUEUE)
                .deadLetterExchange(ORDER_EVENTS_DEAD_LETTER_EXCHANGE)
                .deadLetterRoutingKey(ORDER_DELIVERED_DEAD_LETTER_ROUTING_KEY)
                .build();
    }

    @Bean
    public DirectExchange orderEventsDeadLetterExchange() {
        return ExchangeBuilder
                .directExchange(ORDER_EVENTS_DEAD_LETTER_EXCHANGE)
                .durable(true)
                .build();
    }

    @Bean
    public Queue orderAssignedDeadLetterQueue() {
        return QueueBuilder.durable(ORDER_ASSIGNED_DLQ).build();
    }

    @Bean
    public Queue orderDeliveredDeadLetterQueue() {
        return QueueBuilder.durable(ORDER_DELIVERED_DLQ).build();
    }

    @Bean
    public Binding orderAssignedBinding() {
        return BindingBuilder
                .bind(orderAssignedQueue())
                .to(orderEventsExchange())
                .with(ORDER_ASSIGNED_ROUTING_KEY);
    }

    @Bean
    public Binding orderDeliveredBinding() {
        return BindingBuilder
                .bind(orderDeliveredQueue())
                .to(orderEventsExchange())
                .with(ORDER_DELIVERED_ROUTING_KEY);
    }

    @Bean
    public Binding orderAssignedDeadLetterBinding() {
        return BindingBuilder
                .bind(orderAssignedDeadLetterQueue())
                .to(orderEventsDeadLetterExchange())
                .with(ORDER_ASSIGNED_DEAD_LETTER_ROUTING_KEY);
    }

    @Bean
    public Binding orderDeliveredDeadLetterBinding() {
        return BindingBuilder
                .bind(orderDeliveredDeadLetterQueue())
                .to(orderEventsDeadLetterExchange())
                .with(ORDER_DELIVERED_DEAD_LETTER_ROUTING_KEY);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            SimpleRabbitListenerContainerFactoryConfigurer configurer,
            ConnectionFactory connectionFactory,
            MessageConverter messageConverter) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        configurer.configure(factory, connectionFactory);
        factory.setMessageConverter(messageConverter);
        factory.setDefaultRequeueRejected(false);
        return factory;
    }
}
