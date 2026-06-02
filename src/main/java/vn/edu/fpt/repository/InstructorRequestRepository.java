package vn.edu.fpt.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import vn.edu.fpt.entity.InstructorRequest;
import vn.edu.fpt.enums.InstructorRequestStatus;

@Repository
public interface InstructorRequestRepository extends JpaRepository<InstructorRequest, Integer> {

    /**
     * Tìm kiếm và lọc yêu cầu giảng viên theo keyword (tên, email) và trạng thái.
     * Khi status là null thì bỏ qua điều kiện lọc trạng thái.
     */
    @Query("SELECT r FROM InstructorRequest r " +
           "LEFT JOIN FETCH r.user u " +
           "WHERE (:status IS NULL OR r.status = :status) " +
           "AND (:keyword IS NULL OR :keyword = '' " +
           "     OR LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "     OR LOWER(u.firstName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "     OR LOWER(u.lastName) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<InstructorRequest> searchAndFilter(
            @Param("keyword") String keyword,
            @Param("status") InstructorRequestStatus status,
            Pageable pageable);
}
