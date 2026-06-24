package vn.edu.fpt.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.edu.fpt.dto.ReportDetailDto;
import vn.edu.fpt.dto.ReportDto;
import vn.edu.fpt.enums.ReportStatus;
import vn.edu.fpt.enums.ReportType;
import vn.edu.fpt.service.ReportService;
import vn.edu.fpt.util.SecurityUtils;

@Controller
@RequestMapping("/manager/reports")
@RequiredArgsConstructor
public class ManagerReportController {

    private final ReportService reportService;

    @GetMapping("/list")
    public String listReports(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(required = false) ReportStatus status,
            @RequestParam(required = false) ReportType type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "8") int size,
            Model model) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Page<ReportDto> requestPage = reportService.searchReports(status, type, keyword, pageable);

        model.addAttribute("requestPage", requestPage);
        model.addAttribute("keyword", keyword);
        model.addAttribute("status", status);
        model.addAttribute("type", type);
        
        // Add enum arrays for filter select dropdowns
        model.addAttribute("statuses", ReportStatus.values());
        model.addAttribute("types", ReportType.values());

        return "manager/reports/list";
    }

    @GetMapping("/detail/{id}")
    public String detailReport(@PathVariable Integer id, Model model) {
        ReportDetailDto detailDto = reportService.getReportDetail(id);
        model.addAttribute("detail", detailDto);
        return "manager/reports/detail";
    }

    @PostMapping("/resolve/{id}")
    public String resolveReport(
            @PathVariable Integer id,
            @RequestParam ReportStatus status,
            RedirectAttributes redirectAttributes) {

        Integer currentUserId = SecurityUtils.getCurrentUserId();
        if (currentUserId == null) {
            redirectAttributes.addFlashAttribute("error", "Phiên làm việc đã hết hạn. Vui lòng đăng nhập lại.");
            return "redirect:/login";
        }

        try {
            reportService.processReport(id, status, currentUserId);
            String statusText = status == ReportStatus.RESOLVED ? "Phê duyệt vi phạm thành công" : "Từ chối báo cáo thành công";
            redirectAttributes.addFlashAttribute("success", statusText);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi xử lý báo cáo: " + e.getMessage());
        }

        return "redirect:/manager/reports/list";
    }
}
