package emamura_ec.service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;

import emamura_ec.dto.CartItemView;
import emamura_ec.dto.CartView;
import emamura_ec.entity.Product;
import emamura_ec.exception.CartException;
import emamura_ec.repository.ProductRepository;
import jakarta.servlet.http.HttpSession;

@Service
public class CartService {

    private static final String CART_SESSION_ATTRIBUTE = "cartItems";

    private final ProductRepository productRepository;

    public CartService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public void addItem(HttpSession session, Long productId, int quantity) {
        validateQuantity(quantity);

        Product product = findProduct(productId);
        Map<Long, Integer> cartItems = getCartItems(session);
        int currentQuantity = cartItems.getOrDefault(productId, 0);
        long newQuantity = (long) currentQuantity + quantity;

        // セッションに保存された数量はそのまま信用せず、DBの最新在庫と照合する。
        validateStock(product, newQuantity);

        cartItems.put(productId, (int) newQuantity);
        saveCartItems(session, cartItems);
    }

    public CartView getCart(HttpSession session) {
        Map<Long, Integer> cartItems = getCartItems(session);
        List<CartItemView> itemViews = new ArrayList<>();
        long total = 0;
        boolean cartChanged = false;

        Iterator<Map.Entry<Long, Integer>> iterator = cartItems.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Long, Integer> entry = iterator.next();
            Product product = productRepository.findById(entry.getKey()).orElse(null);
            Integer quantity = entry.getValue();

            if (product == null || quantity == null || quantity < 1
                    || product.getStock() == null || product.getStock() < 1) {
                iterator.remove();
                cartChanged = true;
                continue;
            }

            int displayQuantity = Math.min(quantity, product.getStock());
            if (displayQuantity != quantity) {
                entry.setValue(displayQuantity);
                cartChanged = true;
            }

            // 価格はセッションから取得せず、DBから再取得した商品情報で表示額を計算する。
            long subtotal = (long) product.getPrice() * displayQuantity;
            itemViews.add(new CartItemView(product, displayQuantity, subtotal));
            total += subtotal;
        }

        if (cartChanged) {
            saveCartItems(session, cartItems);
        }

        return new CartView(List.copyOf(itemViews), total);
    }

    public void updateItem(HttpSession session, Long productId, int quantity) {
        validateQuantity(quantity);

        Map<Long, Integer> cartItems = getCartItems(session);
        if (!cartItems.containsKey(productId)) {
            throw new CartException("指定された商品はカートにありません。");
        }

        Product product = findProduct(productId);
        validateStock(product, quantity);

        cartItems.put(productId, quantity);
        saveCartItems(session, cartItems);
    }

    public void removeItem(HttpSession session, Long productId) {
        Map<Long, Integer> cartItems = getCartItems(session);
        cartItems.remove(productId);
        saveCartItems(session, cartItems);
    }

    private Product findProduct(Long productId) {
        Optional<Product> product = productRepository.findById(productId);
        return product.orElseThrow(() -> new CartException("指定された商品は存在しません。"));
    }

    private void validateQuantity(int quantity) {
        if (quantity < 1) {
            throw new CartException("数量は1以上で指定してください。");
        }
    }

    private void validateStock(Product product, long quantity) {
        if (product.getStock() == null || quantity > product.getStock()) {
            throw new CartException("在庫数を超える数量は指定できません。");
        }
    }

    private Map<Long, Integer> getCartItems(HttpSession session) {
        Object savedCart = session.getAttribute(CART_SESSION_ATTRIBUTE);
        if (savedCart instanceof Map<?, ?> savedMap) {
            Map<Long, Integer> cartItems = new LinkedHashMap<>();
            savedMap.forEach((productId, quantity) -> {
                if (productId instanceof Long && quantity instanceof Integer) {
                    cartItems.put((Long) productId, (Integer) quantity);
                }
            });
            return cartItems;
        }
        return new LinkedHashMap<>();
    }

    private void saveCartItems(HttpSession session, Map<Long, Integer> cartItems) {
        if (cartItems.isEmpty()) {
            session.removeAttribute(CART_SESSION_ATTRIBUTE);
            return;
        }
        session.setAttribute(CART_SESSION_ATTRIBUTE, cartItems);
    }
}
