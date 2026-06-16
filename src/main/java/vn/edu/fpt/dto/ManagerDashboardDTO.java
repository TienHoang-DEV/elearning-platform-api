package vn.edu.fpt.dto;

import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
public class ManagerDashboardDTO {
    private long totalInstructors;
    private long pendingCourses;
    private long pendingFeedbacks;
    private String monthlyRevenue;
    private List<String> chartLabels;
    private List<BigDecimal> chartData;
}
