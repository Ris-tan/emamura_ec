package emamura_ec.repository;

import java.util.List;

import emamura_ec.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    List<Category> findAllByOrderByCategoryNameAsc();
}
