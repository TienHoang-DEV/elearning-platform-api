package vn.edu.fpt.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import vn.edu.fpt.dto.revenueInstructor.DashboardInstructorDto;
import vn.edu.fpt.dto.revenueInstructor.RecentOrderDto;
import vn.edu.fpt.dto.user.ProfileDto;
import vn.edu.fpt.entity.User;
import vn.edu.fpt.service.DashboardInstructorService;
import vn.edu.fpt.util.AppConstants;
import vn.edu.fpt.util.SecurityUtils;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/instructor")
public class InstructorDashBoardController {
    private final DashboardInstructorService service;

    @GetMapping("/dashboard")
    public String dashboard(@RequestParam(defaultValue = "MONTH") String period,
                            @RequestParam(required = false) Integer year,
                            @RequestParam(required = false) Integer month,
                            Model model) {

        User currentUser = SecurityUtils.getCurrentUser();

        DashboardInstructorDto stats = service.getStats(currentUser.getId(), period, year, month);

        model.addAttribute("instructor", toProfileDto(currentUser));
        model.addAttribute("stats", stats);
        model.addAttribute("selectedPeriod", period);
        model.addAttribute("selectedYear", year);
        model.addAttribute("selectedMonth", month);

        return "instructor_course/dashboard";
    }

    @GetMapping("/orders")
    public String orders(Model model) {
        User currentUser = SecurityUtils.getCurrentUser();
        // Instructor orders: list full khi bam "Xem tat ca" tu dashboard.
        model.addAttribute("instructor", toProfileDto(currentUser));
        model.addAttribute("orders", service.getInstructorOrders(currentUser.getId()));
        model.addAttribute("courseThumbnailBaseUrl", courseThumbnailBaseUrl());
        return "instructor_course/orders";
    }

    @GetMapping("/orders/{orderId}")
    public String orderDetails(@PathVariable Integer orderId, Model model) {
        User currentUser = SecurityUtils.getCurrentUser();
        List<RecentOrderDto> orderItems = service.getInstructorOrderDetails(currentUser.getId(), orderId);
        // Instructor orders: neu order khong co course cua instructor nay thi van render empty, khong lo thong tin order cua nguoi khac.
        model.addAttribute("instructor", toProfileDto(currentUser));
        model.addAttribute("orderId", orderId);
        model.addAttribute("orderItems", orderItems);
        model.addAttribute("order", orderItems.isEmpty() ? null : orderItems.get(0));
        model.addAttribute("courseThumbnailBaseUrl", courseThumbnailBaseUrl());
        return "instructor_course/order_detail";
    }

    private String courseThumbnailBaseUrl() {
        return AppConstants.AZURE_STORAGE_BASE_URL + "/" + AppConstants.AZURE_STORAGE_CONTAINER_COURSE_THUMBNAILS + "/";
    }

    private ProfileDto toProfileDto(User user) {
        ProfileDto instructor = new ProfileDto();
        instructor.setFirstname(user.getFirstName());
        instructor.setLastname(user.getLastName());
        instructor.setEmail(user.getEmail());
        instructor.setBio(user.getBio());
        instructor.setAvatar_url(user.getAvatarUrl());
        instructor.setPhone(user.getPhone());
        return instructor;
    }
}
