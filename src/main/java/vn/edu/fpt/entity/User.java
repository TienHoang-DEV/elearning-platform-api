package vn.edu.fpt.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
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
public class User extends BaseEntity{

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    @Builder.Default
    private Set<Role> roles = new HashSet<>();

    @Column(columnDefinition = "Nvarchar(255)", nullable = false)
    private String firstName;

    @Column(columnDefinition = "Nvarchar(255)", nullable = false)
    private String lastName;

    @Column(length= 255, nullable = false, unique = true)
    private String email;

    @Column(length = 20, unique = true, nullable = true)
    private String phone;

    @Column(columnDefinition = "NVARCHAR(MAX)",nullable = true)
    private String bio;

    @Column(length = 255, nullable = true)
    private String passwordHash;

    @Column(length = 500, nullable = true)
    private String avatarUrl;

    private String googleId;

    @Column(length = 20, nullable = false)
    private String status;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Set<PasswordResetToken> passwordResetTokens = new HashSet<>();

<<<<<<< HEAD

    @Builder.Default
    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private Set<Order> orders = new HashSet<>();

    @Builder.Default
    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private Set<Enrollment> enrollments = new HashSet<>();

    public void addCourse(Course course) {
        courses.add(course);
        course.setInstructor(this);
=======
    public void addPasswordResetToken(PasswordResetToken passwordResetToken) {
        passwordResetTokens.add(passwordResetToken);
        passwordResetToken.setUser(this);
>>>>>>> feature/auth-backend-register-final
    }

    public void removePasswordResetToken(PasswordResetToken passwordResetToken) {
        passwordResetTokens.remove(passwordResetToken);
        passwordResetToken.setUser(null);
    }

}
