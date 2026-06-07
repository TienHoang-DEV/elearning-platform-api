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
        // Đếm số lượng yêu cầu giảng viên đang ở trạng thái PENDING
        long pendingInstructors = instructorRequestRepository.countByStatus(InstructorRequestStatus.PENDING);
        dto.setPendingInstructors(pendingInstructors);
        //Đếm số lượng khóa học đang ở trạng thái "PENDING"
        long pendingCourses = courseRepository.countByStatus("PENDING");
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
        // --- Chuẩn bị dữ liệu cho biểu đồ (12 tháng gần nhất) ---
        List<String> chartLabels = new ArrayList<>();// nhãn: "Tháng X"
        List<BigDecimal> chartData = new ArrayList<>();// giá trị doanh thu tương ứng
        java.time.YearMonth currentMonth = java.time.YearMonth.now();//lấy tháng hiện tại
        // Khởi tạo 12 nhãn và set mặc định giá trị = 0
        for (int i = 11; i >= 0; i--) {
            java.time.YearMonth m = currentMonth.minusMonths(i);
            chartLabels.add("Tháng " + m.getMonthValue());
            chartData.add(BigDecimal.ZERO);
        }
        // Xác định mốc 12 tháng trước (bắt đầu tháng)
        LocalDateTime twelveMonthsAgo = LocalDateTime.now()
                .minusMonths(11)
                .with(TemporalAdjusters.firstDayOfMonth())
                .withHour(0).withMinute(0).withSecond(0).withNano(0);
        // Lấy danh sách doanh thu theo tháng
        List<MonthlyRevenueDTO> rawChartData = paymentRepository.getMonthlyRevenue(PaymentStatus.SUCCESS, twelveMonthsAgo);
        // Duyệt dữ liệu thô và gán giá trị vào đúng vị trí trong chartData
        for (MonthlyRevenueDTO row : rawChartData) {
            int year = row.getYear();
            int month = row.getMonth();
            BigDecimal amount = row.getRevenue();
            // Tìm vị trí tương ứng trong mảng 12 tháng đã khởi tạo phía trên
            for (int i = 0; i < 12; i++) {
                java.time.YearMonth m = currentMonth.minusMonths(11 - i);
                // Nếu cùng năm và cùng tháng thì set giá trị vào chartData
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
    //hàm format tiền
    private String formatRevenue(BigDecimal revenue) {
        if (revenue == null) {
            return "0 đ";
        }
        java.text.DecimalFormat formatter = new java.text.DecimalFormat("#,###");
        java.text.DecimalFormatSymbols symbols = new java.text.DecimalFormatSymbols(new java.util.Locale("vi", "VN"));
        symbols.setGroupingSeparator('.');
        formatter.setDecimalFormatSymbols(symbols);
        return formatter.format(revenue) + " đ";
    }
}
