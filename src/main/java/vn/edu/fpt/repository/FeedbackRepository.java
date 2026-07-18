package vn.edu.fpt.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.stereotype.Repository;
import vn.edu.fpt.entity.Feedback;
import java.util.Optional;


import java.util.List;

@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, Integer> {
    boolean existsByUserIdAndCourseId(Integer userId, Integer courseId);
    Optional<Feedback> findByUserIdAndCourseId(Integer userId, Integer courseId);
    long countByRatingGreaterThanEqual(int rating);

    // Instructor course list: lấy đánh giá của đúng khóa học để màn "Xem đánh giá" hiển thị.
    @EntityGraph(attributePaths = "user")
    List<Feedback> findByCourseIdOrderByCreatedAtDesc(Integer courseId);
}
