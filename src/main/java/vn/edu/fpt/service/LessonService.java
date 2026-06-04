package vn.edu.fpt.service;

import jakarta.persistence.criteria.CriteriaBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.fpt.entity.Course;
import vn.edu.fpt.entity.CourseSection;
import vn.edu.fpt.entity.Lesson;
import vn.edu.fpt.exception.CourseNotFoundException;
import vn.edu.fpt.exception.ResourceNotFoundException;
import vn.edu.fpt.repository.LessonRepository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@Transactional
public class LessonService {
    private final LessonRepository repository;

    public LessonService(LessonRepository lessonRepository) {
        this.repository = lessonRepository;
    }

    public List<Lesson> findAll() {
        return repository.findAll();
    }

    public Optional<Lesson> findById(Integer id) {
        return repository.findById(id);
    }

    public Lesson findByIdWithMaterials(Integer id) {
        return repository.findByIdWithMaterials(id).orElseThrow(() -> new CourseNotFoundException("Bài học không tìm thấy"));
    }

    public Lesson findByIdWithQuizzes(Integer id) {
        return repository.findByIdWithQuizzes(id).orElseThrow(() -> new ResourceNotFoundException("Lesson with id " + id + " not found"));
    }

    public Lesson save(Lesson entity) {
        return repository.save(entity);
    }

    public void deleteById(Integer id) {
        repository.deleteById(id);
    }

    public boolean existsById(Integer id) {
        return repository.existsById(id);
    }

    public Set<Lesson> findLessonByCourseSection(CourseSection courseSection) {
        if (courseSection.getLessons() == null || courseSection.getLessons().isEmpty()) {
            throw new CourseNotFoundException("Section không có bài học nào");
        }
        return courseSection.getLessons();
    }

    public Integer findLessonIdFinalCompletedByCourseIdAndUserId(Integer id, Integer id1) {
        List<Integer> lessonId = repository.getCompletedLessonIdByCourseIdAndUserId(id, id1);
        if (lessonId.isEmpty()) {
           return repository.findFirstLessonIdByCourseId(id);
        }
        return lessonId.get(0);
    }

    public Integer findSectionIdByLessonId(Integer lessonIdFinalCompleted) {
        if (lessonIdFinalCompleted == null) {
            return null;
        }
        return repository.findSectionIdByLessonId(lessonIdFinalCompleted);
    }
}
