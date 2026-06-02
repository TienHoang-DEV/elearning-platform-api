package vn.edu.fpt.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
//import vn.edu.fpt.exeption.CourseNotFoundException;
import vn.edu.fpt.repository.CourseRepository;
import vn.edu.fpt.repository.CategoryRepository;
import vn.edu.fpt.entity.Course;
import vn.edu.fpt.entity.Category;
import vn.edu.fpt.entity.User;
import vn.edu.fpt.repository.UserRepository;
import java.util.List;

@Controller
public class CourseController {

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private vn.edu.fpt.repository.EnrollmentRepository enrollmentRepository;

    private User getSessionUser() {
        try {
            jakarta.servlet.http.HttpServletRequest request = ((org.springframework.web.context.request.ServletRequestAttributes) org.springframework.web.context.request.RequestContextHolder.currentRequestAttributes()).getRequest();
            jakarta.servlet.http.HttpSession session = request.getSession(false);
            if (session != null) {
                User sessionUser = (User) session.getAttribute("user");
                if (sessionUser != null) {
                    return userRepository.findById(sessionUser.getId()).orElse(sessionUser);
                }
            }
        } catch (Exception ignored) {
        }
        return userRepository.findByEmail("28tech@gmail.com")
                .orElseGet(() -> userRepository.findAll().stream().findFirst()
                        .orElseThrow(() -> new IllegalStateException("Không tìm thấy người dùng nào trong DB để giả lập.")));
    }

    @org.springframework.transaction.annotation.Transactional
    @GetMapping("/courses")
    public String showCourseList(
            @RequestParam(value = "search", required = false) String search,
            Model model) {
        
        List<Course> courses;
        
        if (search != null && !search.trim().isEmpty()) {
            // Lấy danh sách khóa học khớp với từ khóa tìm kiếm
            courses = courseRepository.findByTitleContainingIgnoreCase((search.trim());
        } else {
            // Nếu không tìm kiếm, lấy toàn bộ khóa học
            courses = courseRepository.findAll();
        }
        
        List<Category> parentCategories = categoryRepository.findByParentIsNullAndStatus("active");
        
        // Khởi tạo các collection Lazy để Thymeleaf có thể sử dụng (vì OSIV có thể đang bị tắt)
        for (Category parent : parentCategories) {
            org.hibernate.Hibernate.initialize(parent.getChildren());
            for (Category child : parent.getChildren()) {
                org.hibernate.Hibernate.initialize(child.getCourses());
            }
        }
        
        for (Course course : courses) {
            org.hibernate.Hibernate.initialize(course.getInstructor());
            org.hibernate.Hibernate.initialize(course.getCategory());
            org.hibernate.Hibernate.initialize(course.getFeedbacks());
            org.hibernate.Hibernate.initialize(course.getSections());
            if (course.getSections() != null) {
                for (vn.edu.fpt.entity.CourseSection section : course.getSections()) {
                    org.hibernate.Hibernate.initialize(section.getLessons());
                }
            }
        }

        model.addAttribute("parentCategories", parentCategories);
        
        User user = getSessionUser();
        java.util.Set<Integer> enrolledCourseIds = new java.util.HashSet<>();
        if (user != null) {
            enrolledCourseIds = enrollmentRepository.findByUser(user).stream()
                    .map(e -> {
                        org.hibernate.Hibernate.initialize(e.getCourse());
                        return e.getCourse().getId();
                    })
                    .collect(java.util.stream.Collectors.toSet());
        }
        model.addAttribute("enrolledCourseIds", enrolledCourseIds);

        // Đưa danh sách khóa học vào Model với key là "courses" để Thymeleaf render
        model.addAttribute("courses", courses);
        // Đưa từ khóa tìm kiếm vào Model để hiển thị lại trên thanh tìm kiếm và tiêu đề
        model.addAttribute("search", search);
        
        // Trả về template course/list.html
        return "course/list";
    }

    @GetMapping("/coursemanager")
    public String getall(){
        return "instructor_course/course_manager";
    }

    @org.springframework.transaction.annotation.Transactional
    @GetMapping("/course/detail")
    public String showCourseDetail(@RequestParam("id") Integer id, Model model) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new CourseNotFoundException("Khóa học không tìm thấy"));

        org.hibernate.Hibernate.initialize(course.getInstructor());
        org.hibernate.Hibernate.initialize(course.getCategory());
        org.hibernate.Hibernate.initialize(course.getFeedbacks());
        if (course.getFeedbacks() != null) {
            for (vn.edu.fpt.entity.Feedback fb : course.getFeedbacks()) {
                org.hibernate.Hibernate.initialize(fb.getUser());
            }
        }
        
        org.hibernate.Hibernate.initialize(course.getEnrollments());
        
        org.hibernate.Hibernate.initialize(course.getSections());
        if (course.getSections() != null) {
            for (vn.edu.fpt.entity.CourseSection section : course.getSections()) {
                org.hibernate.Hibernate.initialize(section.getLessons());
            }
        }

        User user = getSessionUser();
        java.util.Set<Integer> enrolledCourseIds = new java.util.HashSet<>();
        if (user != null) {
            enrolledCourseIds = enrollmentRepository.findByUser(user).stream()
                    .map(e -> {
                        org.hibernate.Hibernate.initialize(e.getCourse());
                        return e.getCourse().getId();
                    })
                    .collect(java.util.stream.Collectors.toSet());
        }
        model.addAttribute("enrolledCourseIds", enrolledCourseIds);
        model.addAttribute("course", course);
        return "course/detail";
    }
}
