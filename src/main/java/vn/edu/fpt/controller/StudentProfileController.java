package vn.edu.fpt.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import vn.edu.fpt.dto.home.HomeDto;
import vn.edu.fpt.dto.user.StudentLearningDto;
import vn.edu.fpt.dto.user.StudentProfileDashboardDto;
import vn.edu.fpt.dto.user.StudentPurchaseHistoryDto;
import vn.edu.fpt.entity.User;
import vn.edu.fpt.repository.UserRepository;
import vn.edu.fpt.service.CourseService;
import vn.edu.fpt.service.HistoryOrderService;
import vn.edu.fpt.service.MyCourseService;
import vn.edu.fpt.service.StudentProfileService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Controller
public class StudentProfileController {

    private final UserRepository userRepository;
    private final CourseService courseService;
    private final StudentProfileService studentProfileService;
    private final MyCourseService myCourseService;
    private final HistoryOrderService historyOrderService;

    public StudentProfileController(UserRepository userRepository,
                                    CourseService courseService,
                                    StudentProfileService studentProfileService,
                                    MyCourseService myCourseService,
                                    HistoryOrderService historyOrderService) {
        this.userRepository = userRepository;
        this.courseService = courseService;
        this.studentProfileService = studentProfileService;
        this.myCourseService = myCourseService;
        this.historyOrderService = historyOrderService;
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
        if (currentUser != null) {
            if (!currentUser.isFavoriteSetupCompleted()) {
                return "redirect:/student/favorites/step1";
            }
        }
        HomeDto homeData = courseService.getHomeData(currentUser);
        model.addAttribute("homeData", homeData);
        return "home/home";
    }

    @GetMapping("/student/profile")
    public String showStudentProfile(Model model) {
        User user = getSessionUser();
        if (user == null) {
            return "redirect:/login";
        }
        
        StudentProfileDashboardDto dashboardData = studentProfileService.getDashboardData(user);

        model.addAttribute("currentUser", dashboardData.getCurrentUser());
        model.addAttribute("enrollmentsCount", dashboardData.getEnrollmentsCount());
        model.addAttribute("certificatesCount", dashboardData.getCertificatesCount());
        model.addAttribute("studyHours", dashboardData.getStudyHours());
        
        return "student_profile/student_profile";
    }

    @GetMapping("/student/my-learning")
    public String showMyLearning(
            @RequestParam(value = "filter", defaultValue = "all") String filter,
            @RequestParam(value = "page", defaultValue = "1") int page,
            Model model) {
        User user = getSessionUser();
        if (user == null) {
            return "redirect:/login";
        }
        
        StudentLearningDto learningData = myCourseService.getLearningData(user, filter, page);
        
        model.addAttribute("currentUser", learningData.getCurrentUser());
        model.addAttribute("enrollments", learningData.getEnrollments());
        model.addAttribute("enrollmentsCount", learningData.getEnrollmentsCount());
        model.addAttribute("filter", learningData.getFilter());
        model.addAttribute("currentPage", learningData.getCurrentPage());
        model.addAttribute("totalPages", learningData.getTotalPages());
        
        return "my_learning/my_learning";
    }

    @GetMapping("/student/purchase-history")
    public String showPurchaseHistory(Model model) {
        User user = getSessionUser();
        if (user == null) {
            return "redirect:/login";
        }
        
        StudentPurchaseHistoryDto purchaseHistoryData = historyOrderService.getPurchaseHistoryData(user);
        
        model.addAttribute("currentUser", purchaseHistoryData.getCurrentUser());
        model.addAttribute("orders", purchaseHistoryData.getOrders());
        
        return "purchase_history/purchase_history";
    }

}



