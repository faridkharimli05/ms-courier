package az.delivery.mscourier.service;

import az.delivery.mscourier.dto.CourierRequestDto;
import az.delivery.mscourier.dto.CourierResponseDto;
import az.delivery.mscourier.entity.Courier;
import az.delivery.mscourier.enums.CourierStatus;
import az.delivery.mscourier.exception.CourierAlreadyExistsException;
import az.delivery.mscourier.exception.CourierAssignmentException;
import az.delivery.mscourier.exception.CourierNotFoundException;
import az.delivery.mscourier.mapper.CourierMapper;
import az.delivery.mscourier.repository.CourierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

import static az.delivery.mscourier.enums.CourierStatus.BUSY;
import static az.delivery.mscourier.enums.CourierStatus.FREE;

@Service
@RequiredArgsConstructor
public class CourierService {

    private final CourierRepository courierRepository;
    private final CourierMapper courierMapper;

    @Transactional
    public CourierResponseDto createCourier(CourierRequestDto request) {
        validatePhone(request.phone(), null);

        Courier courier = courierMapper.toCourier(request);
        Courier saved = courierRepository.save(courier);

        return courierMapper.toResponseDto(saved);
    }

    public List<CourierResponseDto> getCouriers() {
        return courierRepository.findAll()
                .stream()
                .map(courierMapper::toResponseDto)
                .toList();
    }

    public CourierResponseDto getAvailableCourier() {
        Courier courier = courierRepository.findFirstByStatus(FREE)
                .orElseThrow(() ->
                        new CourierNotFoundException("No available courier found"));

        return courierMapper.toResponseDto(courier);
    }

    public CourierResponseDto getCourierById(Long id) {
        return courierMapper.toResponseDto(findCourier(id));
    }

    public List<Long> getCourierOrderHistory(Long courierId) {
        return List.copyOf(findCourier(courierId).getOrderHistory());
    }

    @Transactional
    public CourierResponseDto updateCourier(Long id, CourierRequestDto request) {
        Courier courier = findCourier(id);

        validatePhone(request.phone(), id);

        Courier updated = courierMapper.updateCourierFromRequest(courier, request);
        Courier saved = courierRepository.save(updated);

        return courierMapper.toResponseDto(saved);
    }

    @Transactional
    public void deleteCourier(Long id) {
        Courier courier = findCourier(id);

        if (courier.getStatus() == BUSY) {
            throw new CourierAssignmentException("Busy courier cannot be deleted");
        }

        courierRepository.delete(courier);
    }

    @Transactional
    public void handleOrderAssigned(Long courierId, Long orderId) {
        validateOrderId(orderId);

        Courier courier = findCourier(courierId);

        if (courier.getStatus() == BUSY &&
                !Objects.equals(courier.getCurrentOrderId(), orderId)) {
            throw new CourierAssignmentException(
                    "Courier is already assigned to another order: " + courier.getCurrentOrderId());
        }

        courier.setStatus(BUSY);
        courier.setCurrentOrderId(orderId);

        addToOrderHistory(courier, orderId);

        courierRepository.save(courier);
    }

    @Transactional
    public void handleOrderDelivered(Long courierId, Long orderId) {
        validateOrderId(orderId);

        Courier courier = findCourier(courierId);

        if (courier.getCurrentOrderId() == null) {
            throw new CourierAssignmentException("Courier is not assigned to any order");
        }

        if (!Objects.equals(courier.getCurrentOrderId(), orderId)) {
            throw new CourierAssignmentException(
                    "Courier is assigned to order " + courier.getCurrentOrderId()
                            + ", not order " + orderId);
        }

        addToOrderHistory(courier, orderId);

        courier.setStatus(FREE);
        courier.setCurrentOrderId(null);

        courierRepository.save(courier);
    }

    private void addToOrderHistory(Courier courier, Long orderId) {
        if (!courier.getOrderHistory().contains(orderId)) {
            courier.getOrderHistory().add(orderId);
        }
    }

    private void validatePhone(String phone, Long courierId) {
        boolean exists = courierId == null
                ? courierRepository.existsByPhone(phone)
                : courierRepository.existsByPhoneAndIdNot(phone, courierId);

        if (exists) {
            throw new CourierAlreadyExistsException(
                    "Courier already exists with phone: " + phone);
        }
    }

    private void validateOrderId(Long orderId) {
        if (orderId == null) {
            throw new CourierAssignmentException("Order id cannot be null");
        }
    }

    private Courier findCourier(Long courierId) {
        return courierRepository.findById(courierId)
                .orElseThrow(() ->
                        new CourierNotFoundException(
                                "Courier not found with id: " + courierId));
    }
}