package vn.edu.fpt.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import vn.edu.fpt.dto.ManagerDashboardDTO;
import vn.edu.fpt.service.ManagerDashboardService;

@Controller
@RequestMapping("/manager")
public class ManagerDashboardController {

    private final ManagerDashboardService managerDashboardService;

    public ManagerDashboardController(ManagerDashboardService managerDashboardService) {
        this.managerDashboardService = managerDashboardService;
    }

    @GetMapping({"", "/dashboard"})
    public String dashboard(Model model) {
        ManagerDashboardDTO data = managerDashboardService.getDashboardData();

        // Add attributes to model
        model.addAttribute("pendingInstructors", data.getPendingInstructors());
        model.addAttribute("pendingCourses", data.getPendingCourses());
        model.addAttribute("pendingFeedbacks", data.getPendingFeedbacks());
        model.addAttribute("monthlyRevenue", data.getMonthlyRevenue());
        model.addAttribute("chartLabels", data.getChartLabels());
        model.addAttribute("chartData", data.getChartData());

        return "manager/dashboard/dashboard";
    }

    @GetMapping("/course/list")
    public String courseList() {
        return "manager/approval-course/course-list";
    }

    @GetMapping("/revenue/list")
    public String revenueList() {
        return "manager/revenue/revenue-list";
    }

    @GetMapping("/feedback-report/list")
    public String feedbackReportList() {
        return "manager/feedback-report/feedback-report-list";
    }
}
