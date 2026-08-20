package emamura_ec.controller;

import java.util.Objects;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import emamura_ec.form.RegisterForm;
import emamura_ec.service.UserService;
import jakarta.validation.Valid;

@Controller
public class RegisterController {

    private final UserService userService;

    public RegisterController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("registerForm", new RegisterForm());
        return "register";
    }

    @PostMapping("/register")
    public String register(
            @Valid @ModelAttribute("registerForm") RegisterForm form,
            BindingResult bindingResult) {

        if (!Objects.equals(form.getPassword(), form.getPasswordConfirmation())) {
            bindingResult.rejectValue(
                    "passwordConfirmation",
                    "password.mismatch",
                    "パスワードとパスワード確認が一致していません");
        }

        if (!bindingResult.hasFieldErrors("email")
                && userService.existsByEmail(form.getEmail())) {
            bindingResult.rejectValue(
                    "email",
                    "email.duplicate",
                    "このメールアドレスは既に登録されています");
        }

        if (bindingResult.hasErrors()) {
            return "register";
        }

        userService.register(form);
        return "redirect:/register/complete";
    }

    @GetMapping("/register/complete")
    public String showRegisterComplete() {
        return "register-complete";
    }
}
