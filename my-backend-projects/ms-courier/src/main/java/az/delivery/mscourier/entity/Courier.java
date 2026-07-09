package az.delivery.mscourier.entity;

import az.delivery.mscourier.enums.CourierStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "couriers")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class Courier {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CourierStatus status;

    @Column(name = "current_order_id")
    private Long currentOrderId;

    @ElementCollection
    @CollectionTable(
            name = "courier_order_history",
            joinColumns = @JoinColumn(name = "courier_id"))
    @OrderColumn(name = "history_index")
    @Column(name = "order_id", nullable = false)
    @Builder.Default
    private List<Long> orderHistory = new ArrayList<>();
}
