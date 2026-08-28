package emamura_ec.repository;

import emamura_ec.entity.Category;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class CategoryRepositoryTest {

    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    void カテゴリを保存して取得できる() {

        Category category = new Category("花束");

        Category saved = categoryRepository.save(category);

        Category found = categoryRepository
                .findById(saved.getCategoryId())
                .orElseThrow();

        assertEquals("花束", found.getCategoryName());
    }
}