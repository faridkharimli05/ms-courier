package az.delivery.mscourier.mapper;

import az.delivery.mscourier.dto.CourierRequestDto;
import az.delivery.mscourier.dto.CourierResponseDto;
import az.delivery.mscourier.entity.Courier;
import org.springframework.stereotype.Component;

import static az.delivery.mscourier.enums.CourierStatus.FREE;

@Component
public class CourierMapper {

    public Courier toEntity(CourierRequestDto request) {
        return Courier.builder()
                .name(request.getName())
                .phone(request.getPhone())
                .status(FREE)
                .build();
    }

    public CourierResponseDto toResponseDto(Courier courier) {
        return CourierResponseDto.builder()
                .id(courier.getId())
                .name(courier.getName())
                .phone(courier.getPhone())
                .status(courier.getStatus())
                .currentOrderId(courier.getCurrentOrderId())
                .build();
    }
}
