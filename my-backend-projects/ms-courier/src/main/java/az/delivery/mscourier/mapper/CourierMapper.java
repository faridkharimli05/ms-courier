package az.delivery.mscourier.mapper;

import az.delivery.mscourier.dto.CourierRequestDto;
import az.delivery.mscourier.dto.CourierResponseDto;
import az.delivery.mscourier.entity.Courier;
import az.delivery.mscourier.enums.CourierStatus;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CourierMapper {

    public Courier toCourier(CourierRequestDto request) {
        return Courier.builder()
                .name(request.name())
                .phone(request.phone())
                .status(CourierStatus.FREE)
                .build();
    }

    public CourierResponseDto toResponseDto(Courier courier) {
        return new CourierResponseDto(
                courier.getId(),
                courier.getName(),
                courier.getPhone(),
                courier.getStatus(),
                courier.getCurrentOrderId(),
                List.copyOf(courier.getOrderHistory())
        );
    }

    public Courier updateCourierFromRequest(Courier courier, CourierRequestDto request) {
        return Courier.builder()
                .id(courier.getId())
                .name(request.name())
                .phone(request.phone())
                .status(courier.getStatus())
                .currentOrderId(courier.getCurrentOrderId())
                .orderHistory(List.copyOf(courier.getOrderHistory()))
                .build();
    }
}

