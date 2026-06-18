package az.delivery.mscourier.dto.event;

public record OrderDeliveredEvent(Long orderId, Long courierId) {
}
