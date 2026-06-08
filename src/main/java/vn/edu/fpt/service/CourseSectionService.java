package vn.edu.fpt.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.fpt.entity.Course;
import vn.edu.fpt.entity.CourseSection;
import vn.edu.fpt.exception.CourseNotFoundException;
import vn.edu.fpt.exception.ResourceNotFoundException;
import vn.edu.fpt.repository.CourseSectionRepository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.Map;
import java.util.HashMap;
import vn.edu.fpt.dto.CourseSectionDto;
import vn.edu.fpt.dto.LessonDto;

@Service
@Transactional
public class CourseSectionService {
    private final CourseSectionRepository repository;

    public CourseSectionService(CourseSectionRepository courseSectionRepository) {
        this.repository = courseSectionRepository;
    }

    public List<CourseSection> findAll() {
        return repository.findAll();
    }

    public Optional<CourseSection> findById(Integer id) {
        return repository.findById(id);
    }

    public CourseSection save(CourseSection entity) {
        return repository.save(entity);
    }

    public void deleteById(Integer id) {
        repository.deleteById(id);
    }

    public boolean existsById(Integer id) {
        return repository.existsById(id);
    }

    public Set<CourseSection> findSectionsByCourse(Course course) {
        if (course.getSections() == null || course.getSections().isEmpty()) {
            throw new CourseNotFoundException("Khóa học không có section nào");
        }
        return course.getSections();
    }

    public Integer findCourseIdBySectionId(Integer sectionId) {
        return repository.findBySectionId(sectionId).orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy Khóa học nào với Section id là " + sectionId));
    }

    public Map<Integer, Boolean> getSectionCompletedMap(Set<CourseSectionDto> sections, List<Integer> completedLessonIds) {
        Map<Integer, Boolean> sectionCompletedMap = new HashMap<>();
        if (sections != null) {
            for (CourseSectionDto sectionDto : sections) {
                boolean allCompleted = true;
                if (sectionDto.getLessons() == null || sectionDto.getLessons().isEmpty()) {
                    allCompleted = false;
                } else {
                    for (LessonDto l : sectionDto.getLessons()) {
                        if (completedLessonIds == null || !completedLessonIds.contains(l.getId())) {
                            allCompleted = false;
                            break;
                        }
                    }
                }
                sectionCompletedMap.put(sectionDto.getId(), allCompleted);
            }
        }
        return sectionCompletedMap;
    }
}
