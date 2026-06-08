package vn.edu.fpt.security;

import lombok.Getter;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import org.springframework.security.core.userdetails.UserDetails;
import vn.edu.fpt.entity.User;

import java.util.Collection;
import java.util.List;

@Getter
public class CustomUserDetails implements UserDetails {

    private final User user;


    public CustomUserDetails(User user) {
        this.user = user;
    }

    @Override
    public Collection<? extends GrantedAuthority>
    getAuthorities() {
        List<SimpleGrantedAuthority> authorities = new java.util.ArrayList<>();
        if (user.getRoles() != null) {
            for (vn.edu.fpt.entity.Role role : user.getRoles()) {
                authorities.add(new SimpleGrantedAuthority(
                        "ROLE_" + role.getName().toUpperCase()
                ));
            }
        }
        return authorities;
    }


    @Override
    public String getPassword() {
        return user.getPasswordHash();
    }

    @Override
    public String getUsername() {
        return user.getEmail();
    }

    /**
     * Getter để truy cập User entity từ SecurityContext
     * @return User entity
     */
    public User getUser() {
        return user;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
