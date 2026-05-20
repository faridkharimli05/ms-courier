package az.delivery.mscourier.listener;

import az.delivery.mscourier.dto.event.OrderAssignedEvent;
import az.delivery.mscourier.dto.event.OrderDeliveredEvent;
import az.delivery.mscourier.service.CourierService;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class OrderEventListenerTest {

    private final CourierService courierService = mock(CourierService.class);
    private final OrderEventListener listener = new OrderEventListener(courierService);

    @Test
    void handleOrderAssignedMarksCourierBusy() {
        listener.handleOrderAssigned(new OrderAssignedEvent(10L, 1L));

        verify(courierService).assignCourierToOrder(1L, 10L);
    }

    @Test
    void handleOrderDeliveredMarksCourierFree() {
        listener.handleOrderDelivered(new OrderDeliveredEvent(10L, 1L));

        verify(courierService).completeDelivery(1L, 10L);
    }
}
