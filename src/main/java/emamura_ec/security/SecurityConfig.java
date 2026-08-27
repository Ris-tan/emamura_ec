package emamura_ec.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/",
                                "/products/**",
                                "/register/**",
                                "/login",
                                "/cart",
                                "/cart/**",
                                "/checkout/start",
                                "/css/**",
                                "/images/**",
                                "/js/**",
                                "/webjars/**",
                                "/favicon.ico",
                                "/error")
                        .permitAll()
                        // Hiding admin links is only a UI convenience; authorization must be enforced on the server.
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        // The start endpoint only records a non-sensitive option; the next checkout page requires login.
                        .requestMatchers("/checkout/**").authenticated()
                        .requestMatchers("/orders/**").authenticated()
                        .anyRequest().authenticated())
                .formLogin(form -> form
                        .loginPage("/login")
                        .usernameParameter("email")
                        // false lets SavedRequest return checkout users to the page they originally requested.
                        .defaultSuccessUrl("/", false)
                        .failureUrl("/login?error")
                        .permitAll())
                .logout(logout -> logout
                        .logoutSuccessUrl("/login?logout")
                        .permitAll());

        return http.build();
    }
}
