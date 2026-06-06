package vn.edu.fpt.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Entity
@Table(name = "roles")
public class Role extends BaseEntity {

    @Column(name = "name", nullable = false, unique = true, length = 50)
    private String name;

    @Column(length = 255)
    private String description;

    @Builder.Default
    @OneToMany(mappedBy = "role", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<UserRole> userRoles = new HashSet<>();

    public void removeUser(User user) {
        if (userRoles == null) {
            return;
        }
        Iterator<UserRole> roleIterator = userRoles.iterator();
        while (roleIterator.hasNext()) {
            UserRole userRole = roleIterator.next();
            User currentUser = userRole.getUser();
            if (currentUser == user ||
                    (currentUser.getId() != null
                            && user.getId() != null
                            && currentUser.getId().equals(user.getId()))) {
                roleIterator.remove();
            }
        }
        if (user.getUserRoles() != null) {
            Iterator<UserRole> userIterator = user.getUserRoles().iterator();
            while (userIterator.hasNext()) {
                UserRole userRole = userIterator.next();
                Role currentRole = userRole.getRole();
                if (currentRole == this ||
                        (currentRole.getId() != null
                                && this.getId() != null
                                && currentRole.getId().equals(this.getId()))) {
                    userIterator.remove();
                }
            }
        }
    }

    public Set<User> getUsers() {
        Set<User> users = new HashSet<>();
        if (userRoles == null) {
            return users;
        }
        for (UserRole userRole : userRoles) {
            users.add(userRole.getUser());
        }
        return users;
    }

    public void addUser(User user) {
        if (userRoles == null) {
            userRoles = new HashSet<>();
        }

        boolean exists = false;

        for (UserRole userRole : userRoles) {
            User currentUser = userRole.getUser();
            if (currentUser == user) {
                exists = true;
                break;
            }
            if (currentUser.getId() != null
                    && user.getId() != null
                    && currentUser.getId().equals(user.getId())) {
                exists = true;
                break;
            }
        }
        if (!exists) {
            UserRole userRole = UserRole.builder()
                    .user(user)
                    .role(this)
                    .build();

            userRoles.add(userRole);
            user.addUserRole(this);
        }
    }

}
