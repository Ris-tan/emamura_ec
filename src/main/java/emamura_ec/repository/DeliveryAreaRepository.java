package emamura_ec.repository;

import java.util.Optional;

import emamura_ec.entity.DeliveryArea;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeliveryAreaRepository extends JpaRepository<DeliveryArea, Long> {

    Optional<DeliveryArea> findByPrefecture(String prefecture);
}
