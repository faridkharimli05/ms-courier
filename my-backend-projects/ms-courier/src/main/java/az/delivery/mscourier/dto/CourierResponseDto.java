package az.delivery.mscourier.dto;

import az.delivery.mscourier.enums.CourierStatus;

import java.util.List;

public record CourierResponseDto(
        Long id,
        String name,
        String phone,
        CourierStatus status,
        Long currentOrderId,
        List<Long> orderHistory
) {
    public CourierResponseDto {
        orderHistory = List.copyOf(orderHistory);
    }
}
