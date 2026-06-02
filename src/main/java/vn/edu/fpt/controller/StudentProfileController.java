package vn.edu.fpt.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import vn.edu.fpt.entity.User;
import vn.edu.fpt.entity.Enrollment;
import vn.edu.fpt.entity.Order;
import vn.edu.fpt.entity.Course;
import vn.edu.fpt.repository.UserRepository;
import vn.edu.fpt.repository.OrderRepository;
import vn.edu.fpt.repository.EnrollmentRepository;
import vn.edu.fpt.repository.CourseRepository;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import vn.edu.fpt.mapper.DtoMapper;
import vn.edu.fpt.dto.*;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
public class StudentProfileController {

    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final CourseRepository courseRepository;

    public StudentProfileController(UserRepository userRepository,
                                    OrderRepository orderRepository,
                                    EnrollmentRepository enrollmentRepository,
                                    CourseRepository courseRepository) {
        this.userRepository = userRepository;
        this.orderRepository = orderRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.courseRepository = courseRepository;
    }

    private User getSessionUser() {
        try {
            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
            HttpSession session = request.getSession(false);
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
                        .orElseThrow(() -> new IllegalStateException("Không tìm thấy người dùng nào trong cơ sở dữ liệu để giả lập. Vui lòng import lại file sql_ddl_dml/ElearningPlatform.sql vào SQL Server của bạn!")));
    }

    @GetMapping("/")
    public String showHomePage(Model model) {
        return "home/home_logged_in";
    }

    @org.springframework.transaction.annotation.Transactional
    @GetMapping("/student/profile")
    public String showStudentProfile(Model model) {
        User user = getSessionUser();
        
        int enrollmentsCount = user.getEnrollments().size();
        long certificatesCount = user.getEnrollments().stream()
                .filter(e -> e.getProgressPercent() != null && e.getProgressPercent().doubleValue() >= 100)
                .count();
                
        int totalHours = 0;
        for (Enrollment en : user.getEnrollments()) {
            double pct = en.getProgressPercent() != null ? en.getProgressPercent().doubleValue() : 0.0;
            totalHours += (int) (pct * 8.0 / 100.0);
        }
        if (totalHours == 0 && enrollmentsCount > 0) {
            totalHours = 2;
        }

        model.addAttribute("currentUser", DtoMapper.INSTANCE.toUserDto(user));
        model.addAttribute("enrollmentsCount", enrollmentsCount);
        model.addAttribute("certificatesCount", certificatesCount);
        model.addAttribute("studyHours", totalHours);
        
        return "student_profile/student_profile";
    }

    @org.springframework.transaction.annotation.Transactional
    @GetMapping("/student/my-learning")
    public String showMyLearning(Model model) {
        User user = getSessionUser();
        List<Enrollment> enrollments = enrollmentRepository.findByUser(user);
        
        List<EnrollmentDto> enrollmentDtos = enrollments.stream()
                .map(DtoMapper.INSTANCE::toEnrollmentDto)
                .collect(Collectors.toList());
        
        model.addAttribute("currentUser", DtoMapper.INSTANCE.toUserDto(user));
        model.addAttribute("enrollments", enrollmentDtos);
        model.addAttribute("enrollmentsCount", enrollmentDtos.size());
        
        return "my_learning/my_learning";
    }

    @org.springframework.transaction.annotation.Transactional
    @GetMapping("/student/purchase-history")
    public String showPurchaseHistory(Model model) {
        User user = getSessionUser();
        List<Order> orders = orderRepository.findByUser(user);
        
        List<OrderDto> orderDtos = orders.stream()
                .map(DtoMapper.INSTANCE::toOrderDto)
                .collect(Collectors.toList());
        
        model.addAttribute("currentUser", DtoMapper.INSTANCE.toUserDto(user));
        model.addAttribute("orders", orderDtos);
        
        return "purchase_history/purchase_history";
    }

    @org.springframework.transaction.annotation.Transactional
    @GetMapping("/student/recommendations")
    public String showRecommendations(Model model) {
        User user = getSessionUser();
        
        Set<Integer> enrolledCourseIds = user.getEnrollments().stream()
                .filter(e -> e.getCourse() != null)
                .map(e -> e.getCourse().getId())
                .collect(Collectors.toSet());
                
        List<CourseDto> recommendedCourses = courseRepository.findAll().stream()
                .filter(c -> !enrolledCourseIds.contains(c.getId()))
                .map(DtoMapper.INSTANCE::toCourseDto)
                .collect(Collectors.toList());
                
        model.addAttribute("currentUser", DtoMapper.INSTANCE.toUserDto(user));
        model.addAttribute("recommendedCourses", recommendedCourses);
        
        return "recommendations/recommendations";
    }
}

