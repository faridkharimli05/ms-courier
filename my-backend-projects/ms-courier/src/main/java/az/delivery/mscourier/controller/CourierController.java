package az.delivery.mscourier.controller;

import az.delivery.mscourier.dto.CourierAssignmentRequestDto;
import az.delivery.mscourier.dto.CourierRequestDto;
import az.delivery.mscourier.dto.CourierResponseDto;
import az.delivery.mscourier.dto.CourierUpdateRequestDto;
import az.delivery.mscourier.service.CourierService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;

@RestController
@RequestMapping("/api/couriers")
@RequiredArgsConstructor
public class CourierController {

    private final CourierService courierService;

    @PostMapping
    @ResponseStatus(CREATED)
    public CourierResponseDto createCourier(
            @Valid @RequestBody CourierRequestDto request) {
        return courierService.createCourier(request);
    }

    @GetMapping
    public List<CourierResponseDto> getCouriers() {
        return courierService.getCouriers();
    }

    @GetMapping("/available")
    public CourierResponseDto getAvailableCourier() {
        return courierService.getAvailableCourier();
    }

    @PostMapping("/assign")
    @ResponseStatus(OK)
    public CourierResponseDto assignAvailableCourier(
            @Valid @RequestBody CourierAssignmentRequestDto request) {
        return courierService.assignAvailableCourier(request.getOrderId());
    }

    @GetMapping("/{id}")
    public CourierResponseDto getCourierById(@PathVariable Long id) {
        return courierService.getCourierById(id);
    }

    @PutMapping("/{id}")
    public CourierResponseDto updateCourier(
            @PathVariable Long id,
            @Valid @RequestBody CourierUpdateRequestDto request) {
        return courierService.updateCourier(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(NO_CONTENT)
    public void deleteCourier(@PathVariable Long id) {
        courierService.deleteCourier(id);
    }

    @PatchMapping("/{id}/busy")
    @ResponseStatus(NO_CONTENT)
    public void markBusy(
            @PathVariable Long id,
            @Valid @RequestBody CourierAssignmentRequestDto request) {
        courierService.assignCourierToOrder(id, request.getOrderId());
    }

    @PatchMapping("/{id}/free")
    @ResponseStatus(NO_CONTENT)
    public void markFree(@PathVariable Long id) {
        courierService.markFree(id);
    }
}
