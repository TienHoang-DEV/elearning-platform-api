package vn.edu.fpt.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.fpt.dto.ManagerDashboardDTO;
import vn.edu.fpt.dto.MonthlyRevenueDTO;
import vn.edu.fpt.enums.InstructorRequestStatus;
import vn.edu.fpt.enums.PaymentStatus;
import vn.edu.fpt.repository.CourseRepository;
import vn.edu.fpt.repository.FeedbackReportRepository;
import vn.edu.fpt.repository.InstructorRequestRepository;
import vn.edu.fpt.repository.PaymentRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class ManagerDashboardService {

    private final InstructorRequestRepository instructorRequestRepository;
    private final CourseRepository courseRepository;
    private final FeedbackReportRepository feedbackReportRepository;
    private final PaymentRepository paymentRepository;

    public ManagerDashboardService(
            InstructorRequestRepository instructorRequestRepository,
            CourseRepository courseRepository,
            FeedbackReportRepository feedbackReportRepository,
            PaymentRepository paymentRepository) {
        this.instructorRequestRepository = instructorRequestRepository;
        this.courseRepository = courseRepository;
        this.feedbackReportRepository = feedbackReportRepository;
        this.paymentRepository = paymentRepository;
    }

    public ManagerDashboardDTO getDashboardData() {
        ManagerDashboardDTO dto = new ManagerDashboardDTO();

        // 1. Count pending instructors
        long pendingInstructors = instructorRequestRepository.countByStatus(InstructorRequestStatus.PENDING);
        dto.setPendingInstructors(pendingInstructors);

        // 2. Count pending courses
        long pendingCourses = courseRepository.countByStatus("PENDING");
        dto.setPendingCourses(pendingCourses);

        // 3. Count pending feedbacks (unprocessed feedback reports)
        long pendingFeedbacks = feedbackReportRepository.countByStatus("PENDING");
        dto.setPendingFeedbacks(pendingFeedbacks);

        // 4. Calculate monthly revenue (total payment amount of status = SUCCESS in
        // this month)
        LocalDateTime startOfMonth = LocalDateTime.now()
                .with(TemporalAdjusters.firstDayOfMonth())
                .withHour(0).withMinute(0).withSecond(0).withNano(0);
        BigDecimal monthlyRevenue = paymentRepository.sumAmountByStatusAndPaidAtAfter(PaymentStatus.SUCCESS, startOfMonth);


        dto.setMonthlyRevenue(formatRevenue(monthlyRevenue));

        List<String> chartLabels = new ArrayList<>();
        List<BigDecimal> chartData = new ArrayList<>();
        java.time.YearMonth currentMonth = java.time.YearMonth.now();
        for (int i = 11; i >= 0; i--) {
            java.time.YearMonth m = currentMonth.minusMonths(i);
            chartLabels.add("Tháng " + m.getMonthValue());
            chartData.add(BigDecimal.ZERO);
        }

        LocalDateTime twelveMonthsAgo = LocalDateTime.now()
                .minusMonths(11)
                .with(TemporalAdjusters.firstDayOfMonth())
                .withHour(0).withMinute(0).withSecond(0).withNano(0);

        List<MonthlyRevenueDTO> rawChartData = paymentRepository.getMonthlyRevenue(PaymentStatus.SUCCESS, twelveMonthsAgo);
        for (MonthlyRevenueDTO row : rawChartData) {
            int year = row.year();
            int month = row.month();
            BigDecimal amount = row.revenue();

            for (int i = 0; i < 12; i++) {
                java.time.YearMonth m = currentMonth.minusMonths(11 - i);
                if (m.getYear() == year && m.getMonthValue() == month) {
                    chartData.set(i, amount);
                    break;
                }
            }
        }

        dto.setChartLabels(chartLabels);
        dto.setChartData(chartData);

        return dto;
    }

    private String formatRevenue(BigDecimal revenue) {
        if (revenue == null) {
            return "0 ₫";
        }
        java.text.DecimalFormat formatter = new java.text.DecimalFormat("#,###");
        java.text.DecimalFormatSymbols symbols = new java.text.DecimalFormatSymbols(new java.util.Locale("vi", "VN"));
        symbols.setGroupingSeparator('.');
        formatter.setDecimalFormatSymbols(symbols);
        return formatter.format(revenue) + " ₫";
    }
}
