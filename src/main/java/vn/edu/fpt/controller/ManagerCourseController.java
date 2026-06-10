package vn.edu.fpt.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import vn.edu.fpt.dto.CourseDto;
import vn.edu.fpt.service.CourseService;

@Controller
@RequestMapping("/manager/course")
public class ManagerCourseController {

    private final CourseService courseService;

    public ManagerCourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    /**
     * GET /manager/course/list
     * Hiển thị danh sách khóa học cần xét duyệt, hỗ trợ tìm kiếm và lọc theo trạng thái.
     */
    @GetMapping("/list")
    public String listCourses(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "") String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "8") int size,
            Model model) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Page<CourseDto> coursePage = courseService.searchAndFilter(keyword, status, pageable);

        model.addAttribute("coursePage", coursePage);
        model.addAttribute("keyword", keyword);
        model.addAttribute("status", status);

        return "manager/approval-course/course-list";
    }
}
