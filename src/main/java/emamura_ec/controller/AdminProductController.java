package emamura_ec.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import emamura_ec.exception.AdminProductException;
import emamura_ec.form.AdminProductForm;
import emamura_ec.service.AdminProductService;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/admin/products")
public class AdminProductController {

    private final AdminProductService adminProductService;

    public AdminProductController(AdminProductService adminProductService) {
        this.adminProductService = adminProductService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("products", adminProductService.findAllProducts());
        return "admin/products/list";
    }

    @GetMapping("/new")
    public String showNewForm(Model model) {
        prepareForm(model, new AdminProductForm(), "/admin/products", "商品を新規登録", "登録する");
        return "admin/products/form";
    }

    @PostMapping
    public String create(
            @Valid @ModelAttribute("productForm") AdminProductForm form,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            prepareForm(model, form, "/admin/products", "商品を新規登録", "登録する");
            return "admin/products/form";
        }

        try {
            adminProductService.create(form);
            redirectAttributes.addFlashAttribute("adminProductMessage", "商品を登録しました。");
            return "redirect:/admin/products";
        } catch (AdminProductException exception) {
            bindingResult.reject("product.validation", exception.getMessage());
            prepareForm(model, form, "/admin/products", "商品を新規登録", "登録する");
            return "admin/products/form";
        }
    }

    @GetMapping("/{productId}/edit")
    public String showEditForm(
            @PathVariable Long productId,
            Model model,
            RedirectAttributes redirectAttributes) {
        try {
            prepareForm(
                    model,
                    adminProductService.findForm(productId),
                    "/admin/products/" + productId,
                    "商品を編集",
                    "更新する");
            return "admin/products/form";
        } catch (AdminProductException exception) {
            redirectAttributes.addFlashAttribute("adminProductError", exception.getMessage());
            return "redirect:/admin/products";
        }
    }

    @PostMapping("/{productId}")
    public String update(
            @PathVariable Long productId,
            @Valid @ModelAttribute("productForm") AdminProductForm form,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            prepareForm(
                    model,
                    form,
                    "/admin/products/" + productId,
                    "商品を編集",
                    "更新する");
            return "admin/products/form";
        }

        try {
            adminProductService.update(productId, form);
            redirectAttributes.addFlashAttribute("adminProductMessage", "商品を更新しました。");
            return "redirect:/admin/products";
        } catch (AdminProductException exception) {
            bindingResult.reject("product.validation", exception.getMessage());
            prepareForm(
                    model,
                    form,
                    "/admin/products/" + productId,
                    "商品を編集",
                    "更新する");
            return "admin/products/form";
        }
    }

    private void prepareForm(
            Model model,
            AdminProductForm form,
            String action,
            String title,
            String submitLabel) {
        model.addAttribute("productForm", form);
        model.addAttribute("productFormAction", action);
        model.addAttribute("productFormTitle", title);
        model.addAttribute("productFormSubmitLabel", submitLabel);
        model.addAttribute("categories", adminProductService.findAllCategories());
    }
}
