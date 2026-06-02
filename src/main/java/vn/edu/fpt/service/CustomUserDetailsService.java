package vn.edu.fpt.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import vn.edu.fpt.entity.User;
import vn.edu.fpt.repository.UserRepository;
import vn.edu.fpt.security.CustomUserDetails;

@Service
public class CustomUserDetailsService
        implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(
            UserRepository userRepository) {

        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email)
            throws UsernameNotFoundException {

        System.out.println("=== LOAD USER CALLED: " + email);

        User user = userRepository
                .findByEmail(email)
                .orElseThrow(() -> {
                    System.out.println("=== USER NOT FOUND: " + email);
                    return new UsernameNotFoundException("User not found");
                });

        System.out.println("=== USER FOUND: " + user.getEmail());
        System.out.println("=== PASSWORD HASH: " + user.getPasswordHash());
        System.out.println("=== ROLE: " + user.getRole().getName());

        // Thêm dòng này để test
//        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
//        System.out.println("=== TEST MATCH: " +
//                encoder.matches("Linh123@", user.getPasswordHash()));

        return new CustomUserDetails(user);
    }
}
