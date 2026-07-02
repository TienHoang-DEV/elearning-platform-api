package vn.edu.fpt.dto.revenueInstructor;

import java.math.BigDecimal;
import java.util.List;

public class DashboardInstructorDto {
    private BigDecimal totalRevenue;
    private BigDecimal revenueDeltaPercent;

    private long totalOrders;
    private BigDecimal ordersDeltaPercent;

    private long totalCourse;
    private long newCoursesInPeriod;


    private List<String> revenueTrendLabels;
    private List<BigDecimal> revenueTrendValues;

    private List<CourseRevenueDto> topSellingCourses;
    private List<CoursePerformanceDto> coursePerformance;
    private List<RecentOrderDto> recentOrders;
}
