package vn.edu.fpt.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import vn.edu.fpt.entity.User;
import vn.edu.fpt.entity.Enrollment;
import vn.edu.fpt.entity.Order;
import vn.edu.fpt.entity.Course;
import vn.edu.fpt.repository.UserRepository;
import vn.edu.fpt.service.OrderService;
import vn.edu.fpt.repository.EnrollmentRepository;
import vn.edu.fpt.repository.CourseRepository;
import vn.edu.fpt.enums.CourseStatus;

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
    private final OrderService orderService;
    private final EnrollmentRepository enrollmentRepository;
    private final CourseRepository courseRepository;
    private final DtoMapper dtoMapper;

    public StudentProfileController(UserRepository userRepository,
                                    OrderService orderService,
                                    EnrollmentRepository enrollmentRepository,
                                    CourseRepository courseRepository,
                                    DtoMapper dtoMapper) {
        this.userRepository = userRepository;
        this.orderService = orderService;
        this.enrollmentRepository = enrollmentRepository;
        this.courseRepository = courseRepository;
        this.dtoMapper = dtoMapper;
    }

    private User getSessionUser() {
        try {
            User currentUser = vn.edu.fpt.util.SecurityUtils.getCurrentUser();
            if (currentUser != null) {
                return userRepository.findById(currentUser.getId()).orElse(currentUser);
            }
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
        return null;
    }

    @GetMapping("/")
    public String showHomePage(Model model) {
        User currentUser = getSessionUser();
        model.addAttribute("currentUser", currentUser);
        return "home/home";
    }

    @org.springframework.transaction.annotation.Transactional
    @GetMapping("/student/profile")
    public String showStudentProfile(Model model) {
        User user = getSessionUser();
        if (user == null) {
            return "redirect:/login_no";
        }
        
        int enrollmentsCount = user.getEnrollments().size();
        long certificatesCount = 0;
        for (Enrollment e : user.getEnrollments()) {
            if (e.getProgressPercent() != null && e.getProgressPercent().doubleValue() >= 100) {
                certificatesCount++;
            }
        }
                
        int totalHours = 0;
        for (Enrollment en : user.getEnrollments()) {
            double pct = en.getProgressPercent() != null ? en.getProgressPercent().doubleValue() : 0.0;
            totalHours += (int) (pct * 8.0 / 100.0);
        }
        if (totalHours == 0 && enrollmentsCount > 0) {
            totalHours = 2;
        }

        model.addAttribute("currentUser", dtoMapper.toUserDto(user));
        model.addAttribute("enrollmentsCount", enrollmentsCount);
        model.addAttribute("certificatesCount", certificatesCount);
        model.addAttribute("studyHours", totalHours);
        
        return "student_profile/student_profile";
    }

    @org.springframework.transaction.annotation.Transactional
    @GetMapping("/student/my-learning")
    public String showMyLearning(
            @org.springframework.web.bind.annotation.RequestParam(value = "filter", defaultValue = "all") String filter,
            @org.springframework.web.bind.annotation.RequestParam(value = "page", defaultValue = "1") int page,
            Model model) {
        User user = getSessionUser();
        if (user == null) {
            return "redirect:/login_no";
        }
        List<Enrollment> enrollments = enrollmentRepository.findByUser(user);
        
        List<EnrollmentDto> enrollmentDtos = new java.util.ArrayList<>();
        for (Enrollment enrollment : enrollments) {
            EnrollmentDto dto = dtoMapper.toEnrollmentDto(enrollment);
            if ("incomplete".equalsIgnoreCase(filter)) {
                if (dto.getProgressPercent() != null && dto.getProgressPercent().doubleValue() >= 100.0) {
                    continue;
                }
            }
            enrollmentDtos.add(dto);
        }
        
        int pageSize = 6;
        int totalItems = enrollmentDtos.size();
        int totalPages = (int) Math.ceil((double) totalItems / pageSize);
        if (totalPages == 0) {
            totalPages = 1;
        }
        
        int currentPage = Math.max(1, Math.min(page, totalPages));
        int fromIndex = (currentPage - 1) * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, totalItems);
        
        List<EnrollmentDto> pagedEnrollments = new java.util.ArrayList<>();
        if (fromIndex < totalItems) {
            pagedEnrollments = enrollmentDtos.subList(fromIndex, toIndex);
        }
        
        model.addAttribute("currentUser", dtoMapper.toUserDto(user));
        model.addAttribute("enrollments", pagedEnrollments);
        model.addAttribute("enrollmentsCount", totalItems);
        model.addAttribute("filter", filter);
        model.addAttribute("currentPage", currentPage);
        model.addAttribute("totalPages", totalPages);
        
        return "my_learning/my_learning";
    }

    @org.springframework.transaction.annotation.Transactional
    @GetMapping("/student/purchase-history")
    public String showPurchaseHistory(Model model) {
        User user = getSessionUser();
        if (user == null) {
            return "redirect:/login_no";
        }
        List<OrderDto> orderDtos = orderService.getPurchaseHistory(user);
        
        model.addAttribute("currentUser", dtoMapper.toUserDto(user));
        model.addAttribute("orders", orderDtos);
        
        return "purchase_history/purchase_history";
    }

    @org.springframework.transaction.annotation.Transactional
    @GetMapping("/student/recommendations")
    public String showRecommendations(Model model) {
        User user = getSessionUser();
        if (user == null) {
            return "redirect:/login_no";
        }
        
        Set<Integer> enrolledCourseIds = new java.util.HashSet<>();
        for (Enrollment e : user.getEnrollments()) {
            if (e.getCourse() != null) {
                enrolledCourseIds.add(e.getCourse().getId());
            }
        }
                
        List<CourseDto> recommendedCourses = new java.util.ArrayList<>();
        for (Course c : courseRepository.findAll()) {
            if (!enrolledCourseIds.contains(c.getId()) && c.getStatus() == CourseStatus.PUBLISHED) {
                recommendedCourses.add(dtoMapper.toCourseDto(c));
            }
        }
                
        model.addAttribute("currentUser", dtoMapper.toUserDto(user));
        model.addAttribute("recommendedCourses", recommendedCourses);
        
        return "recommendations/recommendations";
    }
}

