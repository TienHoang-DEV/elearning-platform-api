package vn.edu.fpt.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.edu.fpt.dto.course.CourseDto;
import vn.edu.fpt.enums.CourseStatus;
import vn.edu.fpt.service.CategoryService;
import vn.edu.fpt.service.CourseService;

@Controller
@RequestMapping("/manager/course")
public class ManagerCourseController {

    private final CourseService courseService;
    private final CategoryService categoryService;

    public ManagerCourseController(CourseService courseService, CategoryService categoryService) {
        this.courseService = courseService;
        this.categoryService = categoryService;
    }

    /**
     * GET /manager/course/list
     * Hiển thị danh sách khóa học cần xét duyệt, hỗ trợ tìm kiếm và lọc theo trạng thái.
     */
    @GetMapping("/list")
    public String listCourses(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(required = false) CourseStatus status,
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "8") int size,
            Model model) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Page<CourseDto> coursePage = courseService.searchAndFilter(keyword, status, categoryId, pageable);

        model.addAttribute("coursePage", coursePage);
        model.addAttribute("keyword", keyword);
        model.addAttribute("status", status);
        model.addAttribute("categoryId", categoryId);
        model.addAttribute("categories", categoryService.findAll());

        return "manager/approval-course/course-list";
    }

    /**
     * GET /manager/course/detail/{id}
     * Hiển thị trang chi tiết của một khóa học.
     */
    @GetMapping("/detail/{id}")
    public String detailCourse(@PathVariable Integer id, Model model) {
        CourseDto course = courseService.getCourseDetail(id);
        model.addAttribute("course", course);
        return "manager/approval-course/course-detail";
    }

    /**
     * POST /manager/course/edit/{id}
     * Cập nhật trạng thái khóa học (PHÊ DUYỆT, TỪ CHỐI, ẨN).
     */
    @PostMapping("/edit/{id}")
    public String updateCourseStatus(
            @PathVariable Integer id,
            @RequestParam("status") CourseStatus status,
            @RequestParam(value = "rejectionReason", required = false) String rejectionReason,
            RedirectAttributes redirectAttributes) {

        courseService.updateCourseStatus(id, status, rejectionReason);
        redirectAttributes.addFlashAttribute("successMessage", "Cập nhật trạng thái khóa học thành công.");

        return "redirect:/manager/course/detail/" + id;
    }
}
