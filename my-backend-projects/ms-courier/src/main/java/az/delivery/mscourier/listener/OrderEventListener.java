package az.delivery.mscourier.listener;

import az.delivery.mscourier.config.RabbitMqConfig;
import az.delivery.mscourier.dto.event.OrderAssignedEvent;
import az.delivery.mscourier.dto.event.OrderDeliveredEvent;
import az.delivery.mscourier.service.CourierService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "courier.rabbitmq.enabled", havingValue = "true")
public class OrderEventListener {

    private final CourierService courierService;

    @RabbitListener(queues = RabbitMqConfig.ORDER_ASSIGNED_QUEUE)
    public void handleOrderAssigned(OrderAssignedEvent event) {
        log.info("Order assigned event received: orderId={}, courierId={}",
                event.getOrderId(), event.getCourierId());
        courierService.assignCourierToOrder(event.getCourierId(), event.getOrderId());
    }

    @RabbitListener(queues = RabbitMqConfig.ORDER_DELIVERED_QUEUE)
    public void handleOrderDelivered(OrderDeliveredEvent event) {
        log.info("Order delivered event received: orderId={}, courierId={}",
                event.getOrderId(), event.getCourierId());
        courierService.completeDelivery(event.getCourierId(), event.getOrderId());
    }
}
