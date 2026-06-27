package vn.edu.fpt.dto.user;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentProfileDashboardDto {
    private UserDto currentUser;
    private int enrollmentsCount;
    private long certificatesCount;
    private int studyHours;
}
