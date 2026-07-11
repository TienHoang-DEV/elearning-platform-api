package vn.edu.fpt.service;

import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import vn.edu.fpt.entity.Role;
import vn.edu.fpt.entity.User;
import vn.edu.fpt.enums.RoleType;
import vn.edu.fpt.enums.UserStatus;
import vn.edu.fpt.repository.RoleRepository;
import vn.edu.fpt.repository.UserRepository;

@Service
public class CustomOAuth2UserService
        extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    public CustomOAuth2UserService(
            UserRepository userRepository,
            RoleRepository roleRepository) {

        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    @Override
    public OAuth2User loadUser(
            OAuth2UserRequest request)
            throws OAuth2AuthenticationException {

        OAuth2User oAuth2User =
                super.loadUser(request);

        String email =
                oAuth2User.getAttribute("email");

        String googleId =
                oAuth2User.getAttribute("sub");

        User user =
                userRepository
                        .findByEmail(email)
                        .orElse(null);

        if (user == null) {

            Role role =
                    roleRepository
                            .findByName(RoleType.LEARNER)
                            .orElseThrow();

            String firstName =
                    oAuth2User.getAttribute("given_name");

            String lastName =
                    oAuth2User.getAttribute("family_name");

            if (firstName == null || firstName.isBlank()) {
                firstName = "";
            }

            if (lastName == null || lastName.isBlank()) {
                lastName = "";
            }

            String picture =
                    oAuth2User.getAttribute("picture");

            user = new User();

            user.setEmail(email);

            user.setGoogleId(googleId);

            user.setFirstName(firstName);

            user.setLastName(lastName);

            user.setAvatarUrl(picture);

            user.setStatus(UserStatus.ACTIVE);

            user.addUserRole(role);

            userRepository.save(user);

            user.setPhone("NONE_" + user.getId());
            //System.out.println(user.getId());

            userRepository.save(user);

        } else {

            if (user.getGoogleId() == null) {

                user.setGoogleId(
                        googleId);

                userRepository.save(user);
            }
        }

        return new vn.edu.fpt.security.CustomOAuth2User(oAuth2User, user);
    }
}
