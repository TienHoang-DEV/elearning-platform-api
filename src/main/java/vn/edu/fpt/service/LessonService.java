package vn.edu.fpt.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.fpt.entity.CourseSection;
import vn.edu.fpt.entity.Lesson;
import vn.edu.fpt.entity.User;
import vn.edu.fpt.exception.CourseNotFoundException;
import vn.edu.fpt.exception.ResourceNotFoundException;
import vn.edu.fpt.repository.LessonRepository;
import vn.edu.fpt.util.AppConstants;
import vn.edu.fpt.util.SecurityUtils;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@Transactional
@RequiredArgsConstructor
public class LessonService {

    private final LessonRepository repository;
    private final AzureBlobService azureBlobService;

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

    public List<Integer> getCompletedLessonIdsByCourseIdAndUserId(Integer courseId, Integer userId) {
        return repository.getCompletedLessonIdByCourseIdAndUserId(courseId, userId);
    }

    public Integer findSectionIdByLessonId(Integer lessonIdFinalCompleted) {
        if (lessonIdFinalCompleted == null) {
            return null;
        }
        return repository.findSectionIdByLessonId(lessonIdFinalCompleted);
    }

    public Integer findNumberOfLessonByCourseId(Integer courseId) {
        return repository.findNumberOfLessonByCourseId(courseId);
    }

    public String findLessonUrl(Integer lessonId) {
        try {
            Lesson lesson = this.findById(lessonId).orElse(null);
            if (lesson == null || lesson.getVideoUrl() == null || lesson.getVideoUrl().trim().isEmpty()) {
                return "https://www.w3schools.com/html/mov_bbb.mp4";
            }
            return azureBlobService.generateSasUrl(AppConstants.AZURE_STORAGE_CONTAINER_VIDEOS, lesson.getVideoUrl());
        } catch (Exception e) {
            // Fallback sang video test công cộng nếu Azure bị lỗi ở local dev
            return "https://www.w3schools.com/html/mov_bbb.mp4";
        }
    }

    public Lesson findNextLessonByCurrentLesson(Lesson lesson, Integer totalNumberOfLesson, Integer totalNumberOfLessonCompleted) {
        if (totalNumberOfLesson == totalNumberOfLessonCompleted) {
            return null;
        }
        User user = SecurityUtils.getCurrentUser();
        List<Lesson> nextLessons = repository.findNotCompletedLessons(user, lesson);
        if (nextLessons == null || nextLessons.isEmpty()) {
            return null;
        }
        nextLessons.forEach(lesson1 -> {
            System.out.println("khoa hoc " +lesson1.getId());
        });
        for (Lesson nextLesson : nextLessons) {
            if ((nextLesson.getId() > lesson.getId()) || (lesson.getId() == nextLessons.get(nextLessons.size() - 1).getId())) {
                return nextLesson;
            }
        }
        return null;
    }
}
