package vn.edu.fpt.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.edu.fpt.dto.revenue_manager.MonthlyRevenueForManagerDTO;
import vn.edu.fpt.entity.Payment;
import vn.edu.fpt.enums.PaymentStatus;
import vn.edu.fpt.dto.MonthlyRevenueDTO;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Integer> {

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.status = :status AND p.paidAt >= :startDate")
    BigDecimal sumAmountByStatusAndPaidAtAfter(@Param("status") PaymentStatus status, @Param("startDate") LocalDateTime startDate);

    @Query("SELECT new vn.edu.fpt.dto.MonthlyRevenueDTO(YEAR(p.paidAt), MONTH(p.paidAt), COALESCE(SUM(p.amount), 0)) " +
           "FROM Payment p " +
           "WHERE p.status = :status AND p.paidAt >= :startDate " +
           "GROUP BY YEAR(p.paidAt), MONTH(p.paidAt) " +
           "ORDER BY YEAR(p.paidAt) ASC, MONTH(p.paidAt) ASC")
    List<MonthlyRevenueDTO> getMonthlyRevenue(@Param("status") PaymentStatus status, @Param("startDate") LocalDateTime startDate);

    /**
     * Find payment by PayOS gateway order code
     */
    Optional<Payment> findByGatewayOrderCode(String gatewayOrderCode);

    @Query("""
      SELECT new vn.edu.fpt.dto.revenue_manager.MonthlyRevenueForManagerDTO(COALESCE(SUM(p.amount), 0))  FROM Payment p 
      WHERE p.status = vn.edu.fpt.enums.PaymentStatus.PAID
      AND CAST(p.paidAt as date) >= :startDate AND CAST(p.paidAt as date) <= :endDate 
""")
    MonthlyRevenueForManagerDTO getMonthlyRevenueTotal(@Param("startDate") LocalDate startDate,@Param("endDate") LocalDate endDate);
}
