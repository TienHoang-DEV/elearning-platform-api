package vn.edu.fpt.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.edu.fpt.entity.Quiz;

@Repository
public interface QuizRepository extends JpaRepository<Quiz, Integer> {
    @Query("""
            select distinct z from Quiz z 
            left join fetch z.questions q
            left join fetch q.answers a
            where z.lesson.id = :id
            """)
    Quiz findByLessonId(@Param("id") Integer lessonId);
}
