package vn.edu.fpt.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.fpt.dto.course.CourseDto;
import vn.edu.fpt.entity.Course;
import vn.edu.fpt.enums.CourseStatus;
import vn.edu.fpt.exception.CourseNotFoundException;
import vn.edu.fpt.mapper.DtoMapper;
import vn.edu.fpt.repository.CourseRepository;

@Service
@Transactional
public class ManagerCourseService {

    private final CourseRepository repository;
    private final DtoMapper dtoMapper;
    private final EmailService emailService;

    public ManagerCourseService(CourseRepository repository, DtoMapper dtoMapper, EmailService emailService) {
        this.repository = repository;
        this.dtoMapper = dtoMapper;
        this.emailService = emailService;
    }

    public Page<CourseDto> searchAndFilter(String keyword, CourseStatus status, Integer categoryId, Pageable pageable) {
        return repository.searchAndFilter(keyword, status, categoryId, pageable)
                .map(dtoMapper::toCourseDto);
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
            course.setRejectionReason(null); // Clear rejection reason if approved
        }
        repository.save(course);

        // Send email notification to instructor
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
