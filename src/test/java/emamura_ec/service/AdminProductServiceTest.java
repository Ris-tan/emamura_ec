package emamura_ec.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import emamura_ec.dto.AdminProductView;
import emamura_ec.entity.Category;
import emamura_ec.entity.Product;
import emamura_ec.exception.AdminProductException;
import emamura_ec.form.AdminProductForm;
import emamura_ec.repository.CategoryRepository;
import emamura_ec.repository.ProductRepository;

@SpringBootTest
@Transactional
class AdminProductServiceTest {

    @Autowired
    private AdminProductService adminProductService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    void findAllProducts_includesActiveAndStoppedProducts() {
        Category category = createCategory();
        Product active = productRepository.save(new Product(
                "販売中商品", 1000, 3, category, "説明", "/images/active.jpg", true));
        Product stopped = productRepository.save(new Product(
                "販売停止商品", 2000, 0, category, "説明", "/images/stopped.jpg", false));

        var products = adminProductService.findAllProducts();

        assertTrue(products.stream().anyMatch(item -> item.getProductId().equals(active.getProductId())));
        assertTrue(products.stream().anyMatch(item -> item.getProductId().equals(stopped.getProductId())));
    }

    @Test
    void create_reloadsCategoryAndPersistsProductFields() {
        Category category = createCategory();
        AdminProductForm form = form("新商品", category.getCategoryId(), 3500, 8, true);

        adminProductService.create(form);

        Product saved = productRepository.findAll().stream()
                .filter(product -> "新商品".equals(product.getProductName()))
                .findFirst()
                .orElseThrow();
        assertEquals(category.getCategoryId(), saved.getCategory().getCategoryId());
        assertEquals(3500, saved.getPrice());
        assertEquals(8, saved.getStock());
        assertTrue(saved.getIsActive());
    }

    @Test
    void update_changesProductManagementFieldsWithoutDeletingProduct() {
        Category originalCategory = createCategory();
        Category newCategory = createCategory();
        Product product = productRepository.save(new Product(
                "旧商品", 1000, 4, originalCategory, "旧説明", "/images/old.jpg", true));
        AdminProductForm form = form("更新商品", newCategory.getCategoryId(), 2500, 12, false);
        form.setDescription("新しい説明");
        form.setImageUrl("/images/new.jpg");

        adminProductService.update(product.getProductId(), form);

        Product updated = productRepository.findById(product.getProductId()).orElseThrow();
        assertEquals("更新商品", updated.getProductName());
        assertEquals(newCategory.getCategoryId(), updated.getCategory().getCategoryId());
        assertEquals("新しい説明", updated.getDescription());
        assertEquals(2500, updated.getPrice());
        assertEquals(12, updated.getStock());
        assertEquals("/images/new.jpg", updated.getImageUrl());
        assertFalse(updated.getIsActive());
    }

    @Test
    void findForm_readsStoppedProductForAdminEditing() {
        Category category = createCategory();
        Product product = productRepository.save(new Product(
                "停止商品", 1800, 0, category, "説明", null, false));

        AdminProductForm form = adminProductService.findForm(product.getProductId());

        assertEquals("停止商品", form.getProductName());
        assertEquals(category.getCategoryId(), form.getCategoryId());
        assertFalse(form.getIsActive());
    }

    @Test
    void create_rejectsUnknownCategory() {
        AdminProductForm form = form("不正カテゴリ商品", 999999L, 1000, 1, true);

        assertThrows(AdminProductException.class, () -> adminProductService.create(form));
        assertFalse(productRepository.findAll().stream()
                .anyMatch(product -> "不正カテゴリ商品".equals(product.getProductName())));
    }

    private Category createCategory() {
        return categoryRepository.save(new Category("カテゴリ-" + UUID.randomUUID()));
    }

    private AdminProductForm form(
            String productName,
            Long categoryId,
            int price,
            int stock,
            boolean active) {
        AdminProductForm form = new AdminProductForm();
        form.setProductName(productName);
        form.setCategoryId(categoryId);
        form.setPrice(price);
        form.setStock(stock);
        form.setIsActive(active);
        return form;
    }
}
