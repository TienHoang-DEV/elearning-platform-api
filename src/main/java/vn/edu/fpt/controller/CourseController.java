package vn.edu.fpt.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
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
import vn.edu.fpt.exception.ResourceNotFoundException;
import vn.edu.fpt.service.FeedbackService;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        return vn.edu.fpt.util.SecurityUtils.getCurrentUser();
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

            boolean canReview = courseService.canUserReviewCourse(user, id);
            model.addAttribute("canReview", canReview);
        }
        return "course/detail";
    }

    @PostMapping("/course/review/add")
    public String addCourseReview(@RequestParam("courseId") Integer courseId,
            @RequestParam("rating") Integer rating,
            @RequestParam(value = "comment", required = false, defaultValue = "") String comment,
            jakarta.servlet.http.HttpServletRequest request,
            RedirectAttributes redirectAttributes) {
        User user = getSessionUser();
        if (user == null) {
            return "redirect:/";
        }
        
        String referer = request.getHeader("Referer");
        String redirectUrl = (referer != null && referer.contains("/course/")) ? "redirect:" + referer : "redirect:/course/detail?id=" + courseId;

        String errorMessage = courseService.addCourseReview(user, courseId, rating, comment);
        if (errorMessage != null) {
            redirectAttributes.addFlashAttribute("reviewErrorMessage", errorMessage);
        } else {
            redirectAttributes.addFlashAttribute("reviewSuccessMessage", "Cảm ơn bạn đã gửi đánh giá khóa học thành công!");
        }
        return redirectUrl;
    }

    @PostMapping("/course/review/edit")
    public String editCourseReview(@RequestParam("feedbackId") Integer feedbackId,
            @RequestParam("rating") Integer rating,
            @RequestParam(value = "comment", required = false, defaultValue = "") String comment,
            RedirectAttributes redirectAttributes) {
        User user = getSessionUser();
        if (user == null) {
            return "redirect:/";
        }
        
        Feedback feedback = feedbackService.findById(feedbackId).orElse(null);
        if (feedback == null) {
            redirectAttributes.addFlashAttribute("reviewErrorMessage", "Đánh giá không tồn tại!");
            return "redirect:/courses";
        }
        
        try {
            feedbackService.updateReview(feedbackId, rating, comment, user);
            redirectAttributes.addFlashAttribute("reviewSuccessMessage", "Cập nhật đánh giá thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("reviewErrorMessage", e.getMessage());
        }
        
        return "redirect:/course/detail?id=" + feedback.getCourse().getId();
    }
}
