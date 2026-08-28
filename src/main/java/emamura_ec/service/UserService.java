package emamura_ec.service;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import emamura_ec.entity.User;
import emamura_ec.entity.UserRole;
import emamura_ec.form.RegisterForm;
import emamura_ec.repository.UserRepository;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    public void register(RegisterForm form) {
        String encodedPassword = passwordEncoder.encode(form.getPassword());
        User user = new User(
                form.getName(),
                form.getEmail(),
                encodedPassword,
                form.getPhoneNumber(),
                form.getBirthDate(),
                // Registration must never allow a submitted form to choose an elevated role.
                UserRole.USER);

        userRepository.save(user);
    }
}
