package az.delivery.mscourier.controller;

import az.delivery.mscourier.dto.CourierRequestDto;
import az.delivery.mscourier.dto.CourierResponseDto;
import az.delivery.mscourier.service.CourierService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/couriers")
@RequiredArgsConstructor
public class CourierController {

    private final CourierService courierService;

    @PostMapping
    public ResponseEntity<CourierResponseDto> createCourier(
            @Valid @RequestBody CourierRequestDto request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(courierService.createCourier(request));
    }

    @GetMapping("/available")
    public ResponseEntity<CourierResponseDto> getAvailableCourier() {
        return ResponseEntity.ok(courierService.getAvailableCourier());
    }
}
