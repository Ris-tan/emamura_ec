package emamura_ec.repository;

import java.util.List;
import java.util.Optional;

import emamura_ec.entity.Order;
import emamura_ec.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByUserOrderByOrderDateDesc(User user);

    Optional<Order> findByOrderIdAndUser(Long orderId, User user);
}
