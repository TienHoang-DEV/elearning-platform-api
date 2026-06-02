package vn.edu.fpt.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
public class UserDto {
    private Integer id;
    private String firstName;
    private String lastName;
    private String email;
    private String avatarUrl;
    private String bio;
    private String phone;
}
