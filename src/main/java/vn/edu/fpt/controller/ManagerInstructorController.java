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
import vn.edu.fpt.dto.InstructorRequestDTO;
import vn.edu.fpt.enums.InstructorRequestStatus;
import vn.edu.fpt.exception.BadRequestException;
import vn.edu.fpt.exception.ResourceNotFoundException;
import vn.edu.fpt.service.InstructorRequestService;

@Controller
@RequestMapping("/manager/instructor")
public class ManagerInstructorController {

    private final InstructorRequestService instructorRequestService;

    public ManagerInstructorController(InstructorRequestService instructorRequestService) {
        this.instructorRequestService = instructorRequestService;
    }

    /**
     * GET /manager/instructor/list
     * Hiển thị danh sách yêu cầu đăng ký giảng viên, hỗ trợ tìm kiếm và lọc theo trạng thái.
     */
    @GetMapping("/list")
    public String listInstructorRequests(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "") String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<InstructorRequestDTO> requestPage = instructorRequestService.searchAndFilter(keyword, status, pageable);

        model.addAttribute("requestPage", requestPage);
        model.addAttribute("keyword", keyword);
        model.addAttribute("status", status);

        return "manager/approval-instructor/instructor-list";
    }

    /**
     * GET /manager/instructor/detail/{id}
     * Hiển thị trang chi tiết (chỉ đọc) của một yêu cầu giảng viên.
     */
    @GetMapping("/detail/{id}")
    public String detailInstructorRequest(@PathVariable Integer id, Model model) {
        InstructorRequestDTO request = instructorRequestService.findDtoById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy yêu cầu với ID: " + id));

        model.addAttribute("request", request);
        return "manager/approval-instructor/instructor-detail";
    }

    /**
     * GET /manager/instructor/edit/{id}
     * Hiển thị trang xét duyệt (form hành động) cho một yêu cầu giảng viên.
     */
    @GetMapping("/edit/{id}")
    public String editInstructorRequest(@PathVariable Integer id, Model model) {
        InstructorRequestDTO request = instructorRequestService.findDtoById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy yêu cầu với ID: " + id));

        model.addAttribute("request", request);
        return "manager/approval-instructor/instructor-edit";
    }

    @PostMapping("/edit/{id}")
    public String reviewInstructorRequest(
            @PathVariable Integer id,
            @RequestParam("status") InstructorRequestStatus status,
            @RequestParam(required = false) String rejectionReason,
            RedirectAttributes redirectAttributes) {
        try {
            instructorRequestService.reviewRequest(id, status, rejectionReason);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật trạng thái yêu cầu thành công.");
            return "redirect:/manager/instructor/detail/" + id;
        } catch (BadRequestException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/manager/instructor/edit/" + id;
        }
    }
}
