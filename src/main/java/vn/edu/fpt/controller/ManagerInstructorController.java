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
import vn.edu.fpt.dto.UserDto;
import vn.edu.fpt.entity.Course;
import vn.edu.fpt.entity.User;
import vn.edu.fpt.exception.ResourceNotFoundException;
import vn.edu.fpt.mapper.DtoMapper;
import vn.edu.fpt.repository.CourseRepository;
import vn.edu.fpt.repository.UserRepository;
import vn.edu.fpt.enums.UserStatus;

import java.util.List;

@Controller
@RequestMapping("/manager/instructor")
public class ManagerInstructorController {

    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final DtoMapper dtoMapper;

    public ManagerInstructorController(UserRepository userRepository,
                                       CourseRepository courseRepository,
                                       DtoMapper dtoMapper) {
        this.userRepository = userRepository;
        this.courseRepository = courseRepository;
        this.dtoMapper = dtoMapper;
    }

    /**
     * GET /manager/instructor/list
     * Hiển thị danh sách giảng viên, hỗ trợ tìm kiếm và lọc theo trạng thái tài khoản.
     */
    @GetMapping("/list")
    public String listInstructors(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "") UserStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "8") int size,
            Model model) {


        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Page<User> instructorPage = userRepository.searchAndFilterInstructors(keyword, status, pageable);
        Page<UserDto> requestPage = instructorPage.map(dtoMapper::toUserDto);

        model.addAttribute("requestPage", requestPage);
        model.addAttribute("keyword", keyword);
        model.addAttribute("status", status);

        return "manager/approval-instructor/instructor-list";
    }

    /**
     * GET /manager/instructor/detail/{id}
     * Hiển thị trang chi tiết của một giảng viên.
     */
    @GetMapping("/detail/{id}")
    public String detailInstructor(@PathVariable Integer id, Model model) {
        User instructor = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy giảng viên với ID: " + id));
        UserDto request = dtoMapper.toUserDto(instructor);
        List<Course> courses = courseRepository.findByInstructor(instructor);

        model.addAttribute("request", request);
        model.addAttribute("courses", courses);
        return "manager/approval-instructor/instructor-detail";
    }

    @PostMapping("/edit/{id}")
    public String updateInstructorStatus(
            @PathVariable Integer id,
            @RequestParam("status") String status,
            RedirectAttributes redirectAttributes) {
        User instructor = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy giảng viên với ID: " + id));

        try {
            UserStatus userStatus = UserStatus.valueOf(status);
            instructor.setStatus(userStatus);
            userRepository.save(instructor);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật trạng thái tài khoản giảng viên thành công.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Trạng thái không hợp lệ.");
        }

        return "redirect:/manager/instructor/detail/" + id;
    }
}
