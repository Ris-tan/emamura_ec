package emamura_ec.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import emamura_ec.exception.AdminOrderException;
import emamura_ec.service.AdminOrderService;

@Controller
@RequestMapping("/admin/orders")
public class AdminOrderController {

    private final AdminOrderService adminOrderService;

    public AdminOrderController(AdminOrderService adminOrderService) {
        this.adminOrderService = adminOrderService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("orders", adminOrderService.findAllOrders());
        return "admin/orders/list";
    }

    @GetMapping("/{orderId}")
    public String detail(
            @PathVariable Long orderId,
            Model model,
            RedirectAttributes redirectAttributes) {
        try {
            model.addAttribute("orderDetail", adminOrderService.findOrderDetail(orderId));
            model.addAttribute("orderStatusOptions", adminOrderService.getOrderStatusOptions(orderId));
            return "admin/orders/detail";
        } catch (AdminOrderException exception) {
            redirectAttributes.addFlashAttribute("adminOrderError", exception.getMessage());
            return "redirect:/admin/orders";
        }
    }

    @PostMapping("/{orderId}/status")
    public String updateStatus(
            @PathVariable Long orderId,
            @RequestParam(name = "status") String status,
            RedirectAttributes redirectAttributes) {
        try {
            adminOrderService.updateStatus(orderId, status);
            redirectAttributes.addFlashAttribute("adminOrderMessage", "注文ステータスを更新しました。");
        } catch (AdminOrderException exception) {
            redirectAttributes.addFlashAttribute("adminOrderError", exception.getMessage());
        }
        return "redirect:/admin/orders/" + orderId;
    }
}
