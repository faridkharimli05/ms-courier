package az.delivery.mscourier.repository;

import az.delivery.mscourier.entity.Courier;
import az.delivery.mscourier.enums.CourierStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CourierRepository extends JpaRepository<Courier, Long> {
    Optional<Courier> findFirstByStatus(CourierStatus status);
}
