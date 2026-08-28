package emamura_ec.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import emamura_ec.dto.CartView;
import emamura_ec.exception.CartException;
import emamura_ec.service.CartService;
import jakarta.servlet.http.HttpSession;

@Controller
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping("/cart")
    public String showCart(HttpSession session, Model model) {
        CartView cart = cartService.getCart(session);
        model.addAttribute("cart", cart);
        return "cart";
    }

    @PostMapping("/cart/items")
    public String addItem(
            @RequestParam Long productId,
            // 数量の形式エラーをSpringの例外画面にせず、カート画面で利用者へ返すため文字列で受け取る。
            @RequestParam(defaultValue = "1") String quantity,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        try {
            cartService.addItem(session, productId, parseQuantity(quantity));
            return "redirect:/cart";
        } catch (CartException exception) {
            return redirectWithError(redirectAttributes, exception.getMessage());
        }
    }

    @PostMapping("/cart/items/{productId}")
    public String updateItem(
            @PathVariable Long productId,
            @RequestParam String quantity,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        try {
            cartService.updateItem(session, productId, parseQuantity(quantity));
            return "redirect:/cart";
        } catch (CartException exception) {
            return redirectWithError(redirectAttributes, exception.getMessage());
        }
    }

    @PostMapping("/cart/items/{productId}/delete")
    public String removeItem(
            @PathVariable Long productId,
            HttpSession session) {
        cartService.removeItem(session, productId);
        return "redirect:/cart";
    }

    private int parseQuantity(String quantity) {
        try {
            return Integer.parseInt(quantity);
        } catch (NumberFormatException exception) {
            throw new CartException("数量は整数で入力してください。");
        }
    }

    private String redirectWithError(RedirectAttributes redirectAttributes, String message) {
        redirectAttributes.addFlashAttribute("cartError", message);
        return "redirect:/cart";
    }
}
