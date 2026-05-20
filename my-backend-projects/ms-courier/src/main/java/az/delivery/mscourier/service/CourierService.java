package az.delivery.mscourier.service;

import az.delivery.mscourier.dto.CourierRequestDto;
import az.delivery.mscourier.dto.CourierResponseDto;
import az.delivery.mscourier.dto.CourierUpdateRequestDto;
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

import static az.delivery.mscourier.enums.CourierStatus.BUSY;
import static az.delivery.mscourier.enums.CourierStatus.FREE;

@Service
@RequiredArgsConstructor
public class CourierService {

    private final CourierRepository courierRepository;
    private final CourierMapper courierMapper;

    @Transactional
    public CourierResponseDto createCourier(CourierRequestDto request) {
        validatePhoneIsUnique(request.getPhone());
        Courier courier = courierMapper.toEntity(request);
        Courier savedCourier = save(courier);
        return toResponse(savedCourier);
    }

    @Transactional(readOnly = true)
    public CourierResponseDto getAvailableCourier() {
        Courier courier = findAvailableCourier();
        return toResponse(courier);
    }

    @Transactional(readOnly = true)
    public List<CourierResponseDto> getCouriers() {
        return courierRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public CourierResponseDto assignAvailableCourier(Long orderId) {
        validateOrderId(orderId);
        Courier courier = findFirstAvailableCourierForAssignment();
        courier.setStatus(BUSY);
        courier.setCurrentOrderId(orderId);
        Courier assignedCourier = save(courier);
        return toResponse(assignedCourier);
    }

    @Transactional
    public CourierResponseDto assignCourierToOrder(Long courierId, Long orderId) {
        validateOrderId(orderId);
        Courier courier = findById(courierId);
        if (courier.getStatus() == BUSY && !orderId.equals(courier.getCurrentOrderId())) {
            throw new CourierAssignmentException(
                    "Courier is already assigned to another order: " + courier.getCurrentOrderId());
        }
        courier.setStatus(BUSY);
        courier.setCurrentOrderId(orderId);
        return toResponse(save(courier));
    }

    @Transactional
    public void markBusy(Long courierId) {
        updateStatus(courierId, BUSY);
    }

    @Transactional
    public void markFree(Long courierId) {
        updateStatus(courierId, FREE);
    }

    @Transactional
    public void completeDelivery(Long courierId, Long orderId) {
        validateOrderId(orderId);
        Courier courier = findById(courierId);
        if (courier.getCurrentOrderId() == null) {
            throw new CourierAssignmentException("Courier is not assigned to any order");
        }
        if (!courier.getCurrentOrderId().equals(orderId)) {
            throw new CourierAssignmentException(
                    "Courier is assigned to order " + courier.getCurrentOrderId()
                            + ", not order " + orderId);
        }
        courier.setStatus(FREE);
        courier.setCurrentOrderId(null);
        save(courier);
    }

    @Transactional(readOnly = true)
    public CourierResponseDto getCourierById(Long id) {
        Courier courier = findById(id);
        return toResponse(courier);
    }

    @Transactional
    public CourierResponseDto updateCourier(Long id, CourierUpdateRequestDto request) {
        Courier courier = findById(id);
        validatePhoneIsUniqueForCourier(request.getPhone(), id);
        courier.setName(request.getName());
        courier.setPhone(request.getPhone());
        return toResponse(save(courier));
    }

    @Transactional
    public void deleteCourier(Long id) {
        Courier courier = findById(id);
        if (courier.getStatus() == BUSY) {
            throw new CourierAssignmentException("Busy courier cannot be deleted");
        }
        courierRepository.delete(courier);
    }

    private void updateStatus(Long courierId, CourierStatus status) {
        Courier courier = findById(courierId);
        if (courier.getStatus() == status) {
            return;
        }
        courier.setStatus(status);
        if (status == FREE) {
            courier.setCurrentOrderId(null);
        }
        save(courier);
    }

    private void validatePhoneIsUnique(String phone) {
        if (courierRepository.existsByPhone(phone)) {
            throw new CourierAlreadyExistsException(
                    "Courier already exists with phone: " + phone);
        }
    }

    private void validatePhoneIsUniqueForCourier(String phone, Long courierId) {
        if (courierRepository.existsByPhoneAndIdNot(phone, courierId)) {
            throw new CourierAlreadyExistsException(
                    "Courier already exists with phone: " + phone);
        }
    }

    private void validateOrderId(Long orderId) {
        if (orderId == null) {
            throw new CourierAssignmentException("Order id cannot be null");
        }
    }

    private Courier findAvailableCourier() {
        return courierRepository
                .findFirstByStatus(FREE)
                .orElseThrow(() -> new CourierNotFoundException(
                        "No available courier found"));
    }

    private Courier findFirstAvailableCourierForAssignment() {
        return courierRepository
                .findFirstByStatusOrderByIdAsc(FREE)
                .orElseThrow(() -> new CourierNotFoundException(
                        "No available courier found"));
    }

    private Courier findById(Long courierId) {
        return courierRepository.findById(courierId)
                .orElseThrow(() -> new CourierNotFoundException(
                        "Courier not found with id: " + courierId));
    }

    private Courier save(Courier courier) {
        return courierRepository.save(courier);
    }

    private CourierResponseDto toResponse(Courier courier) {
        return courierMapper.toResponseDto(courier);
    }

}
