package emamura_ec.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import emamura_ec.exception.OrderHistoryException;
import emamura_ec.service.OrderHistoryService;

@Controller
public class OrderHistoryController {

    private final OrderHistoryService orderHistoryService;

    public OrderHistoryController(OrderHistoryService orderHistoryService) {
        this.orderHistoryService = orderHistoryService;
    }

    @GetMapping("/orders")
    public String showOrderHistory(
            Authentication authentication,
            Model model,
            RedirectAttributes redirectAttributes) {
        try {
            model.addAttribute("orders", orderHistoryService.findOrders(authentication.getName()));
            return "orders/list";
        } catch (OrderHistoryException exception) {
            redirectAttributes.addFlashAttribute("orderError", exception.getMessage());
            return "redirect:/";
        }
    }

    @GetMapping("/orders/{orderId}")
    public String showOrderDetail(
            @PathVariable Long orderId,
            Authentication authentication,
            Model model,
            RedirectAttributes redirectAttributes) {
        try {
            model.addAttribute(
                    "orderDetail",
                    orderHistoryService.findOrderDetail(authentication.getName(), orderId));
            return "orders/detail";
        } catch (OrderHistoryException exception) {
            // A missing order and an order owned by somebody else use the same response to avoid leaking ownership.
            redirectAttributes.addFlashAttribute("orderError", exception.getMessage());
            return "redirect:/orders";
        }
    }
}
