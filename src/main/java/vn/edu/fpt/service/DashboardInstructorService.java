package vn.edu.fpt.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import vn.edu.fpt.dto.revenueInstructor.CoursePerformanceDto;
import vn.edu.fpt.dto.revenueInstructor.DashboardInstructorDto;
import vn.edu.fpt.repository.CourseRepository;
import vn.edu.fpt.repository.EnrollmentRepository;
import vn.edu.fpt.repository.OrderItemRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardInstructorService {
    private final CourseRepository courseRepository;
    private final OrderItemRepository orderItemRepository;
    private final EnrollmentRepository enrollmentRepository;

    public DashboardInstructorDto getStats(Integer instructorId, String period, Integer year, Integer month) {

        LocalDateTime[] current = resolveDateRange(period, year, month);
        LocalDateTime[] previous = previousRange(current);

        DashboardInstructorDto dto = new DashboardInstructorDto();


        BigDecimal curRevenue = orDefault(orderItemRepository.sumTotalRevenueByInstructor(instructorId, current[0], current[1]));
        BigDecimal preRevenue = orDefault(orderItemRepository.sumTotalRevenueByInstructor(instructorId, previous[0], previous[1]));
        dto.setTotalRevenue(curRevenue);
        dto.setRevenueDeltaPercent(percentChange(curRevenue, preRevenue));

        long curOrders = orDefaultLong(orderItemRepository.countOrder(instructorId, current[0], current[1]));
        long prevOrders = orDefaultLong(orderItemRepository.countOrder(instructorId, previous[0], previous[1]));
        dto.setTotalOrders(curOrders);
        dto.setOrdersDeltaPercent(percentChange(BigDecimal.valueOf(curOrders), BigDecimal.valueOf(prevOrders)));

        long curStudents = enrollmentRepository.countDistictStudents(instructorId, current[0], current[1]);
        long prevStudents = enrollmentRepository.countDistictStudents(instructorId, previous[0], previous[1]);
        dto.setTotalStudents(curStudents);
        dto.setStudentsDeltaPercent(percentChange(BigDecimal.valueOf(curStudents), BigDecimal.valueOf(prevStudents)));


        long totalCourse = courseRepository.countPublishedCourse(instructorId, current[0], current[1]);
        long newCourses = courseRepository.countNewCourse(instructorId, current[0], current[1]);
        dto.setTotalCourse(totalCourse);
        dto.setNewCoursesInPeriod(newCourses);


        dto.setTopSellingCourses(
                orderItemRepository.topSellingCourse(instructorId, current[0], current[1], PageRequest.of(0, 4))
        );

        List<CoursePerformanceDto> perf =
                orderItemRepository.coursePerformance(instructorId, current[0], current[1], PageRequest.of(0, 4));
        long maxSales = perf.stream().mapToLong(CoursePerformanceDto::getSalesCount).max().orElse(1);
        perf.forEach(p -> p.setPercent(maxSales == 0 ? 0 : (int) (p.getSalesCount() * 100 / maxSales)));
        dto.setCoursePerformance(perf);

        dto.setRecentOrders(orderItemRepository.recentOrder(instructorId, PageRequest.of(0, 5)));

        List<Object[]> raw = orderItemRepository.revenueTrend(instructorId, current[0], current[1]);
        List<String> labels = raw.stream()
                .map(r -> {
                    Object dateObj = r[0];
                    LocalDate localDate;
                    if (dateObj instanceof LocalDate) {
                        localDate = (LocalDate) dateObj;
                    } else if (dateObj instanceof Date) {
                        localDate = ((Date) dateObj).toLocalDate();
                    } else if (dateObj instanceof Timestamp) {
                        localDate = ((Timestamp) dateObj).toLocalDateTime().toLocalDate();
                    } else if (dateObj instanceof LocalDateTime) {
                        localDate = ((LocalDateTime) dateObj).toLocalDate();
                    } else {
                        throw new IllegalArgumentException("Unsupported date type: " + dateObj.getClass());
                    }
                    return localDate.format(DateTimeFormatter.ofPattern("dd/MM"));
                })
                .toList();
        List<BigDecimal> values = raw.stream()
                .map(r -> (BigDecimal) r[1]).toList();
        dto.setRevenueTrendLabels(labels);
        dto.setRevenueTrendValues(values);
        dto.setChartPoints(buildChartPoints(values));

        return dto;
    }

    private long orDefaultLong(Long value) {
        return value != null ? value : 0L;
    }

    private BigDecimal orDefault(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }

    private String buildChartPoints(List<BigDecimal> values) {
        if (values == null || values.isEmpty()) return "0,220 700,220";

        double max = values.stream().mapToDouble(BigDecimal::doubleValue).max().orElse(1);
        if (max == 0) max = 1;

        int width = 700, height = 220;
        int n = values.size();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n; i++) {
            double x = n == 1 ? 0 : (double) width * i / (n - 1);
            double y = height - (values.get(i).doubleValue() / max * height);
            sb.append((int) x).append(",").append((int) y);
            if (i < n - 1) sb.append(" ");
        }
        return sb.toString();
    }

    private LocalDateTime[] resolveDateRange(String period, Integer year, Integer month) {
        LocalDate now = LocalDate.now();
        return switch (period) {
            // BUG cũ: end date là 31/12 (hết năm) thay vì hết THÁNG -> đã sửa lại
            case "MONTH" -> new LocalDateTime[]{
                    now.withDayOfMonth(1).atStartOfDay(),
                    now.withDayOfMonth(now.lengthOfMonth()).atTime(23, 59, 59)
            };

            case "YEAR" -> new LocalDateTime[]{
                    now.withDayOfYear(1).atStartOfDay(),
                    LocalDate.of(now.getYear(), 12, 31).atTime(23, 59, 59)
            };

            case "CUSTOM" -> {
                LocalDate d = LocalDate.of(year, month, 1);
                yield new LocalDateTime[]{
                        d.withDayOfMonth(1).atStartOfDay(),
                        d.withDayOfMonth(d.lengthOfMonth()).atTime(23, 59, 59)
                };
            }

            default -> new LocalDateTime[]{
                    LocalDateTime.of(2000, 1, 1, 0, 0),
                    LocalDateTime.now()
            };
        };
    }

    private LocalDateTime[] previousRange(LocalDateTime[] current) {
        long days = ChronoUnit.DAYS.between(current[0], current[1]) + 1;
        return new LocalDateTime[]{
                current[0].minusDays(days),
                current[0].minusSeconds(1)
        };
    }

    private BigDecimal percentChange(BigDecimal current, BigDecimal previous) {
        if (previous == null || previous.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        if (current == null) current = BigDecimal.ZERO;
        return current.subtract(previous)
                .multiply(BigDecimal.valueOf(100))
                .divide(previous, 2, RoundingMode.HALF_UP);
    }
}