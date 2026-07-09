package az.delivery.mscourier.controller;

import az.delivery.mscourier.dto.CourierRequestDto;
import az.delivery.mscourier.dto.CourierResponseDto;
import az.delivery.mscourier.service.CourierService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NO_CONTENT;

@RestController
@RequestMapping("/api/couriers")
@RequiredArgsConstructor
public class CourierController {

    private final CourierService courierService;

    @PostMapping
    @ResponseStatus(CREATED)
    public CourierResponseDto createCourier(@Valid @RequestBody CourierRequestDto request) {
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

    @GetMapping("/{id}")
    public CourierResponseDto getCourierById(@PathVariable Long id) {
        return courierService.getCourierById(id);
    }

    @GetMapping("/{id}/history")
    public List<Long> getCourierOrderHistory(@PathVariable Long id) {
        return courierService.getCourierOrderHistory(id);
    }

    @PutMapping("/{id}")
    public CourierResponseDto updateCourier(@PathVariable Long id,
                                            @Valid @RequestBody CourierRequestDto request) {
        return courierService.updateCourier(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(NO_CONTENT)
    public void deleteCourier(@PathVariable Long id) {
        courierService.deleteCourier(id);
    }
}
