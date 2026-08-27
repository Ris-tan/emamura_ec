package emamura_ec.security;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.transaction.annotation.Transactional;

import emamura_ec.entity.User;
import emamura_ec.entity.UserRole;
import emamura_ec.repository.UserRepository;

@SpringBootTest
@Transactional
class CustomUserDetailsServiceTest {

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    @Autowired
    private UserRepository userRepository;

    @Test
    void userRoleIsMappedToRoleUserAuthority() {
        User user = saveUser("user", UserRole.USER);

        UserDetails userDetails = customUserDetailsService.loadUserByUsername(user.getEmail());

        assertTrue(userDetails.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_USER".equals(authority.getAuthority())));
    }

    @Test
    void adminRoleIsMappedToRoleAdminAuthority() {
        User user = saveUser("admin", UserRole.ADMIN);

        UserDetails userDetails = customUserDetailsService.loadUserByUsername(user.getEmail());

        assertTrue(userDetails.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority())));
    }

    private User saveUser(String suffix, UserRole role) {
        return userRepository.save(new User(
                "Test " + suffix,
                suffix + "-" + System.nanoTime() + "@example.com",
                "{bcrypt}test-password",
                "09000000000",
                null,
                role));
    }
}
