package az.delivery.mscourier.service;

import az.delivery.mscourier.dto.CourierRequestDto;
import az.delivery.mscourier.dto.CourierResponseDto;
import az.delivery.mscourier.entity.Courier;
import az.delivery.mscourier.enums.CourierStatus;
import az.delivery.mscourier.exception.CourierNotFoundException;
import az.delivery.mscourier.repository.CourierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CourierService {

    private final CourierRepository courierRepository;

    public CourierResponseDto createCourier(CourierRequestDto request) {
        Courier courier = Courier.builder()
                .name(request.getName())
                .phone(request.getPhone())
                .status(CourierStatus.FREE)
                .build();

        return toResponse(courierRepository.save(courier));
    }

    public CourierResponseDto getAvailableCourier() {
        Courier courier = courierRepository
                .findFirstByStatus(CourierStatus.FREE)
                .orElseThrow(() -> new CourierNotFoundException(
                        "No available courier found"));

        return toResponse(courier);
    }

    public void markBusy(Long courierId) {
        Courier courier = findById(courierId);
        courier.setStatus(CourierStatus.BUSY);
        courierRepository.save(courier);
    }

    public void markFree(Long courierId) {
        Courier courier = findById(courierId);
        courier.setStatus(CourierStatus.FREE);
        courierRepository.save(courier);
    }

    private Courier findById(Long courierId) {
        return courierRepository.findById(courierId)
                .orElseThrow(() -> new CourierNotFoundException(
                        "Courier not found with id: " + courierId));
    }

    private CourierResponseDto toResponse(Courier courier) {
        return CourierResponseDto.builder()
                .id(courier.getId())
                .name(courier.getName())
                .phone(courier.getPhone())
                .status(courier.getStatus())
                .build();
    }
}
