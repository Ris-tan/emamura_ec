package emamura_ec.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import emamura_ec.entity.Category;
import emamura_ec.entity.Product;
import emamura_ec.repository.CategoryRepository;
import emamura_ec.repository.ProductRepository;

@SpringBootTest
@Transactional
class ProductServiceTest {

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    void publicProductQueriesExcludeStoppedProducts() {
        Category category = categoryRepository.save(new Category("公開カテゴリ"));
        Product active = productRepository.save(new Product(
                "公開商品", 1000, 1, category, "説明", null, true));
        Product stopped = productRepository.save(new Product(
                "停止商品", 1000, 1, category, "説明", null, false));

        var products = productService.getAllProducts();

        assertTrue(products.stream().anyMatch(product -> product.getProductId().equals(active.getProductId())));
        assertFalse(products.stream().anyMatch(product -> product.getProductId().equals(stopped.getProductId())));
        assertTrue(productService.getProductById(active.getProductId()).isPresent());
        assertTrue(productService.getProductById(stopped.getProductId()).isEmpty());
    }
}
