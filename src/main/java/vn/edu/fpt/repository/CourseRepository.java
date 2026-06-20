package vn.edu.fpt.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.edu.fpt.entity.Course;
import vn.edu.fpt.entity.User;
import vn.edu.fpt.enums.CourseStatus;

import java.util.List;
import java.util.Optional;

@Repository
public interface CourseRepository extends JpaRepository<Course, Integer> {

    //Luu khoá học
    Course save(Course course);
    // Tìm danh sách các khóa học theo trạng thái
    List<Course> findByStatus(CourseStatus status);


    //Kiểm tra không title trùng
    boolean existsByInstructorAndTitle(User instructor, String title);


    //Phân trang khoá học của mỗi instructor
    Page<Course> findByInstructorAndStatus(User instructor, Pageable pageable, CourseStatus courseStatus);


    long countByStatus(CourseStatus status);

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

    List<Course> findByInstructorAndStatus(User user, CourseStatus status);
    List<Course> findByInstructor(User instructor);

    @Query("""
            select c from Course c join c.enrollments e where e.user.id = :userId and c.id = :courseId
            """)
    Optional<Course> findByCourseIdAndUserId(@Param("courseId") Integer courseId, @Param("userId") Integer userId);

    @Query("SELECT c FROM Course c " +
            "LEFT JOIN FETCH c.instructor i " +
            "WHERE (:status IS NULL OR c.status = :status) " +
            "AND (:categoryId IS NULL OR c.category.id = :categoryId) " +
            "AND (:keyword IS NULL OR :keyword = '' OR LOWER(c.title) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Course> searchAndFilter(
            @Param("keyword") String keyword,
            @Param("status") CourseStatus status,
            @Param("categoryId") Integer categoryId,
            Pageable pageable);

    Course findCourseById(Integer id);
}



