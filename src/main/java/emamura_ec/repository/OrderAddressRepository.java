package emamura_ec.repository;

import java.util.Optional;

import emamura_ec.entity.OrderAddress;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderAddressRepository extends JpaRepository<OrderAddress, Long> {

    Optional<OrderAddress> findByOrder_OrderId(Long orderId);
}
