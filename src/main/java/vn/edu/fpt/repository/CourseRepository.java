package vn.edu.fpt.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.edu.fpt.dto.course.CourseGrantDTO;
import vn.edu.fpt.entity.Course;
import vn.edu.fpt.entity.User;
import vn.edu.fpt.enums.CourseStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import vn.edu.fpt.dto.course.CourseListDto;
import vn.edu.fpt.dto.revenue_manager.InstructorCourseRevenueDTO;

@Repository
public interface CourseRepository extends JpaRepository<Course, Integer>, CourseRepositoryCustom {

    //Luu khoá học
    Course save(Course course);
    // Tìm danh sách các khóa học theo trạng thái
    List<Course> findByStatus(CourseStatus status);


    //Xoá Khoa Hoc
    void deleteCourseById(Integer id);

    //Kiểm tra không title trùng
    boolean existsByInstructorAndTitle(User instructor, String title);


    //Phân trang khoá học của mỗi instructor
    Page<Course> findByInstructorAndStatus(User instructor, Pageable pageable, CourseStatus courseStatus);


    long countByStatus(CourseStatus status);

    // Tìm các khóa học có tên chứa từ khóa tìm kiếm (không phân biệt hoa thường)
    List<Course> findByTitleContainingIgnoreCase(String title);

    Optional<Course> findFirstByOrderByIdAsc();

    @Query("""
          select c distinct from Course c
          LEFT JOIN FETCH c.category ca
          LEFT JOIN FETCH c.sections s
          LEFT JOIN FETCH s.lessons l
          LEFT JOIN FETCH l.materials m
          WHERE c.id = :courseId
          """)
    Course findDetailById(@Param("courseId") Integer courseId);

    //tổng số khoá học đã publish (tính cộng dồn, không lọc theo kỳ)
    @Query("""
           select count(c) from Course c
           where c.instructor.id = :instructorId
           and c.status = 'PUBLISHED'
           """)
    long countPublishedCourse(@Param("instructorId") Integer instructorId,
                              @Param("fromDate") LocalDateTime fromDate,
                              @Param("toDate") LocalDateTime toDate);


    //tổng số khoá học mơi
    @Query("""
           select count(c) from Course c
           where c.instructor.id = :instructorId
           and c.createdAt between :fromDate and :toDate
           """)
    long countNewCourse(@Param("instructorId") Integer instructorId,
                        @Param("fromDate") LocalDateTime fromDate,
                        @Param("toDate") LocalDateTime toDate);

    @Query("""
            select distinct c from Course c left join fetch c.sections s left join fetch s.lessons where c.id = :id
            """)
    Optional<Course> findByIdWithSectionsAndLessons(@Param("id") Integer id);

    @Query("select count(s) from CourseSection s where s.course.id = :courseId")
    long countSectionsByCourseId(@Param("courseId") Integer courseId);

    @Query("""
            select count(s) from CourseSection s
            where s.course.id = :courseId
            and not exists (
                select l.id from Lesson l where l.courseSection.id = s.id
            )
            """)
    long countSectionsWithoutLessons(@Param("courseId") Integer courseId);

    @Query("""
            select count(l) from Lesson l
            where l.courseSection.course.id = :courseId
            """)
    long countLessonsByCourseId(@Param("courseId") Integer courseId);

    @Query("""
            select count(distinct l.id) from Lesson l
            left join l.materials m
            where l.courseSection.course.id = :courseId
            and (
                (l.videoUrl is not null and trim(l.videoUrl) <> '')
                or m.id is not null
            )
            """)
    long countLessonsHavingVideoOrMaterial(@Param("courseId") Integer courseId);

    @Query("""
            select count(q) from Quiz q
            where q.lesson.courseSection.course.id = :courseId
            """)
    long countQuizzesByCourseId(@Param("courseId") Integer courseId);

    @Query("""
            select distinct c from Course c 
            left join fetch c.instructor i 
            left join fetch c.category cat 
            left join fetch c.sections s 
            left join fetch s.lessons 
            where c.id = :id
            """)
    Optional<Course> findByIdWithDetails(@Param("id") Integer id);

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

    @Query("SELECT new vn.edu.fpt.dto.course.CourseListDto(c.id, c.title, c.thumbnailUrl, c.price, c.level, i.firstName, i.lastName, cat.id, cat.name, " +
            "COALESCE((SELECT AVG(f.rating) FROM Feedback f WHERE f.course.id = c.id), 0.0), " +
            "(SELECT COUNT(f.id) FROM Feedback f WHERE f.course.id = c.id), " +
            "(SELECT COUNT(l.id) FROM CourseSection cs JOIN cs.lessons l WHERE cs.course.id = c.id), " +
            "(SELECT COUNT(e.id) FROM Enrollment e WHERE e.course.id = c.id)) " +
            "FROM Course c JOIN c.instructor i JOIN c.category cat " +
            "WHERE c.status = vn.edu.fpt.enums.CourseStatus.PUBLISHED " +
            "AND cat.id IN :categoryIds " +
            "ORDER BY COALESCE((SELECT AVG(f.rating) FROM Feedback f WHERE f.course.id = c.id), 0.0) DESC, c.id DESC")
    List<CourseListDto> findTop4ByCategoryIdsOrderByAverageRatingDesc(@Param("categoryIds") List<Integer> categoryIds, Pageable pageable);


    @Query("""
        select new vn.edu.fpt.dto.course.CourseGrantDTO(c.id, c.title) from Course c 
""")
    List<CourseGrantDTO> findAllCourseGrantDTO();

    @Query("""
        select new vn.edu.fpt.dto.course.CourseGrantDTO(c.id, c.title) from Course c 
        where c.id not in (
            select e.course.id from Enrollment e where e.user.id = :userId
        )
""")
    List<CourseGrantDTO> findAvailableCoursesForUser(@Param("userId") Integer userId);

    @Query("""
        SELECT new vn.edu.fpt.dto.revenue_manager.InstructorCourseRevenueDTO(
            c.id, c.title, c.price,
            COALESCE(SUM(CASE WHEN o.status = vn.edu.fpt.enums.OrderStatus.PAID OR o.status = vn.edu.fpt.enums.OrderStatus.COMPLETED THEN 1 ELSE 0 END), 0),
            COALESCE(SUM(CASE WHEN o.status = vn.edu.fpt.enums.OrderStatus.PAID OR o.status = vn.edu.fpt.enums.OrderStatus.COMPLETED THEN oi.priceSnapshot ELSE 0 END), 0)
        )
        FROM Course c
        LEFT JOIN OrderItem oi ON oi.course.id = c.id
        LEFT JOIN oi.order o
        WHERE c.instructor.id = :instructorId
        GROUP BY c.id, c.title, c.price
        ORDER BY COALESCE(SUM(CASE WHEN o.status = vn.edu.fpt.enums.OrderStatus.PAID OR o.status = vn.edu.fpt.enums.OrderStatus.COMPLETED THEN oi.priceSnapshot ELSE 0 END), 0) DESC
    """)
    List<InstructorCourseRevenueDTO> getCourseRevenueStatsByInstructor(@Param("instructorId") Integer instructorId);
}



