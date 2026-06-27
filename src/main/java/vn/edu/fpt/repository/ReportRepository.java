package vn.edu.fpt.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.edu.fpt.entity.Report;
import vn.edu.fpt.enums.ReportStatus;
import vn.edu.fpt.enums.ReportType;

@Repository
public interface ReportRepository extends JpaRepository<Report, Integer> {

    @Query("""
        SELECT r FROM Report r
        JOIN FETCH r.reporter
        LEFT JOIN FETCH r.reviewedBy
        WHERE (:status IS NULL OR r.status = :status)
          AND (:type IS NULL OR r.reportType = :type)
          AND (:keyword IS NULL OR :keyword = '' 
               OR LOWER(r.reasonType) LIKE LOWER(CONCAT('%', :keyword, '%'))
               OR LOWER(r.description) LIKE LOWER(CONCAT('%', :keyword, '%'))
               OR LOWER(r.reporter.firstName) LIKE LOWER(CONCAT('%', :keyword, '%'))
               OR LOWER(r.reporter.lastName) LIKE LOWER(CONCAT('%', :keyword, '%'))
              )
    """)
    Page<Report> searchReports(
            @Param("status") ReportStatus status,
            @Param("type") ReportType type,
            @Param("keyword") String keyword,
            Pageable pageable
    );
}
