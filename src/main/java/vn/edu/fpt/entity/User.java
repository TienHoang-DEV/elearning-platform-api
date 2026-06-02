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

    @ManyToOne
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    @Column(columnDefinition = "Nvarchar(255)", nullable = false)
    private String firstName;

    @Column(columnDefinition = "Nvarchar(255)", nullable = false)
    private String lastName;

    @Column(length= 255, nullable = false, unique = true)
    private String email;

    @Column(length = 20, unique = true, nullable = true)
    private String phone;

    @Column(length = 255, nullable = true)
    private String passwordHash;

    @Column(length = 500, nullable = true)
    private String avatarUrl;

    private String googleId;

    @Column(length = 20, nullable = false)
    private String status;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Set<PasswordResetToken> passwordResetTokens = new HashSet<>();

    public void addPasswordResetToken(PasswordResetToken passwordResetToken) {
        passwordResetTokens.add(passwordResetToken);
        passwordResetToken.setUser(this);
    }

    public void removePasswordResetToken(PasswordResetToken passwordResetToken) {
        passwordResetTokens.remove(passwordResetToken);
        passwordResetToken.setUser(null);
    }

}
