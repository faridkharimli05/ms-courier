package az.delivery.mscourier.dto.event;

public record OrderAssignedEvent(Long orderId, Long courierId) {
}
