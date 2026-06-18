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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static az.delivery.mscourier.enums.CourierStatus.BUSY;
import static az.delivery.mscourier.enums.CourierStatus.FREE;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CourierServiceTest {

    @Mock
    private CourierRepository courierRepository;

    @Mock
    private CourierMapper courierMapper;

    private CourierService courierService;

    @BeforeEach
    void setUp() {
        courierService = new CourierService(courierRepository, courierMapper);
    }

    @Test
    void createCourierCreatesFreeCourier() {
        CourierRequestDto request = request("Farid", "+994501112233");
        Courier courierFromMapper = Courier.builder()
                .name(request.name())
                .phone(request.phone())
                .status(FREE)
                .build();
        Courier savedCourier = Courier.builder()
                .id(1L)
                .name(request.name())
                .phone(request.phone())
                .status(FREE)
                .build();
        CourierResponseDto expectedResponse = new CourierResponseDto(1L, "Farid", "+994501112233", FREE, null, List.of());

        when(courierRepository.existsByPhone(request.phone())).thenReturn(false);
        when(courierMapper.toCourier(request)).thenReturn(courierFromMapper);
        when(courierRepository.save(any(Courier.class))).thenReturn(savedCourier);
        when(courierMapper.toResponseDto(savedCourier)).thenReturn(expectedResponse);

        CourierResponseDto response = courierService.createCourier(request);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.status()).isEqualTo(FREE);
        assertThat(response.orderHistory()).isEmpty();
    }

    @Test
    void createCourierThrowsWhenPhoneExists() {
        CourierRequestDto request = request("Farid", "+994501112233");
        when(courierRepository.existsByPhone(request.phone())).thenReturn(true);

        assertThatThrownBy(() -> courierService.createCourier(request))
                .isInstanceOf(CourierAlreadyExistsException.class)
                .hasMessageContaining(request.phone());

        verify(courierRepository, never()).save(any(Courier.class));
    }

    @Test
    void getCouriersReturnsAllCouriers() {
        Courier c1 = courier(1L, "Farid", "+994501112233", FREE);
        Courier c2 = courier(2L, "Ali", "+994501112244", BUSY);
        CourierResponseDto response1 = new CourierResponseDto(1L, "Farid", "+994501112233", FREE, null, List.of());
        CourierResponseDto response2 = new CourierResponseDto(2L, "Ali", "+994501112244", BUSY, null, List.of());

        when(courierRepository.findAll()).thenReturn(List.of(c1, c2));
        when(courierMapper.toResponseDto(c1)).thenReturn(response1);
        when(courierMapper.toResponseDto(c2)).thenReturn(response2);

        List<CourierResponseDto> response = courierService.getCouriers();

        assertThat(response).hasSize(2);
        assertThat(response).extracting(CourierResponseDto::status)
                .containsExactly(FREE, BUSY);
    }

    @Test
    void getAvailableCourierReturnsFreeCourier() {
        Courier freeCourier = courier(1L, "Farid", "+994501112233", FREE);
        CourierResponseDto expectedResponse = new CourierResponseDto(1L, "Farid", "+994501112233", FREE, null, List.of());

        when(courierRepository.findFirstByStatus(FREE)).thenReturn(Optional.of(freeCourier));
        when(courierMapper.toResponseDto(freeCourier)).thenReturn(expectedResponse);

        CourierResponseDto response = courierService.getAvailableCourier();

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.status()).isEqualTo(FREE);
    }

    @Test
    void getAvailableCourierThrowsWhenNoFreeCourierExists() {
        when(courierRepository.findFirstByStatus(FREE)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> courierService.getAvailableCourier())
                .isInstanceOf(CourierNotFoundException.class);
    }

    @Test
    void handleOrderAssignedMarksCourierBusy() {
        Courier freeCourier = courier(1L, "Farid", "+994501112233", FREE);
        when(courierRepository.findById(1L)).thenReturn(Optional.of(freeCourier));
        when(courierRepository.save(freeCourier)).thenReturn(freeCourier);

        courierService.handleOrderAssigned(1L, 10L);

        assertThat(freeCourier.getStatus()).isEqualTo(BUSY);
        assertThat(freeCourier.getCurrentOrderId()).isEqualTo(10L);
        assertThat(freeCourier.getOrderHistory()).containsExactly(10L);
        verify(courierRepository).save(freeCourier);
    }

    @Test
    void handleOrderAssignedDoesNotDuplicateHistoryForSameOrder() {
        Courier busyCourier = courier(1L, "Farid", "+994501112233", BUSY);
        busyCourier.setCurrentOrderId(10L);
        busyCourier.getOrderHistory().add(10L);
        when(courierRepository.findById(1L)).thenReturn(Optional.of(busyCourier));
        when(courierRepository.save(busyCourier)).thenReturn(busyCourier);

        courierService.handleOrderAssigned(1L, 10L);

        assertThat(busyCourier.getOrderHistory()).containsExactly(10L);
    }

    @Test
    void handleOrderAssignedThrowsWhenBusyWithAnotherOrder() {
        Courier busyCourier = courier(1L, "Farid", "+994501112233", BUSY);
        busyCourier.setCurrentOrderId(9L);
        busyCourier.getOrderHistory().add(9L);
        when(courierRepository.findById(1L)).thenReturn(Optional.of(busyCourier));

        assertThatThrownBy(() -> courierService.handleOrderAssigned(1L, 10L))
                .isInstanceOf(CourierAssignmentException.class);

        verify(courierRepository, never()).save(any(Courier.class));
    }

    @Test
    void handleOrderDeliveredClearsCurrentOrderAndFreesCourier() {
        Courier courier = courier(1L, "Farid", "+994501112233", BUSY);
        courier.setCurrentOrderId(10L);
        courier.getOrderHistory().add(10L);
        when(courierRepository.findById(1L)).thenReturn(Optional.of(courier));
        when(courierRepository.save(courier)).thenReturn(courier);

        courierService.handleOrderDelivered(1L, 10L);

        assertThat(courier.getStatus()).isEqualTo(FREE);
        assertThat(courier.getCurrentOrderId()).isNull();
        assertThat(courier.getOrderHistory()).containsExactly(10L);
        verify(courierRepository).save(courier);
    }

    @Test
    void handleOrderDeliveredThrowsWhenOrderDoesNotMatch() {
        Courier courier = courier(1L, "Farid", "+994501112233", BUSY);
        courier.setCurrentOrderId(10L);
        courier.getOrderHistory().add(10L);
        when(courierRepository.findById(1L)).thenReturn(Optional.of(courier));

        assertThatThrownBy(() -> courierService.handleOrderDelivered(1L, 11L))
                .isInstanceOf(CourierAssignmentException.class);

        verify(courierRepository, never()).save(any(Courier.class));
    }

    @Test
    void handleOrderDeliveredThrowsWhenCourierHasNoCurrentOrder() {
        Courier courier = courier(1L, "Farid", "+994501112233", FREE);
        when(courierRepository.findById(1L)).thenReturn(Optional.of(courier));

        assertThatThrownBy(() -> courierService.handleOrderDelivered(1L, 10L))
                .isInstanceOf(CourierAssignmentException.class);

        verify(courierRepository, never()).save(any(Courier.class));
    }

    @Test
    void updateCourierChangesNameAndPhone() {
        Courier courier = courier(1L, "Farid", "+994501112233", FREE);
        Courier updatedCourier = courier(1L, "Ali", "+994501112244", FREE);
        CourierRequestDto request = request("Ali", "+994501112244");
        CourierResponseDto expectedResponse = new CourierResponseDto(1L, "Ali", "+994501112244", FREE, null, List.of());

        when(courierRepository.findById(1L)).thenReturn(Optional.of(courier));
        when(courierRepository.existsByPhoneAndIdNot(request.phone(), 1L)).thenReturn(false);
        when(courierMapper.updateCourierFromRequest(courier, request)).thenReturn(updatedCourier);
        when(courierRepository.save(updatedCourier)).thenReturn(updatedCourier);
        when(courierMapper.toResponseDto(updatedCourier)).thenReturn(expectedResponse);

        CourierResponseDto response = courierService.updateCourier(1L, request);

        assertThat(response.name()).isEqualTo("Ali");
        assertThat(response.phone()).isEqualTo("+994501112244");
    }

    @Test
    void deleteCourierThrowsWhenCourierIsBusy() {
        Courier courier = courier(1L, "Farid", "+994501112233", BUSY);
        when(courierRepository.findById(1L)).thenReturn(Optional.of(courier));

        assertThatThrownBy(() -> courierService.deleteCourier(1L))
                .isInstanceOf(CourierAssignmentException.class);
    }

    @Test
    void deleteCourierDeletesFreeCourier() {
        Courier courier = courier(1L, "Farid", "+994501112233", FREE);
        when(courierRepository.findById(1L)).thenReturn(Optional.of(courier));

        courierService.deleteCourier(1L);

        verify(courierRepository).delete(courier);
    }

    @Test
    void getCourierOrderHistoryReturnsRecordedOrders() {
        Courier courier = courier(1L, "Farid", "+994501112233", FREE);
        courier.getOrderHistory().addAll(List.of(10L, 15L, 18L));
        when(courierRepository.findById(1L)).thenReturn(Optional.of(courier));

        List<Long> response = courierService.getCourierOrderHistory(1L);

        assertThat(response).containsExactly(10L, 15L, 18L);
    }

    private Courier courier(Long id, String name, String phone, CourierStatus status) {
        return Courier.builder()
                .id(id)
                .name(name)
                .phone(phone)
                .status(status)
                .build();
    }

    private CourierRequestDto request(String name, String phone) {
        return new CourierRequestDto(name, phone);
    }
}
