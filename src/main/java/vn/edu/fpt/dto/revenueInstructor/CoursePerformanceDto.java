package vn.edu.fpt.dto.revenueInstructor;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CoursePerformanceDto {
    private String name;
    private long salesCount;
    private int percent;
}
