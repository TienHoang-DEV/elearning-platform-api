package vn.edu.fpt.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.fpt.dto.ManagerDashboardDTO;
import vn.edu.fpt.dto.MonthlyRevenueDTO;
import vn.edu.fpt.enums.CourseStatus;
import vn.edu.fpt.enums.PaymentStatus;
import vn.edu.fpt.repository.CourseRepository;
import vn.edu.fpt.repository.FeedbackReportRepository;
import vn.edu.fpt.repository.PaymentRepository;
import vn.edu.fpt.repository.UserRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class ManagerDashboardService {

    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final FeedbackReportRepository feedbackReportRepository;
    private final PaymentRepository paymentRepository;

    public ManagerDashboardService(
            UserRepository userRepository,
            CourseRepository courseRepository,
            FeedbackReportRepository feedbackReportRepository,
            PaymentRepository paymentRepository) {
        this.userRepository = userRepository;
        this.courseRepository = courseRepository;
        this.feedbackReportRepository = feedbackReportRepository;
        this.paymentRepository = paymentRepository;
    }

    public ManagerDashboardDTO getDashboardData() {
        ManagerDashboardDTO dto = new ManagerDashboardDTO();
        // Đếm tổng số lượng giảng viên
        long totalInstructors = userRepository.countInstructors();
        dto.setTotalInstructors(totalInstructors);
        //Đếm số lượng khóa học đang ở trạng thái "PENDING"
        long pendingCourses = courseRepository.countByStatus(CourseStatus.PENDING);
        dto.setPendingCourses(pendingCourses);
        //Đếm số lượng phản hồi/chủ đề đang ở trạng thái "PENDING"
        long pendingFeedbacks = feedbackReportRepository.countByStatus("PENDING");
        dto.setPendingFeedbacks(pendingFeedbacks);
        //Tính doanh thu từ đầu tháng đến thời điểm hiện tại (status = SUCCESS)
        LocalDateTime startOfMonth = LocalDateTime.now()
                .with(TemporalAdjusters.firstDayOfMonth())
                .withHour(0).withMinute(0).withSecond(0).withNano(0);//set ngày đầu tiên vd: 01/06/2026 00:00:00
        BigDecimal monthlyRevenue = paymentRepository.sumAmountByStatusAndPaidAtAfter(PaymentStatus.SUCCESS, startOfMonth);
        dto.setMonthlyRevenue(formatRevenue(monthlyRevenue));
        // --- Chuẩn bị dữ liệu cho biểu đồ (12 tháng trong năm hiện tại) ---
        List<String> chartLabels = new ArrayList<>();// nhãn: "Tháng 1" -> "Tháng 12"
        List<BigDecimal> chartData = new ArrayList<>();// giá trị doanh thu tương ứng
        int currentYear = LocalDateTime.now().getYear();

        // Khởi tạo 12 nhãn từ Tháng 1 đến Tháng 12 và set mặc định giá trị = 0
        for (int i = 1; i <= 12; i++) {
            chartLabels.add("Tháng " + i);
            chartData.add(BigDecimal.ZERO);
        }

        // Xác định mốc đầu năm hiện tại (01/01/năm hiện tại 00:00:00)
        LocalDateTime startOfYear = LocalDateTime.now()
                .with(TemporalAdjusters.firstDayOfYear())
                .withHour(0).withMinute(0).withSecond(0).withNano(0);

        // Lấy danh sách doanh thu theo tháng từ đầu năm hiện tại
        List<MonthlyRevenueDTO> rawChartData = paymentRepository.getMonthlyRevenue(PaymentStatus.SUCCESS, startOfYear);

        // Duyệt dữ liệu thô và gán giá trị vào đúng vị trí trong chartData (chỉ cho năm hiện tại)
        for (MonthlyRevenueDTO row : rawChartData) {
            int year = row.getYear();
            int month = row.getMonth();
            BigDecimal amount = row.getRevenue();

            if (year == currentYear && month >= 1 && month <= 12) {
                chartData.set(month - 1, amount);
            }
        }

        dto.setChartLabels(chartLabels);
        dto.setChartData(chartData);

        return dto;
    }
    //hàm format tiền
    private String formatRevenue(BigDecimal revenue) {
        if (revenue == null) {
            return "0 đ";
        }
        java.text.DecimalFormat formatter = new java.text.DecimalFormat("#,###");
        java.text.DecimalFormatSymbols symbols = new java.text.DecimalFormatSymbols(java.util.Locale.of("vi", "VN"));
        symbols.setGroupingSeparator('.');
        formatter.setDecimalFormatSymbols(symbols);
        return formatter.format(revenue) + " đ";
    }
}
