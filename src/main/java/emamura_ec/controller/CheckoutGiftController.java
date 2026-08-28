package emamura_ec.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import emamura_ec.exception.CheckoutGiftException;
import emamura_ec.form.CheckoutGiftForm;
import emamura_ec.service.CartService;
import emamura_ec.service.CheckoutGiftService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Controller
public class CheckoutGiftController {

    private final CartService cartService;
    private final CheckoutGiftService checkoutGiftService;

    public CheckoutGiftController(
            CartService cartService,
            CheckoutGiftService checkoutGiftService) {
        this.cartService = cartService;
        this.checkoutGiftService = checkoutGiftService;
    }

    @GetMapping("/checkout/gift")
    public String showGiftForm(HttpSession session, Model model) {
        if (!canStartGiftSetting(session)) {
            return redirectToDeliveryOrCart(session);
        }

        return showForm(session, model, checkoutGiftService.createForm(session));
    }

    @PostMapping("/checkout/gift")
    public String submitGiftForm(
            @Valid @ModelAttribute("checkoutGiftForm") CheckoutGiftForm form,
            BindingResult bindingResult,
            HttpSession session,
            Model model,
            RedirectAttributes redirectAttributes) {
        if (!canStartGiftSetting(session)) {
            return redirectToDeliveryOrCart(session);
        }

        if (bindingResult.hasErrors()) {
            checkoutGiftService.refreshDisplayData(session, form);
            return showForm(session, model, form);
        }

        try {
            checkoutGiftService.saveToSession(
                    session,
                    checkoutGiftService.validateAndCreate(session, form));
            return "redirect:/checkout/delivery";
        } catch (CheckoutGiftException exception) {
            redirectAttributes.addFlashAttribute("giftError", exception.getMessage());
            return "redirect:/checkout/gift";
        }
    }

    private String showForm(HttpSession session, Model model, CheckoutGiftForm form) {
        model.addAttribute("checkoutGiftForm", form);
        model.addAttribute("paperBagUnitPrice", checkoutGiftService.getPaperBagUnitPrice());
        model.addAttribute("wrappingTypes", emamura_ec.entity.WrappingType.values());
        model.addAttribute("ribbonColors", emamura_ec.entity.RibbonColor.values());
        return "checkout/gift";
    }

    private boolean canStartGiftSetting(HttpSession session) {
        // Gift is an optional branch; users who did not opt in must never see this screen through a direct URL.
        return !cartService.getCart(session).getItems().isEmpty()
                && checkoutGiftService.isGiftEnabled(session);
    }

    private String redirectToDeliveryOrCart(HttpSession session) {
        if (cartService.getCart(session).getItems().isEmpty()) {
            return "redirect:/cart";
        }
        return "redirect:/checkout/delivery";
    }
}
