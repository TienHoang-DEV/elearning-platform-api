package vn.edu.fpt.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import vn.edu.fpt.dto.course.CategoryDto;
import vn.edu.fpt.dto.course.CourseDto;
import vn.edu.fpt.dto.course.CourseListDto;
import vn.edu.fpt.entity.User;
import vn.edu.fpt.entity.Course;
import vn.edu.fpt.entity.Feedback;
import org.springframework.data.domain.Page;
import vn.edu.fpt.service.CourseService;
import vn.edu.fpt.service.CategoryService;
import vn.edu.fpt.service.UserService;
import vn.edu.fpt.service.EnrollmentService;
import vn.edu.fpt.service.CartService;
import vn.edu.fpt.service.CartItemService;
import vn.edu.fpt.service.FeedbackService;
import java.util.List;

@Controller
public class CourseController {

    @Autowired
    private CourseService courseService;

    @Autowired
    private FeedbackService feedbackService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private UserService userService;

    @Autowired
    private EnrollmentService enrollmentService;

    @Autowired
    private CartService cartService;

    @Autowired
    private CartItemService cartItemService;

    private User getSessionUser() {
        try {
            User currentUser = vn.edu.fpt.util.SecurityUtils.getCurrentUser();
            if (currentUser != null) {
                try {
                    return userService.findById(currentUser.getId());
                } catch (Exception e) {
                    return currentUser;
                }
            }
            jakarta.servlet.http.HttpServletRequest request = ((org.springframework.web.context.request.ServletRequestAttributes) org.springframework.web.context.request.RequestContextHolder
                    .currentRequestAttributes()).getRequest();
            jakarta.servlet.http.HttpSession session = request.getSession(false);
            if (session != null) {
                User sessionUser = (User) session.getAttribute("user");
                if (sessionUser != null) {
                    try {
                        return userService.findById(sessionUser.getId());
                    } catch (Exception e) {
                        return sessionUser;
                    }
                }
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    @GetMapping("/courses")
    public String showCourseList(
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "categoryId", required = false) Integer categoryId,
            @RequestParam(value = "ratings", required = false) List<Double> ratings,
            @RequestParam(value = "prices", required = false) List<String> prices,
            @RequestParam(value = "sort", required = false, defaultValue = "rating") String sort,
            @RequestParam(value = "page", required = false, defaultValue = "1") Integer page,
            Model model) {

        Page<CourseListDto> coursePage = courseService.getPagedCoursesSummary(
                search, categoryId, ratings, prices, sort, page, 4);
        List<CategoryDto> categoryDtos = categoryService.getActiveParentCategories();

        model.addAttribute("parentCategories", categoryDtos);

        User user = getSessionUser();

        java.util.Set<Integer> enrolledCourseIds = enrollmentService.getEnrolledCourseIds(user);
        model.addAttribute("enrolledCourseIds", enrolledCourseIds);



        // Đưa dữ liệu trang hiện tại và các thuộc tính phân trang vào Model để render ra UI
        model.addAttribute("courses", coursePage.getContent());
        model.addAttribute("search", search);
        model.addAttribute("categoryId", categoryId);
        if (categoryId != null) {
            try {
                vn.edu.fpt.entity.Category selectedCategory = categoryService.findByIdAndStatus(categoryId, "ACTIVE");
                if (selectedCategory != null) {
                    model.addAttribute("selectedCategoryName", selectedCategory.getName());
                }
            } catch (Exception ignored) {
            }
        }
        model.addAttribute("selectedRatings", ratings);
        model.addAttribute("selectedPrices", prices);
        model.addAttribute("sort", sort);
        model.addAttribute("currentPage", coursePage.getNumber() + 1);
        model.addAttribute("totalPages", coursePage.getTotalPages());
        model.addAttribute("totalCourses", coursePage.getTotalElements());

        return "course/list";
    }

    @GetMapping("/coursemanager")
    public String getall() {
        return "instructor_course/course_manager";
    }

    @GetMapping("/course/detail")
    public String showCourseDetail(@RequestParam("id") Integer id, Model model) {
        CourseDto courseDto = courseService.getCourseDetail(id);
        model.addAttribute("course", courseDto);

        User user = getSessionUser();
        if (user != null) {
            java.util.Set<Integer> enrolledCourseIds = enrollmentService.getEnrolledCourseIds(user);
            model.addAttribute("enrolledCourseIds", enrolledCourseIds);

            boolean hasReviewed = feedbackService.hasUserReviewedCourse(user.getId(), id);
            model.addAttribute("hasReviewed", hasReviewed);


        }
        return "course/detail";
    }

    @PostMapping("/course/review/add")
    public String addCourseReview(@RequestParam("courseId") Integer courseId,
            @RequestParam("rating") Integer rating,
            @RequestParam(value = "comment", required = false, defaultValue = "") String comment,
            org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        User user = getSessionUser();
        if (user == null) {
            return "redirect:/";
        }
        if (feedbackService.hasUserReviewedCourse(user.getId(), courseId)) {
            return "redirect:/course/detail?id=" + courseId;
        }
        Course course = courseService.findById(courseId);
        Feedback feedback = Feedback.builder()
                .user(user)
                .course(course)
                .rating(rating)
                .comment(comment)
                .createdAt(java.time.LocalDateTime.now())
                .build();
        feedbackService.save(feedback);
        redirectAttributes.addFlashAttribute("reviewSuccessMessage", "Cảm ơn bạn đã gửi đánh giá khóa học thành công!");
        return "redirect:/course/detail?id=" + courseId;
    }
}
