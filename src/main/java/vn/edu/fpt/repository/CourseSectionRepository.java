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
public interface CourseSectionRepository extends JpaRepository<CourseSection, Integer> {
    Optional<CourseSection> findFirstByCourse_IdOrderByPositionAscIdAsc(Integer courseId);

    CourseSection save(CourseSection courseSection);

    @Query("""
    select cs.course.id from CourseSection cs where cs.id = :id
""")
    Optional<Integer> findBySectionId(@Param("id") Integer sectionId);

    CourseSection findCourseSectionById(Integer courseSectionId);

    @Query(""" 
         SELECT COALESCE(MAX(s.position), 0) from CourseSection s where s.course.id = :courseid
          """)
    Integer FindMaxPositionByCourseId(@Param("courseid") Integer courseId);

    List<CourseSection> findByCourseId(Integer courseId);

    @Query("""
           select c from CourseSection c LEFT JOIN FETCH c.lessons l where c.course.id = :courseId Order by c.position 
           """)
    List<CourseSection> findByCourseAndLesson(@Param("courseId") Integer courseId);
}
