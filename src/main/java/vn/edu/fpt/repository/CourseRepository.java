package vn.edu.fpt.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.edu.fpt.entity.Course;
import java.util.List;
import java.util.Optional;

@Repository
public interface CourseRepository extends JpaRepository<Course, Integer> {
    // Tìm danh sách các khóa học theo trạng thái
    List<Course> findByStatus(String status);

    // Tìm các khóa học có tên chứa từ khóa tìm kiếm (không phân biệt hoa thường)
    List<Course> findByTitleContainingIgnoreCase(String title);

    Optional<Course> findFirstByOrderByIdAsc();

    @Query("""
            select distinct c from Course c left join fetch c.sections s left join fetch s.lessons where c.id = :id
            """)
    Optional<Course> findByIdWithSectionsAndLessons(@Param("id") Integer id);

    @Query("""
            select distinct c from Course c left join c.enrollments e
                        left join e.lessonProgresses
                                    where c.id = :id
            """)
    Optional<Course> findByIdWithEnrollmentAndLessonProgress(@Param("id") Integer courseId);

    @Query("SELECT c FROM Course c " +
           "LEFT JOIN FETCH c.instructor i " +
           "WHERE (:status IS NULL OR :status = '' OR c.status = :status) " +
           "AND (:keyword IS NULL OR :keyword = '' OR LOWER(c.title) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Course> searchAndFilter(
            @Param("keyword") String keyword,
            @Param("status") String status,
            Pageable pageable);

    long countByStatus(String status);
}
