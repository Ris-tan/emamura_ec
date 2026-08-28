package emamura_ec.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import emamura_ec.exception.UserAddressException;
import emamura_ec.form.UserAddressForm;
import emamura_ec.service.UserAddressService;
import jakarta.validation.Valid;

@Controller
public class UserAddressController {

    private final UserAddressService userAddressService;

    public UserAddressController(UserAddressService userAddressService) {
        this.userAddressService = userAddressService;
    }

    @GetMapping("/mypage/addresses")
    public String list(
            Authentication authentication,
            Model model,
            RedirectAttributes redirectAttributes) {
        try {
            model.addAttribute("addresses", userAddressService.findAll(authentication.getName()));
            return "mypage/addresses/list";
        } catch (UserAddressException exception) {
            redirectAttributes.addFlashAttribute("addressError", exception.getMessage());
            return "redirect:/mypage";
        }
    }

    @GetMapping("/mypage/addresses/new")
    public String showNewForm(Model model) {
        prepareForm(model, new UserAddressForm(), "/mypage/addresses", "新しいお届け先", "登録する");
        return "mypage/addresses/form";
    }

    @PostMapping("/mypage/addresses")
    public String create(
            Authentication authentication,
            @Valid @ModelAttribute("userAddressForm") UserAddressForm form,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            prepareForm(model, form, "/mypage/addresses", "新しいお届け先", "登録する");
            return "mypage/addresses/form";
        }

        try {
            userAddressService.create(authentication.getName(), form);
            redirectAttributes.addFlashAttribute("addressMessage", "お届け先を登録しました。");
            return "redirect:/mypage/addresses";
        } catch (UserAddressException exception) {
            bindingResult.reject("address.validation", exception.getMessage());
            prepareForm(model, form, "/mypage/addresses", "新しいお届け先", "登録する");
            return "mypage/addresses/form";
        }
    }

    @GetMapping("/mypage/addresses/{addressId}/edit")
    public String showEditForm(
            @PathVariable Long addressId,
            Authentication authentication,
            Model model,
            RedirectAttributes redirectAttributes) {
        try {
            UserAddressForm form = userAddressService.findForm(authentication.getName(), addressId);
            prepareForm(model, form, "/mypage/addresses/" + addressId, "お届け先を編集", "更新する");
            return "mypage/addresses/form";
        } catch (UserAddressException exception) {
            redirectAttributes.addFlashAttribute("addressError", exception.getMessage());
            return "redirect:/mypage/addresses";
        }
    }

    @PostMapping("/mypage/addresses/{addressId}")
    public String update(
            @PathVariable Long addressId,
            Authentication authentication,
            @Valid @ModelAttribute("userAddressForm") UserAddressForm form,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            prepareForm(model, form, "/mypage/addresses/" + addressId, "お届け先を編集", "更新する");
            return "mypage/addresses/form";
        }

        try {
            userAddressService.update(authentication.getName(), addressId, form);
            redirectAttributes.addFlashAttribute("addressMessage", "お届け先を更新しました。");
        } catch (UserAddressException exception) {
            redirectAttributes.addFlashAttribute("addressError", exception.getMessage());
        }
        return "redirect:/mypage/addresses";
    }

    @PostMapping("/mypage/addresses/{addressId}/delete")
    public String delete(
            @PathVariable Long addressId,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {
        try {
            userAddressService.delete(authentication.getName(), addressId);
            redirectAttributes.addFlashAttribute("addressMessage", "お届け先を削除しました。");
        } catch (UserAddressException exception) {
            redirectAttributes.addFlashAttribute("addressError", exception.getMessage());
        }
        return "redirect:/mypage/addresses";
    }

    @PostMapping("/mypage/addresses/{addressId}/default")
    public String setDefault(
            @PathVariable Long addressId,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {
        try {
            userAddressService.setDefault(authentication.getName(), addressId);
            redirectAttributes.addFlashAttribute("addressMessage", "既定のお届け先を変更しました。");
        } catch (UserAddressException exception) {
            redirectAttributes.addFlashAttribute("addressError", exception.getMessage());
        }
        return "redirect:/mypage/addresses";
    }

    private void prepareForm(
            Model model,
            UserAddressForm form,
            String action,
            String title,
            String submitLabel) {
        model.addAttribute("userAddressForm", form);
        model.addAttribute("addressFormAction", action);
        model.addAttribute("addressFormTitle", title);
        model.addAttribute("addressFormSubmitLabel", submitLabel);
    }

}
