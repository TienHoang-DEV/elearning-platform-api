package vn.edu.fpt.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.edu.fpt.entity.Quiz;

import java.util.List;

@Repository
public interface QuizRepository extends JpaRepository<Quiz, Integer> {
    @Query("""
            select distinct z from Quiz z 
            left join fetch z.questions q
            left join fetch q.answers a
            where z.lesson.id = :id
            """)
    List<Quiz> findByLessonId(@Param("id") Integer lessonId);

    Quiz findQuizById(Integer quizId);

    @Query("""
    SELECT q
    FROM Quiz q
    WHERE q.lesson.id = :lessonId
      AND (:status = 'ALL' OR q.status = :status)
""")
    Page<Quiz> findByLessonIdAndStatus(
            @Param("lessonId") Integer lessonId,
            @Param("status") String status,
            Pageable pageable);

    Long countByLessonId(Integer lessonId);
    Long countByLessonIdAndStatus(Integer lessonId, String status);

}
