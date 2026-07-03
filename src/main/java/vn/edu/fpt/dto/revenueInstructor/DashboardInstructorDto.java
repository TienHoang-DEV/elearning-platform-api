package vn.edu.fpt.dto.revenueInstructor;

import com.azure.core.annotation.Get;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class DashboardInstructorDto {
    private BigDecimal totalRevenue;
    private BigDecimal revenueDeltaPercent;

    private long totalOrders;
    private BigDecimal ordersDeltaPercent;


    private long totalStudents;
    private BigDecimal studentsDeltaPercent;

    private long totalCourse;
    private long newCoursesInPeriod;


    private List<String> revenueTrendLabels;
    private List<BigDecimal> revenueTrendValues;

    private List<CourseRevenueDto> topSellingCourses;
    private List<CoursePerformanceDto> coursePerformance;
    private List<RecentOrderDto> recentOrders;

    private String chartPoints;
}
