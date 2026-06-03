package vn.edu.fpt.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.edu.fpt.entity.CourseSection;
import vn.edu.fpt.entity.Lesson;

import java.util.List;
import java.util.Optional;

@Repository
public interface LessonRepository extends JpaRepository<Lesson, Integer> {
    Optional<Lesson> findFirstByCourseSection_IdOrderByPositionAscIdAsc(Integer sectionId);

    @Query("SELECT l FROM Lesson l LEFT JOIN FETCH l.materials WHERE l.id = :id")
    Optional<Lesson> findByIdWithMaterials(@Param("id") Integer id);

    @Query("SELECT DISTINCT l FROM Lesson l LEFT JOIN FETCH l.quizzes q LEFT JOIN FETCH q.questions qs LEFT JOIN FETCH qs.answers WHERE l.id = :id")
    Optional<Lesson> findByIdWithQuizzes(@Param("id") Integer id);

    Lesson getFinalCompledLessonIdByCourseSection(CourseSection courseSection);

    @Query("""
            select lp.lesson.id from LessonProgress lp left join lp.enrollment e
                        where e.course.id = :courseId and e.user.id = :userId
                                    and lp.completed = true 
                                    order by lp.lesson.id desc 
            """)
    List<Integer> getCompletedLessonIdByCourseIdAndUserId(@Param("courseId") Integer id, @Param("userId") Integer id1);

    @Query("""
            select l.courseSection.id from Lesson l where l.id = :id
            """)
    Integer findSectionIdByLessonId(@Param("id") Integer lessonIdFinalCompleted);

    @Query("""
                        select min(l.id) from Lesson l where l.courseSection.course.id = :id
            """)
    Integer findFirstLessonIdByCourseId(@Param("id") Integer attr2);
}
