package vn.edu.fpt.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.fpt.entity.CourseSection;
import vn.edu.fpt.entity.Lesson;
import vn.edu.fpt.exception.CourseNotFoundException;
import vn.edu.fpt.exception.ResourceNotFoundException;
import vn.edu.fpt.repository.LessonRepository;
import vn.edu.fpt.util.AppConstants;

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
            // 1. Tìm thông tin bài giảng theo ID trong Database
            Lesson lesson = this.findById(lessonId).orElse(null);
            
            // 2. Nếu không tìm thấy hoặc videoUrl trống -> Trả về link video test mặc định (W3Schools)
            if (lesson == null || lesson.getVideoUrl() == null || lesson.getVideoUrl().trim().isEmpty()) {
                return "https://www.w3schools.com/html/mov_bbb.mp4";
            }
            
            // 3. Nếu hợp lệ -> Gọi AzureBlobService để ký sinh khóa bảo mật SAS URL từ Azure Container
            return azureBlobService.generateSasUrl(AppConstants.AZURE_STORAGE_CONTAINER_VIDEOS, lesson.getVideoUrl());
        } catch (Exception e) {
            // Fallback sang video test công cộng nếu Azure gặp lỗi kết nối ở localhost phát triển
            return "https://www.w3schools.com/html/mov_bbb.mp4";
        }
    }
}
