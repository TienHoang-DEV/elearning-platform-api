package vn.edu.fpt.service;

import org.junit.jupiter.api.Test;
import vn.edu.fpt.dto.revenue_manager.MonthlyRevenueForManagerDTO;
import vn.edu.fpt.repository.CourseRepository;
import vn.edu.fpt.repository.FeedbackReportRepository;
import vn.edu.fpt.repository.PaymentRepository;
import vn.edu.fpt.repository.UserRepository;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ManagerDashboardServiceTest {

    @Test
    void getMonthlyRevenueForManagerCalculatesRevenueFromMonthlyTotal() {
        PaymentRepository paymentRepository = mock(PaymentRepository.class);
        when(paymentRepository.getMonthlyRevenueTotal(any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(new MonthlyRevenueForManagerDTO(BigDecimal.valueOf(6000)));

        ManagerDashboardService service = new ManagerDashboardService(
                mock(UserRepository.class),
                mock(CourseRepository.class),
                mock(FeedbackReportRepository.class),
                paymentRepository
        );

        MonthlyRevenueForManagerDTO result = service.getMonthlyRevenueForManager();

        assertEquals(0, BigDecimal.valueOf(6000).compareTo(result.getMonthlyRevenue()));
        assertEquals(0, BigDecimal.valueOf(4200).compareTo(result.getInstructorRevenue()));
        assertEquals(0, BigDecimal.valueOf(1800).compareTo(result.getPlatformRevenue()));
    }
}
