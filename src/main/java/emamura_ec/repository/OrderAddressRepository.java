package emamura_ec.repository;

import emamura_ec.entity.OrderAddress;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderAddressRepository extends JpaRepository<OrderAddress, Long> {
}
