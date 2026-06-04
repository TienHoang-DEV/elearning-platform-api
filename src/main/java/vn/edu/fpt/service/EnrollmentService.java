package vn.edu.fpt.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.fpt.entity.Enrollment;
import vn.edu.fpt.entity.User;
import vn.edu.fpt.exception.ResourceNotFoundException;
import vn.edu.fpt.repository.EnrollmentRepository;
import vn.edu.fpt.repository.LessonRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.HashSet;

@Service
@Transactional
@RequiredArgsConstructor
public class EnrollmentService {
    private final EnrollmentRepository repository;
    private final LessonService lessonService;
    private final LessonProgressService lessonProgressService;

    public Set<Integer> getEnrolledCourseIds(User user) {
        Set<Integer> enrolledCourseIds = new HashSet<>();
        if (user != null) {
            List<Enrollment> userEnrollments = repository.findByUser(user);
            for (Enrollment e : userEnrollments) {
                if (e.getCourse() != null) {
                    enrolledCourseIds.add(e.getCourse().getId());
                }
            }
        }
        return enrolledCourseIds;
    }

    public List<Enrollment> findAll() {
        return repository.findAll();
    }

    public Optional<Enrollment> findById(Integer id) {
        return repository.findById(id);
    }

    public Enrollment save(Enrollment entity) {
        return repository.save(entity);
    }

    public void deleteById(Integer id) {
        repository.deleteById(id);
    }

    public boolean existsById(Integer id) {
        return repository.existsById(id);
    }

    public Enrollment findEnrollmentByCourseIdAndUserId(Integer courseId, Integer userId) {
        Enrollment enrollment = repository.findByCourseIdAndUserId(courseId, userId).orElseThrow(() -> new ResourceNotFoundException("Người dùng chưa mua khóa học này"));
        return enrollment;
    }

    public void updateEnrollmentProgressPercent(Enrollment enrollment, Integer courseId, Integer sectionId) {
        Integer totalNumberOfLesson = lessonService.findNumberOfLessonByCourseId(courseId);
        if (totalNumberOfLesson == 0) {
            return;
        }
        Integer totalNumberOfLessonCompleted = lessonProgressService.findNumberOfLessonCompletedByEnrollment(enrollment);
        BigDecimal percent = BigDecimal.valueOf((double) totalNumberOfLessonCompleted / totalNumberOfLesson * 100);
        if (percent.compareTo(BigDecimal.valueOf(100)) < 0) {
            enrollment.setProgressPercent(percent);
        } else {
            if (enrollment.getCompletedAt() == null) {
                enrollment.setProgressPercent(percent);
                enrollment.setCompletedAt(LocalDateTime.now());
            }
        }
        repository.save(enrollment);
    }
}
