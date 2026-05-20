package az.delivery.mscourier.repository;

import az.delivery.mscourier.entity.Courier;
import az.delivery.mscourier.enums.CourierStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import java.util.Optional;
import static jakarta.persistence.LockModeType.PESSIMISTIC_WRITE;

public interface CourierRepository extends JpaRepository<Courier, Long> {
    Optional<Courier> findFirstByStatus(CourierStatus status);

    @Lock(PESSIMISTIC_WRITE)
    Optional<Courier> findFirstByStatusOrderByIdAsc(CourierStatus status);

    boolean existsByPhone(String phone);

    boolean existsByPhoneAndIdNot(String phone, Long id);
}
