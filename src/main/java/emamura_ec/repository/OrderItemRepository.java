package emamura_ec.repository;

import java.util.List;

import emamura_ec.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    List<OrderItem> findByOrder_OrderIdOrderByOrderItemIdAsc(Long orderId);
}
