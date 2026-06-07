package vn.edu.fpt.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.edu.fpt.entity.FeedbackReport;
@Repository
public interface FeedbackReportRepository extends JpaRepository<FeedbackReport, Integer> {
    long countByStatus(String status);
}
