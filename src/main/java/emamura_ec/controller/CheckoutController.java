package emamura_ec.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import emamura_ec.dto.CartView;
import emamura_ec.dto.CheckoutDeliveryData;
import emamura_ec.dto.UserAddressView;
import emamura_ec.exception.CheckoutConfirmException;
import emamura_ec.exception.CheckoutDeliveryException;
import emamura_ec.form.CheckoutDeliveryConfirmForm;
import emamura_ec.form.CheckoutDeliveryForm;
import emamura_ec.service.CartService;
import emamura_ec.service.CheckoutConfirmService;
import emamura_ec.service.CheckoutDeliveryService;
import emamura_ec.service.CheckoutGiftService;
import emamura_ec.service.OrderPlacementService;
import emamura_ec.service.UserAddressService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Controller
public class CheckoutController {

    private final CartService cartService;
    private final CheckoutConfirmService checkoutConfirmService;
    private final CheckoutDeliveryService checkoutDeliveryService;
    private final CheckoutGiftService checkoutGiftService;
    private final OrderPlacementService orderPlacementService;
    private final UserAddressService userAddressService;

    public CheckoutController(
            CartService cartService,
            CheckoutConfirmService checkoutConfirmService,
            CheckoutDeliveryService checkoutDeliveryService,
            CheckoutGiftService checkoutGiftService,
            OrderPlacementService orderPlacementService,
            UserAddressService userAddressService) {
        this.cartService = cartService;
        this.checkoutConfirmService = checkoutConfirmService;
        this.checkoutDeliveryService = checkoutDeliveryService;
        this.checkoutGiftService = checkoutGiftService;
        this.orderPlacementService = orderPlacementService;
        this.userAddressService = userAddressService;
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
    public String showDeliveryForm(
            Authentication authentication,
            HttpSession session,
            Model model) {
        if (isCartEmpty(session) || !checkoutGiftService.isCheckoutStarted(session)) {
            return "redirect:/cart";
        }

        var storedDeliveryData = checkoutDeliveryService.getFromSession(session);
        CheckoutDeliveryForm form = storedDeliveryData
                .map(checkoutDeliveryService::toForm)
                .orElseGet(CheckoutDeliveryForm::new);
        List<UserAddressView> savedAddresses = userAddressService.findAll(authentication.getName());
        if (storedDeliveryData.isEmpty() && form.getSavedAddressId() == null && !savedAddresses.isEmpty()) {
            // The repository returns the default address first, so initial checkout can use the most likely choice
            // while still allowing the customer to switch to manual entry from the select box.
            form.setSavedAddressId(savedAddresses.get(0).getUserAddressId());
        }
        model.addAttribute("checkoutDeliveryForm", form);
        model.addAttribute("savedAddresses", savedAddresses);
        addEarliestDeliveryDate(model, form);
        return "checkout/delivery";
    }

    @PostMapping("/checkout/delivery")
    public String submitDeliveryForm(
            @Valid @ModelAttribute("checkoutDeliveryForm") CheckoutDeliveryForm form,
            BindingResult bindingResult,
            Authentication authentication,
            HttpSession session,
            Model model) {
        if (isCartEmpty(session) || !checkoutGiftService.isCheckoutStarted(session)) {
            return "redirect:/cart";
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("savedAddresses", userAddressService.findAll(authentication.getName()));
            addEarliestDeliveryDate(model, form);
            return "checkout/delivery";
        }

        try {
            CheckoutDeliveryData data = checkoutDeliveryService.validateAndCreate(form, authentication.getName());
            checkoutDeliveryService.saveToSession(session, data);
            return "redirect:/checkout/delivery/confirm";
        } catch (CheckoutDeliveryException exception) {
            bindingResult.reject("delivery.validation", exception.getMessage());
            model.addAttribute("savedAddresses", userAddressService.findAll(authentication.getName()));
            addEarliestDeliveryDate(model, form);
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

        CheckoutDeliveryData data = checkoutDeliveryService.getFromSession(session)
                .orElse(null);
        if (data == null) {
            return "redirect:/checkout/delivery";
        }

        try {
            LocalDate earliestDeliveryDate = checkoutDeliveryService.validateStoredDeliveryData(data);
            addDeliveryConfirmationModel(
                    model,
                    session,
                    data,
                    checkoutDeliveryService.toConfirmationForm(data),
                    earliestDeliveryDate);
            return "checkout/delivery-confirm";
        } catch (CheckoutDeliveryException exception) {
            model.addAttribute("checkoutDeliveryForm", checkoutDeliveryService.toForm(data));
            model.addAttribute("deliveryError", exception.getMessage());
            addEarliestDeliveryDate(model, checkoutDeliveryService.toForm(data));
            return "checkout/delivery";
        }
    }

    @PostMapping("/checkout/delivery/confirm")
    public String submitDeliveryConfirmation(
            @ModelAttribute("checkoutDeliveryConfirmForm") CheckoutDeliveryConfirmForm form,
            BindingResult bindingResult,
            HttpSession session,
            Model model) {
        if (isCartEmpty(session) || !checkoutGiftService.isCheckoutStarted(session)) {
            return "redirect:/cart";
        }

        CheckoutDeliveryData data = checkoutDeliveryService.getFromSession(session)
                .orElse(null);
        if (data == null) {
            return "redirect:/checkout/delivery";
        }

        LocalDate earliestDeliveryDate;
        try {
            // Validate the stored address and current delivery master before accepting a requested date.
            earliestDeliveryDate = checkoutDeliveryService.validateStoredDeliveryData(data);
        } catch (CheckoutDeliveryException exception) {
            model.addAttribute("checkoutDeliveryForm", checkoutDeliveryService.toForm(data));
            model.addAttribute("deliveryError", exception.getMessage());
            addEarliestDeliveryDate(model, checkoutDeliveryService.toForm(data));
            return "checkout/delivery";
        }

        addDeliveryConfirmationModel(model, session, data, form, earliestDeliveryDate);
        if (bindingResult.hasErrors()) {
            return "checkout/delivery-confirm";
        }

        try {
            CheckoutDeliveryData updatedData = checkoutDeliveryService.applyRequestedDeliveryDate(
                    data,
                    form.getRequestedDeliveryDate());
            checkoutDeliveryService.saveToSession(session, updatedData);
            return "redirect:/checkout/confirm";
        } catch (CheckoutDeliveryException exception) {
            model.addAttribute("deliveryError", exception.getMessage());
            return "checkout/delivery-confirm";
        }
    }

    @GetMapping("/checkout/confirm")
    public String showCheckoutConfirmation(
            HttpSession session,
            Model model,
            RedirectAttributes redirectAttributes) {
        try {
            model.addAttribute("checkoutConfirmView", checkoutConfirmService.createView(session));
            return "checkout/confirm";
        } catch (CheckoutConfirmException exception) {
            addCheckoutConfirmError(redirectAttributes, exception);
            return "redirect:" + exception.getRedirectPath();
        }
    }

    @PostMapping("/checkout/place-order")
    public String placeOrder(
            Authentication authentication,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        try {
            Long orderId = orderPlacementService.placeOrder(session, authentication.getName());
            // The transaction has completed successfully before this method clears checkout-only Session data.
            orderPlacementService.clearCheckoutSession(session);
            redirectAttributes.addFlashAttribute("orderId", orderId);
            return "redirect:/checkout/complete";
        } catch (CheckoutConfirmException exception) {
            addCheckoutConfirmError(redirectAttributes, exception);
            return "redirect:" + exception.getRedirectPath();
        }
    }

    @GetMapping("/checkout/complete")
    public String showCheckoutComplete(Model model) {
        if (!model.containsAttribute("orderId")) {
            return "redirect:/";
        }
        return "checkout/complete";
    }

    private void addCheckoutConfirmError(
            RedirectAttributes redirectAttributes,
            CheckoutConfirmException exception) {
        if ("/checkout/gift".equals(exception.getRedirectPath())) {
            redirectAttributes.addFlashAttribute("giftError", exception.getMessage());
        } else if ("/checkout/delivery".equals(exception.getRedirectPath())) {
            redirectAttributes.addFlashAttribute("deliveryError", exception.getMessage());
        } else {
            redirectAttributes.addFlashAttribute("cartError", exception.getMessage());
        }
    }

    private boolean isCartEmpty(HttpSession session) {
        CartView cart = cartService.getCart(session);
        return cart.getItems().isEmpty();
    }

    private void addEarliestDeliveryDate(Model model, CheckoutDeliveryForm form) {
        try {
            LocalDate earliestDeliveryDate = checkoutDeliveryService.calculateEarliestDeliveryDate(
                    form.getDeliveryOption(),
                    form.getPrefecture());
            if (earliestDeliveryDate != null) {
                model.addAttribute("earliestDeliveryDate", earliestDeliveryDate.toString());
                model.addAttribute(
                        "earliestDeliveryDateDisplay",
                        checkoutDeliveryService.formatDeliveryDate(earliestDeliveryDate));
            }
        } catch (CheckoutDeliveryException ignored) {
            // Validation messages from POST remain the source of truth when the prefecture is incomplete or unavailable.
        }
    }

    private void addDeliveryConfirmationModel(
            Model model,
            HttpSession session,
            CheckoutDeliveryData data,
            CheckoutDeliveryConfirmForm form,
            LocalDate earliestDeliveryDate) {
        model.addAttribute("checkoutDeliveryData", data);
        model.addAttribute("checkoutDeliveryConfirmForm", form);
        model.addAttribute("checkoutGiftEnabled", checkoutGiftService.isGiftEnabled(session));
        model.addAttribute("deliveryScheduleVisible", earliestDeliveryDate != null);
        if (earliestDeliveryDate != null) {
            model.addAttribute("earliestDeliveryDate", earliestDeliveryDate.toString());
        }
        model.addAttribute(
                "earliestDeliveryDateDisplay",
                checkoutDeliveryService.formatDeliveryDate(earliestDeliveryDate));
    }
}
