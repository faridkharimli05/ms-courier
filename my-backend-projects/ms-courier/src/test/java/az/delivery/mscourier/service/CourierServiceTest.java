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

    private CourierService courierService;

    @BeforeEach
    void setUp() {
        courierService = new CourierService(courierRepository, new CourierMapper());
    }

    @Test
    void createCourierCreatesFreeCourier() {
        CourierRequestDto request = new CourierRequestDto("Farid", "+994501112233");
        Courier savedCourier = Courier.builder()
                .id(1L)
                .name(request.getName())
                .phone(request.getPhone())
                .status(FREE)
                .build();

        when(courierRepository.existsByPhone(request.getPhone())).thenReturn(false);
        when(courierRepository.save(any(Courier.class))).thenReturn(savedCourier);

        CourierResponseDto response = courierService.createCourier(request);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getStatus()).isEqualTo(FREE);
    }

    @Test
    void createCourierThrowsWhenPhoneExists() {
        CourierRequestDto request = new CourierRequestDto("Farid", "+994501112233");
        when(courierRepository.existsByPhone(request.getPhone())).thenReturn(true);

        assertThatThrownBy(() -> courierService.createCourier(request))
                .isInstanceOf(CourierAlreadyExistsException.class)
                .hasMessageContaining(request.getPhone());

        verify(courierRepository, never()).save(any(Courier.class));
    }

    @Test
    void getCouriersReturnsAllCouriers() {
        when(courierRepository.findAll()).thenReturn(List.of(
                courier(1L, "Farid", "+994501112233", FREE),
                courier(2L, "Ali", "+994501112244", BUSY)
        ));

        List<CourierResponseDto> response = courierService.getCouriers();

        assertThat(response).hasSize(2);
        assertThat(response).extracting(CourierResponseDto::getStatus)
                .containsExactly(FREE, BUSY);
    }

    @Test
    void assignAvailableCourierMarksCourierBusy() {
        Courier freeCourier = courier(1L, "Farid", "+994501112233", FREE);

        when(courierRepository.findFirstByStatusOrderByIdAsc(FREE))
                .thenReturn(Optional.of(freeCourier));
        when(courierRepository.save(freeCourier)).thenReturn(freeCourier);

        CourierResponseDto response = courierService.assignAvailableCourier(10L);

        assertThat(response.getStatus()).isEqualTo(BUSY);
        assertThat(response.getCurrentOrderId()).isEqualTo(10L);
        assertThat(freeCourier.getStatus()).isEqualTo(BUSY);
        assertThat(freeCourier.getCurrentOrderId()).isEqualTo(10L);
    }

    @Test
    void assignAvailableCourierThrowsWhenNoFreeCourierExists() {
        when(courierRepository.findFirstByStatusOrderByIdAsc(FREE))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> courierService.assignAvailableCourier(10L))
                .isInstanceOf(CourierNotFoundException.class);
    }

    @Test
    void assignCourierToOrderStoresCurrentOrder() {
        Courier freeCourier = courier(1L, "Farid", "+994501112233", FREE);
        when(courierRepository.findById(1L)).thenReturn(Optional.of(freeCourier));
        when(courierRepository.save(freeCourier)).thenReturn(freeCourier);

        CourierResponseDto response = courierService.assignCourierToOrder(1L, 10L);

        assertThat(response.getStatus()).isEqualTo(BUSY);
        assertThat(response.getCurrentOrderId()).isEqualTo(10L);
    }

    @Test
    void assignCourierToOrderThrowsWhenBusyWithAnotherOrder() {
        Courier busyCourier = courier(1L, "Farid", "+994501112233", BUSY);
        busyCourier.setCurrentOrderId(9L);
        when(courierRepository.findById(1L)).thenReturn(Optional.of(busyCourier));

        assertThatThrownBy(() -> courierService.assignCourierToOrder(1L, 10L))
                .isInstanceOf(CourierAssignmentException.class);

        verify(courierRepository, never()).save(any(Courier.class));
    }

    @Test
    void markFreeChangesBusyCourierToFree() {
        Courier courier = courier(1L, "Farid", "+994501112233", BUSY);
        courier.setCurrentOrderId(10L);
        when(courierRepository.findById(1L)).thenReturn(Optional.of(courier));

        courierService.markFree(1L);

        assertThat(courier.getStatus()).isEqualTo(FREE);
        assertThat(courier.getCurrentOrderId()).isNull();
        verify(courierRepository).save(courier);
    }

    @Test
    void completeDeliveryClearsCurrentOrderAndFreesCourier() {
        Courier courier = courier(1L, "Farid", "+994501112233", BUSY);
        courier.setCurrentOrderId(10L);
        when(courierRepository.findById(1L)).thenReturn(Optional.of(courier));

        courierService.completeDelivery(1L, 10L);

        assertThat(courier.getStatus()).isEqualTo(FREE);
        assertThat(courier.getCurrentOrderId()).isNull();
        verify(courierRepository).save(courier);
    }

    @Test
    void completeDeliveryThrowsWhenOrderDoesNotMatch() {
        Courier courier = courier(1L, "Farid", "+994501112233", BUSY);
        courier.setCurrentOrderId(10L);
        when(courierRepository.findById(1L)).thenReturn(Optional.of(courier));

        assertThatThrownBy(() -> courierService.completeDelivery(1L, 11L))
                .isInstanceOf(CourierAssignmentException.class);

        verify(courierRepository, never()).save(any(Courier.class));
    }

    @Test
    void completeDeliveryThrowsWhenCourierHasNoCurrentOrder() {
        Courier courier = courier(1L, "Farid", "+994501112233", FREE);
        when(courierRepository.findById(1L)).thenReturn(Optional.of(courier));

        assertThatThrownBy(() -> courierService.completeDelivery(1L, 10L))
                .isInstanceOf(CourierAssignmentException.class);

        verify(courierRepository, never()).save(any(Courier.class));
    }

    @Test
    void updateCourierChangesNameAndPhone() {
        Courier courier = courier(1L, "Farid", "+994501112233", FREE);
        CourierUpdateRequestDto request = new CourierUpdateRequestDto("Ali", "+994501112244");
        when(courierRepository.findById(1L)).thenReturn(Optional.of(courier));
        when(courierRepository.existsByPhoneAndIdNot(request.getPhone(), 1L)).thenReturn(false);
        when(courierRepository.save(courier)).thenReturn(courier);

        CourierResponseDto response = courierService.updateCourier(1L, request);

        assertThat(response.getName()).isEqualTo("Ali");
        assertThat(response.getPhone()).isEqualTo("+994501112244");
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

    private Courier courier(Long id, String name, String phone, CourierStatus status) {
        return Courier.builder()
                .id(id)
                .name(name)
                .phone(phone)
                .status(status)
                .build();
    }
}
