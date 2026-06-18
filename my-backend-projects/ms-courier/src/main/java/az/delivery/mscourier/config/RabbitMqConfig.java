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
        return exchange(ORDER_EVENTS_EXCHANGE);
    }

    @Bean
    public Queue orderAssignedQueue() {
        return queue(ORDER_ASSIGNED_QUEUE, ORDER_ASSIGNED_DEAD_LETTER_ROUTING_KEY);
    }

    @Bean
    public Queue orderDeliveredQueue() {
        return queue(ORDER_DELIVERED_QUEUE, ORDER_DELIVERED_DEAD_LETTER_ROUTING_KEY);
    }

    @Bean
    public DirectExchange orderEventsDeadLetterExchange() {
        return exchange(ORDER_EVENTS_DEAD_LETTER_EXCHANGE);
    }

    @Bean
    public Queue orderAssignedDeadLetterQueue() {
        return queue(ORDER_ASSIGNED_DLQ);
    }

    @Bean
    public Queue orderDeliveredDeadLetterQueue() {
        return queue(ORDER_DELIVERED_DLQ);
    }

    @Bean
    public Binding orderAssignedBinding() {
        return bind(orderAssignedQueue(), orderEventsExchange(), ORDER_ASSIGNED_ROUTING_KEY);
    }

    @Bean
    public Binding orderDeliveredBinding() {
        return bind(orderDeliveredQueue(), orderEventsExchange(), ORDER_DELIVERED_ROUTING_KEY);
    }

    @Bean
    public Binding orderAssignedDeadLetterBinding() {
        return bind(orderAssignedDeadLetterQueue(), orderEventsDeadLetterExchange(),
                ORDER_ASSIGNED_DEAD_LETTER_ROUTING_KEY);
    }

    @Bean
    public Binding orderDeliveredDeadLetterBinding() {
        return bind(orderDeliveredDeadLetterQueue(), orderEventsDeadLetterExchange(),
                ORDER_DELIVERED_DEAD_LETTER_ROUTING_KEY);
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
        var factory = new SimpleRabbitListenerContainerFactory();
        configurer.configure(factory, connectionFactory);
        factory.setMessageConverter(messageConverter);
        factory.setDefaultRequeueRejected(false);
        return factory;
    }

    private static DirectExchange exchange(String name) {
        return ExchangeBuilder.directExchange(name).durable(true).build();
    }

    private static Queue queue(String name) {
        return QueueBuilder.durable(name).build();
    }

    private static Queue queue(String name, String deadLetterRoutingKey) {
        return QueueBuilder.durable(name)
                .deadLetterExchange(ORDER_EVENTS_DEAD_LETTER_EXCHANGE)
                .deadLetterRoutingKey(deadLetterRoutingKey)
                .build();
    }

    private static Binding bind(Queue queue, DirectExchange exchange, String routingKey) {
        return BindingBuilder.bind(queue).to(exchange).with(routingKey);
    }
}
