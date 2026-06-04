package vn.edu.fpt.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.fpt.dto.RegisterRequest;
import vn.edu.fpt.entity.Role;
import vn.edu.fpt.entity.User;
import vn.edu.fpt.repository.RoleRepository;
import vn.edu.fpt.repository.UserRepository;

import java.util.Optional;

@Service
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;



    public AuthService(
            UserRepository userRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder) {

        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public void register(RegisterRequest request) {

        if (userRepository.existsByEmail(
                request.getEmail())) {

            throw new RuntimeException(
                    "Email already exists");
        }

        Role role = roleRepository
                .findByName("learner")
                .orElseThrow();

        User user = new User();

        user.setEmail(request.getEmail());

        user.setPasswordHash(
                passwordEncoder.encode(
                        request.getPassword()));

        user.setFirstName(
                request.getFirstName());

        user.setLastName(
                request.getLastName());

        user.setPhone(request.getPhoneNumber());

        user.setStatus("ACTIVE");


        user.addUserRole(role);

        userRepository.save(user);
    }

//    public User login(String email, String password) {
//
//        Optional<User> optionalUser = userRepository.findByEmail(email);
//
//        if (!optionalUser.isPresent()) {
//            throw new RuntimeException("Email không tồn tại");
//        }
//
//        User user = optionalUser.get();
//
//        boolean match = passwordEncoder.matches(password, user.getPasswordHash());
//
//        if (!match) {
//            throw new RuntimeException("Sai mật khẩu");
//        }
//        System.out.println("INPUT password: " + password);
//        System.out.println("DB hash: " + user.getPasswordHash());
//        System.out.println("MATCH: " + passwordEncoder.matches(password, user.getPasswordHash()));
//
//        return user;
//    }
}
