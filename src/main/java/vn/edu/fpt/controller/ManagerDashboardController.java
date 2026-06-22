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
        model.addAttribute("totalInstructors", data.getTotalInstructors());
        model.addAttribute("totalLearners", data.getTotalLearners());
        model.addAttribute("pendingCourses", data.getPendingCourses());
        model.addAttribute("monthlyRevenue", data.getMonthlyRevenue());
        model.addAttribute("chartLabels", data.getChartLabels());
        model.addAttribute("chartData", data.getChartData());

        return "manager/dashboard/dashboard";
    }

    @GetMapping("/revenue/list")
    public String revenueList() {
        return "manager/revenue/revenue-list";
    }
}
