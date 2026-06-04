package vn.edu.fpt.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Entity
@Table(name = "users")
public class User extends BaseEntity {

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private Set<UserRole> userRoles = new HashSet<>();

    @Column(columnDefinition = "Nvarchar(255)", nullable = false)
    private String firstName;

    @Column(columnDefinition = "Nvarchar(255)", nullable = false)
    private String lastName;

    @Column(length = 255, nullable = false, unique = true)
    private String email;

    @Column(length = 20, unique = true, nullable = true)
    private String phone;

    @Column(columnDefinition = "NVARCHAR(MAX)", nullable = true)
    private String bio;

    @Column(length = 255, nullable = true)
    private String passwordHash;

    @Column(length = 500, nullable = true)
    private String avatarUrl;

    private String googleId;

    @Column(length = 20, nullable = false)
    private String status;

    @Builder.Default
    @OneToMany(mappedBy = "instructor", fetch = FetchType.LAZY)
    private Set<Course> courses = new HashSet<>();


    @Builder.Default
    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private Set<Order> orders = new HashSet<>();

    @Builder.Default
    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private Set<Enrollment> enrollments = new HashSet<>();


    public Set<Role> getRoles() {
        Set<Role> roles = new HashSet<>();
        if (userRoles == null) {
            return roles;
        }
        for (UserRole userRole : userRoles) {
            roles.add(userRole.getRole());
        }
        return roles;
    }

    public void addUserRole(Role role) {
        if (userRoles == null) {
            userRoles = new HashSet<>();
        }
        boolean exists = false;
        for (UserRole userRole : userRoles) {
            Role currentRole = userRole.getRole();
            if (currentRole == role) {
                exists = true;
                break;
            }
            if (currentRole.getId() != null
                    && role.getId() != null
                    && currentRole.getId().equals(role.getId())) {
                exists = true;
                break;
            }
        }
        if (!exists) {
            UserRole userRole = UserRole.builder()
                    .user(this)
                    .role(role)
                    .build();
            userRoles.add(userRole);
        }
    }

    public void addCourse(Course course) {
        courses.add(course);
        course.setInstructor(this);
    }

    public void removeCourse(Course course) {
        courses.remove(course);
        course.setInstructor(null);
    }

    public void addOrder(Order order) {
        orders.add(order);
        order.setUser(this);
    }

    public void removeOrder(Order order) {
        orders.remove(order);
        order.setUser(null);
    }

    public void addEnrollment(Enrollment enrollment) {
        enrollments.add(enrollment);
        enrollment.setUser(this);
    }

    public void removeEnrollment(Enrollment enrollment) {
        enrollments.remove(enrollment);
        enrollment.setUser(null);
    }
}