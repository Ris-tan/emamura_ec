package emamura_ec.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import emamura_ec.dto.AdminProductView;
import emamura_ec.entity.Category;
import emamura_ec.entity.Product;
import emamura_ec.exception.AdminProductException;
import emamura_ec.form.AdminProductForm;
import emamura_ec.repository.CategoryRepository;
import emamura_ec.repository.ProductRepository;

@Service
public class AdminProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public AdminProductService(
            ProductRepository productRepository,
            CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }

    @Transactional(readOnly = true)
    public List<AdminProductView> findAllProducts() {
        // 管理者画面では販売停止商品も含め、再販売や内容確認に必要な全商品を表示する。
        return productRepository.findAll().stream()
                .map(this::toView)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<Category> findAllCategories() {
        return categoryRepository.findAllByOrderByCategoryNameAsc();
    }

    @Transactional(readOnly = true)
    public AdminProductForm findForm(Long productId) {
        Product product = findProduct(productId);
        AdminProductForm form = new AdminProductForm();
        form.setProductName(product.getProductName());
        form.setCategoryId(product.getCategory().getCategoryId());
        form.setDescription(product.getDescription());
        form.setPrice(product.getPrice());
        form.setStock(product.getStock());
        form.setImageUrl(product.getImageUrl());
        form.setIsActive(product.getIsActive());
        return form;
    }

    @Transactional
    public void create(AdminProductForm form) {
        Category category = findCategory(form.getCategoryId());
        Product product = new Product(
                trimRequired(form.getProductName()),
                form.getPrice(),
                form.getStock(),
                category,
                trimToNull(form.getDescription()),
                trimToNull(form.getImageUrl()),
                form.getIsActive());
        productRepository.save(product);
    }

    @Transactional
    public void update(Long productId, AdminProductForm form) {
        Product product = findProduct(productId);
        Category category = findCategory(form.getCategoryId());

        product.setProductName(trimRequired(form.getProductName()));
        product.setCategory(category);
        product.setDescription(trimToNull(form.getDescription()));
        product.setPrice(form.getPrice());
        product.setStock(form.getStock());
        product.setImageUrl(trimToNull(form.getImageUrl()));
        product.setIsActive(form.getIsActive());

        // 物理削除ではなくisActiveを更新することで、過去のOrderItemから参照される商品を残す。
        // 取得したEntityはトランザクション内で管理されているため、saveを明示せずDirty Checkingに任せる。
    }

    private Product findProduct(Long productId) {
        if (productId == null) {
            throw new AdminProductException("指定された商品が見つかりません。");
        }
        return productRepository.findById(productId)
                .orElseThrow(() -> new AdminProductException("指定された商品が見つかりません。"));
    }

    /**
     * categoryIdはフォームから送られた単なる識別子なので、画面の表示値や改ざんを信用せず
     * DBからカテゴリを再取得して商品へ関連付ける。
     */
    private Category findCategory(Long categoryId) {
        if (categoryId == null) {
            throw new AdminProductException("カテゴリを選択してください。");
        }
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new AdminProductException("指定されたカテゴリが見つかりません。"));
    }

    private AdminProductView toView(Product product) {
        return new AdminProductView(
                product.getProductId(),
                product.getProductName(),
                product.getCategory() == null ? "" : product.getCategory().getCategoryName(),
                product.getPrice(),
                product.getStock(),
                product.getImageUrl(),
                product.getIsActive());
    }

    private String trimRequired(String value) {
        return StringUtils.hasText(value) ? value.trim() : value;
    }

    private String trimToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}
