package emamura_ec.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;

import emamura_ec.entity.Category;
import emamura_ec.entity.Product;
import emamura_ec.exception.CartException;
import emamura_ec.repository.ProductRepository;
import jakarta.servlet.http.HttpSession;

class CartServiceTest {

    @Test
    void stoppedProductCannotBeAddedByDirectRequest() {
        ProductRepository productRepository = mock(ProductRepository.class);
        HttpSession session = mock(HttpSession.class);
        Product stopped = new Product(
                "販売停止商品", 1000, 5, new Category("カテゴリ"), "説明", null, false);
        when(productRepository.findById(1L)).thenReturn(Optional.of(stopped));

        CartService cartService = new CartService(productRepository);

        assertThrows(CartException.class, () -> cartService.addItem(session, 1L, 1));
    }
}
