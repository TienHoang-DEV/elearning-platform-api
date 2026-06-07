package vn.edu.fpt.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.edu.fpt.entity.Payment;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Integer> {

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.status = :status AND p.paidAt >= :startDate")
    BigDecimal sumAmountByStatusAndPaidAtAfter(@Param("status") String status, @Param("startDate") LocalDateTime startDate);

    @Query("SELECT YEAR(p.paidAt), MONTH(p.paidAt), COALESCE(SUM(p.amount), 0) " +
           "FROM Payment p " +
           "WHERE p.status = :status AND p.paidAt >= :startDate " +
           "GROUP BY YEAR(p.paidAt), MONTH(p.paidAt) " +
           "ORDER BY YEAR(p.paidAt) ASC, MONTH(p.paidAt) ASC")
    List<Object[]> getMonthlyRevenue(@Param("status") String status, @Param("startDate") LocalDateTime startDate);
}
