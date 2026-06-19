package vn.edu.fpt.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.edu.fpt.entity.CourseSection;

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

    @Query(""" 
         SELECT COALESCE(MAX(s.position), 0) from CourseSection s where s.course.id = :courseid 
          """)
    Integer FindMaxPositionByCourseId(@Param("courseid") Integer courseId);

    List<CourseSection> findByCourseId(Integer courseId);
}
