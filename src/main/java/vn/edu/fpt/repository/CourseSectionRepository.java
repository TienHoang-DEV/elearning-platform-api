package vn.edu.fpt.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.edu.fpt.entity.CourseSection;

import java.util.Optional;

@Repository
public interface CourseSectionRepository extends JpaRepository<CourseSection, Integer> {
    Optional<CourseSection> findFirstByCourse_IdOrderByPositionAscIdAsc(Integer courseId);

    @Query("""
    select cs.course.id from CourseSection cs where cs.id = :id
""")
    Optional<Integer> findBySectionId(@Param("id") Integer sectionId);

    CourseSection findCourseSectionById(Integer courseSectionId);
}
