package emamura_ec.repository;

import java.util.List;
import java.util.Optional;

import emamura_ec.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findByIsActiveTrueOrderByProductIdAsc();

    Optional<Product> findByProductIdAndIsActiveTrue(Long productId);
}
