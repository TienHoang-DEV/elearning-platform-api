package vn.edu.fpt.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.edu.fpt.entity.Lesson;

import java.util.Optional;

@Repository
public interface LessonRepository extends JpaRepository<Lesson, Integer> {
    Optional<Lesson> findFirstByCourseSection_IdOrderByPositionAscIdAsc(Integer sectionId);

    @Query("SELECT l FROM Lesson l LEFT JOIN FETCH l.materials WHERE l.id = :id")
    Optional<Lesson> findByIdWithMaterials(@Param("id") Integer id);

    @Query("SELECT DISTINCT l FROM Lesson l LEFT JOIN FETCH l.quizzes q LEFT JOIN FETCH q.questions qs LEFT JOIN FETCH qs.answers WHERE l.id = :id")
    Optional<Lesson> findByIdWithQuizzes(@Param("id") Integer id);
}
