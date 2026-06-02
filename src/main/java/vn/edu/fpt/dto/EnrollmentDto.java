package vn.edu.fpt.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EnrollmentDto {
    private Integer id;
    private CourseDto course;
    private UserDto user;
    private BigDecimal progressPercent;
}
