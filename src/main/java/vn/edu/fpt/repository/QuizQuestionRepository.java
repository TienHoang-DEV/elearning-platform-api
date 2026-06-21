package vn.edu.fpt.repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.edu.fpt.entity.QuizQuestion;

import java.util.List;

@Repository
public interface QuizQuestionRepository extends JpaRepository<QuizQuestion, Integer> {

    @Query("""
       select max(q.position)
       from QuizQuestion q
       where q.quiz.id = :quizId
       """)
    Integer findMaxPositionByQuizId(
            @Param("quizId") Integer quizId
    );

    Page<QuizQuestion> findAllByQuizId(Integer quizId, Pageable pageable);
}
