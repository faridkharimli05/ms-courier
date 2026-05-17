package az.delivery.mscourier.dto;

import az.delivery.mscourier.enums.CourierStatus;
import lombok.*;


@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourierResponseDto {

    private Long id;
    private String name;
    private String phone;
    private CourierStatus status;
}
