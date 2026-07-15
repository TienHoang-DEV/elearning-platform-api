package vn.edu.fpt.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.fpt.entity.Course;
import vn.edu.fpt.entity.Enrollment;
import vn.edu.fpt.entity.SystemLog;
import vn.edu.fpt.entity.User;
import vn.edu.fpt.enums.LogAction;
import vn.edu.fpt.exception.CourseNotFoundException;
import vn.edu.fpt.exception.ResourceNotFoundException;
import vn.edu.fpt.repository.EnrollmentRepository;
import vn.edu.fpt.service.lesson.LessonProgressService;
import vn.edu.fpt.service.lesson.LessonService;
import vn.edu.fpt.util.SecurityUtils;

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
    private final SystemLogService systemLogService;
    private final UserService userService;
    private final CourseService courseService;
    private final EmailService emailService;

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


    public void grantAccessCourse(Integer userId, Integer courseId, String reason, String note, Boolean sendEmail) {
        if (userId == null) {
            throw new RuntimeException("Không có id người dùng");
        }
        if (courseId == null) {
            throw new CourseNotFoundException("Không tìm thấy khóa học");
        }
        if (reason == null) {
            throw new RuntimeException("Lý do không được để trống");
        }
        if (sendEmail == null) {
            sendEmail = false;
        }
        if (repository.existsByUserAndCourse(User.builder().id(userId).build(), Course.builder().id(courseId).build())) {
         throw new RuntimeException("Người dùng với id = " + userId + " đã tham gia khóa học với id = " + courseId + " trước đó");
        }
        Enrollment enrollment = Enrollment.builder()
                .user(User.builder().id(userId).build())
                .course(Course.builder().id(courseId).build())
                .progressPercent(BigDecimal.ZERO)
                .build();
        repository.save(enrollment);
        SystemLog systemLog = SystemLog.builder()
                .action(LogAction.MANUAL_ENROLLMENT_GRANTED)
                .user(SecurityUtils.getCurrentUser())
                .targetType(Enrollment.class.getName())
                .targetId(enrollment.getId().toString())
                .meta("Lý do: " + reason + " Ghi chú " + note)
                .build();
        systemLogService.save(systemLog);
        if (sendEmail) {
            emailService.sendGrantAccessCourseEmail(userService.findById(userId).getEmail(), courseService.findById(courseId));
        }
    }

    public String grantAccessCourses(Integer userId, List<Integer> courseIds, String reason, String note, Boolean sendEmail) {
        if (userId == null) {
            throw new RuntimeException("Không có id người dùng");
        }
        if (courseIds == null || courseIds.isEmpty()) {
            throw new RuntimeException("Vui lòng chọn ít nhất một khóa học");
        }
        if (reason == null) {
            throw new RuntimeException("Lý do không được để trống");
        }
        
        int successCount = 0;
        StringBuilder alreadyEnrolledMsg = new StringBuilder();
        List<Course> enrolledCourses = new java.util.ArrayList<>();
        
        for (Integer courseId : courseIds) {
            try {
                grantAccessCourse(userId, courseId, reason, note, false);
                enrolledCourses.add(courseService.findById(courseId));
                successCount++;
            } catch (RuntimeException e) {
                if (e.getMessage() != null && e.getMessage().contains("đã tham gia khóa học")) {
                    alreadyEnrolledMsg.append(e.getMessage()).append(". ");
                } else {
                    throw e;
                }
            }
        }
        
        if (successCount == 0 && alreadyEnrolledMsg.length() > 0) {
            throw new RuntimeException(alreadyEnrolledMsg.toString());
        }
        
        if (sendEmail != null && sendEmail && !enrolledCourses.isEmpty()) {
            String toEmail = userService.findById(userId).getEmail();
            emailService.sendGrantAccessCoursesEmail(toEmail, enrolledCourses);
        }
        
        String msg = "Thành công thêm người dùng vào " + successCount + " khóa học.";
        if (alreadyEnrolledMsg.length() > 0) {
            msg += " Lưu ý: " + alreadyEnrolledMsg.toString();
        }
        return msg;
    }
}
