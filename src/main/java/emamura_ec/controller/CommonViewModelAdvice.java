package emamura_ec.controller;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * 全画面で利用する共通レイアウト向けの表示状態をModelへ追加する。
 *
 * Thymeleaf 3.1ではテンプレートからServletRequestを直接参照する方式に
 * 依存しにくいため、認証・認可の判定自体はSpring Securityへ委ね、
 * ヘッダー表示に必要な結果だけをMVCの共通Modelへ渡す。
 */
@ControllerAdvice
public class CommonViewModelAdvice {

    @ModelAttribute
    public void addAuthenticationState(Model model, Authentication authentication) {
        boolean authenticated = authentication != null
                && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken);

        boolean admin = authenticated && authentication.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()));

        model.addAttribute("isAuthenticated", authenticated);
        model.addAttribute("isAdmin", admin);
    }
}
