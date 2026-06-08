package vn.edu.fpt.repository;

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
}
