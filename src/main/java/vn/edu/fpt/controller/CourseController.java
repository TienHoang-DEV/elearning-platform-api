package vn.edu.fpt.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import vn.edu.fpt.entity.User;
import vn.edu.fpt.dto.*;
import vn.edu.fpt.service.CourseService;
import vn.edu.fpt.service.CategoryService;
import vn.edu.fpt.service.UserService;
import vn.edu.fpt.service.EnrollmentService;
import java.util.List;

@Controller
public class CourseController {

    @Autowired
    private CourseService courseService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private UserService userService;

    @Autowired
    private EnrollmentService enrollmentService;

    private User getSessionUser() {
        try {
            User currentUser = vn.edu.fpt.util.SecurityUtils.getCurrentUser();
            if (currentUser != null) {
                return userRepository.findById(currentUser.getId()).orElse(currentUser);
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
        return userService.findByEmail("28tech@gmail.com")
                .orElseGet(() -> {
                    List<User> allUsers = userService.findAll();
                    if (allUsers.isEmpty()) {
                        throw new IllegalStateException("Không tìm thấy người dùng nào trong cơ sở dữ liệu để giả lập. Vui lòng import lại file sql_ddl_dml/ElearningPlatform.sql vào SQL Server của bạn!");
                    }
                    return allUsers.get(0);
                });
    }

    @GetMapping("/courses")
    public String showCourseList(
            @RequestParam(value = "search", required = false) String search,
            Model model) {

        List<CourseDto> courseDtos = courseService.getCoursesBySearch(search);
        List<CategoryDto> categoryDtos = categoryService.getActiveParentCategories();

        model.addAttribute("parentCategories", categoryDtos);

        User user = getSessionUser();
        java.util.Set<Integer> enrolledCourseIds = enrollmentService.getEnrolledCourseIds(user);
        model.addAttribute("enrolledCourseIds", enrolledCourseIds);

        // Đưa danh sách khóa học vào Model với key là "courses" để Thymeleaf render
        model.addAttribute("courses", courseDtos);
        // Đưa từ khóa tìm kiếm vào Model để hiển thị lại trên thanh tìm kiếm và tiêu đề
        model.addAttribute("search", search);

        // Trả về template course/list.html
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
        return "course/detail";
    }
}
