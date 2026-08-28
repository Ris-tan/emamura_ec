package emamura_ec.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import emamura_ec.entity.UserRole;
import emamura_ec.form.RegisterForm;
import emamura_ec.repository.UserRepository;

@SpringBootTest
@Transactional
class UserServiceTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Test
    void registrationAlwaysCreatesAUserRole() {
        String email = UUID.randomUUID() + "@example.com";
        RegisterForm form = new RegisterForm();
        form.setName("Test User");
        form.setEmail(email);
        form.setPassword("password");
        form.setPasswordConfirmation("password");
        form.setPhoneNumber("09000000000");

        userService.register(form);

        assertEquals(UserRole.USER, userRepository.findByEmail(email).orElseThrow().getRole());
    }
}
