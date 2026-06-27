package vn.edu.fpt.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.edu.fpt.entity.Feedback;
@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, Integer> {
    boolean existsByUserIdAndCourseId(Integer userId, Integer courseId);
    long countByRatingGreaterThanEqual(int rating);
}
