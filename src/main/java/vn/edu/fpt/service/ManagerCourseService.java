package vn.edu.fpt.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.fpt.dto.course.CourseDto;
import vn.edu.fpt.entity.Course;
import vn.edu.fpt.enums.CourseStatus;
import vn.edu.fpt.enums.LogAction;
import vn.edu.fpt.exception.CourseNotFoundException;
import vn.edu.fpt.mapper.DtoMapper;
import vn.edu.fpt.repository.CourseRepository;
import vn.edu.fpt.util.SecurityUtils;

@Service
@Transactional
public class ManagerCourseService {

    private final CourseRepository repository;
    private final DtoMapper dtoMapper;
    private final EmailService emailService;
    private final SystemLogService systemLogService;

    public ManagerCourseService(CourseRepository repository, DtoMapper dtoMapper, EmailService emailService, SystemLogService systemLogService) {
        this.repository = repository;
        this.dtoMapper = dtoMapper;
        this.emailService = emailService;
        this.systemLogService = systemLogService;
    }

    public Page<CourseDto> searchAndFilter(String keyword, CourseStatus status, Integer categoryId, Pageable pageable) {
        return repository.searchAndFilter(keyword, status, categoryId, pageable)
                .map(dtoMapper::toSimpleCourseDto);
    }

    public void updateCourseStatus(Integer id, CourseStatus status) {
        updateCourseStatus(id, status, null);
    }

    public void updateCourseStatus(Integer id, CourseStatus status, String rejectionReason) {
        Course course = repository.findById(id)
                .orElseThrow(() -> new CourseNotFoundException("Khóa học không tìm thấy"));
        course.setStatus(status);
        if (status == CourseStatus.REJECTED) {
            course.setRejectionReason(rejectionReason);
        } else if (status == CourseStatus.PUBLISHED) {
            course.setRejectionReason(null); // Xóa lý do từ chối nếu khóa học được phê duyệt
        }
        repository.save(course);

        // Ghi log hoạt động vào SystemLog
        vn.edu.fpt.entity.User currentUser = SecurityUtils.getCurrentUser();
        if (currentUser != null) {
            LogAction action = (status == CourseStatus.PUBLISHED) ? LogAction.APPROVE_COURSE : 
                               (status == CourseStatus.REJECTED) ? LogAction.REJECT_COURSE : null;
            if (action != null) {
                String meta = "Tên khóa học: " + course.getTitle();
                if (status == CourseStatus.REJECTED && rejectionReason != null) {
                    meta += " | Lý do từ chối: " + rejectionReason;
                }
                systemLogService.log(currentUser, action, "COURSE", String.valueOf(id), meta);
            }
        }

        // Gửi email thông báo cho giảng viên
        try {
            if (status == CourseStatus.PUBLISHED) {
                if (course.getInstructor() != null && course.getInstructor().getEmail() != null) {
                    String fullName = (course.getInstructor().getLastName() + " " + course.getInstructor().getFirstName()).trim();
                    emailService.sendCourseApprovedEmail(course.getInstructor().getEmail(), fullName, course.getTitle());
                }
            } else if (status == CourseStatus.REJECTED) {
                if (course.getInstructor() != null && course.getInstructor().getEmail() != null) {
                    String fullName = (course.getInstructor().getLastName() + " " + course.getInstructor().getFirstName()).trim();
                    emailService.sendCourseRejectedEmail(course.getInstructor().getEmail(), fullName, course.getTitle(), rejectionReason);
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to send course approval/rejection email: " + e.getMessage());
        }
    }
}
