package emamura_ec.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import emamura_ec.dto.CartView;
import emamura_ec.dto.CheckoutDeliveryData;
import emamura_ec.exception.CheckoutDeliveryException;
import emamura_ec.form.CheckoutDeliveryForm;
import emamura_ec.service.CartService;
import emamura_ec.service.CheckoutDeliveryService;
import emamura_ec.service.CheckoutGiftService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Controller
public class CheckoutController {

    private final CartService cartService;
    private final CheckoutDeliveryService checkoutDeliveryService;
    private final CheckoutGiftService checkoutGiftService;

    public CheckoutController(
            CartService cartService,
            CheckoutDeliveryService checkoutDeliveryService,
            CheckoutGiftService checkoutGiftService) {
        this.cartService = cartService;
        this.checkoutDeliveryService = checkoutDeliveryService;
        this.checkoutGiftService = checkoutGiftService;
    }

    @PostMapping("/checkout/start")
    public String startCheckout(
            @RequestParam(defaultValue = "false") boolean giftOption,
            HttpSession session) {
        if (isCartEmpty(session)) {
            return "redirect:/cart";
        }

        // Gift options belong to products, so this choice is completed before delivery details are collected.
        // Store it before authentication so the protected next page can be restored after login.
        checkoutGiftService.beginCheckout(session, giftOption);
        return giftOption ? "redirect:/checkout/gift" : "redirect:/checkout/delivery";
    }

    @GetMapping("/checkout/delivery")
    public String showDeliveryForm(HttpSession session, Model model) {
        if (isCartEmpty(session) || !checkoutGiftService.isCheckoutStarted(session)) {
            return "redirect:/cart";
        }

        model.addAttribute("checkoutDeliveryForm", new CheckoutDeliveryForm());
        return "checkout/delivery";
    }

    @PostMapping("/checkout/delivery")
    public String submitDeliveryForm(
            @Valid @ModelAttribute("checkoutDeliveryForm") CheckoutDeliveryForm form,
            BindingResult bindingResult,
            HttpSession session) {
        if (isCartEmpty(session) || !checkoutGiftService.isCheckoutStarted(session)) {
            return "redirect:/cart";
        }

        if (bindingResult.hasErrors()) {
            return "checkout/delivery";
        }

        try {
            CheckoutDeliveryData data = checkoutDeliveryService.validateAndCreate(form);
            checkoutDeliveryService.saveToSession(session, data);
            return "redirect:/checkout/delivery/confirm";
        } catch (CheckoutDeliveryException exception) {
            bindingResult.reject("delivery.validation", exception.getMessage());
            return "checkout/delivery";
        }
    }

    @GetMapping("/checkout/delivery/confirm")
    public String showDeliveryConfirmation(HttpSession session, Model model) {
        if (isCartEmpty(session)) {
            return "redirect:/cart";
        }

        if (!checkoutGiftService.isCheckoutStarted(session)) {
            return "redirect:/cart";
        }

        return checkoutDeliveryService.getFromSession(session)
                .map(data -> {
                    model.addAttribute("checkoutDeliveryData", data);
                    model.addAttribute("checkoutGiftEnabled", checkoutGiftService.isGiftEnabled(session));
                    return "checkout/delivery-confirm";
                })
                .orElse("redirect:/checkout/delivery");
    }

    private boolean isCartEmpty(HttpSession session) {
        CartView cart = cartService.getCart(session);
        return cart.getItems().isEmpty();
    }
}
