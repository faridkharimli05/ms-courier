package az.delivery.mscourier.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record CourierRequestDto(
        @NotBlank(message = "Name cannot be blank")
        String name,
        @NotBlank(message = "Phone cannot be blank")
        @Pattern(regexp = "^\\+994\\d{9}$", message = "Phone must be in +994XXXXXXXXX format")
        String phone
) {
}
