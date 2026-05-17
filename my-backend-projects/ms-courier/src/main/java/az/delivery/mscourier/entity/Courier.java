package az.delivery.mscourier.entity;


import az.delivery.mscourier.enums.CourierStatus;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "couriers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Courier {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String phone;

    @Enumerated(EnumType.STRING)
    private CourierStatus status;



}
