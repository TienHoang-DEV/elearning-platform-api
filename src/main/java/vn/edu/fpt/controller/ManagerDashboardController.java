package vn.edu.fpt.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import vn.edu.fpt.dto.ManagerDashboardDTO;
import vn.edu.fpt.dto.revenue_manager.MonthlyRevenueForManagerDTO;
import vn.edu.fpt.service.ManagerDashboardService;

@Controller
@RequestMapping("/manager")
@RequiredArgsConstructor
public class ManagerDashboardController {

    private final ManagerDashboardService managerDashboardService;

    @GetMapping({"", "/dashboard"})
    public String dashboard(Model model) {
        ManagerDashboardDTO data = managerDashboardService.getDashboardData();

        // Add attributes to model
        model.addAttribute("totalInstructors", data.getTotalInstructors());
        model.addAttribute("pendingCourses", data.getPendingCourses());
        model.addAttribute("pendingFeedbacks", data.getPendingFeedbacks());
        model.addAttribute("monthlyRevenue", data.getMonthlyRevenue());
        model.addAttribute("chartLabels", data.getChartLabels());
        model.addAttribute("chartData", data.getChartData());

        return "manager/dashboard/dashboard";
    }


    @GetMapping("/feedback-report/list")
    public String feedbackReportList() {
        return "manager/feedback-report/feedback-report-list";
    }

    @GetMapping("/revenue/list")
    public String revenueList(Model model) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        MonthlyRevenueForManagerDTO monthlyRevenueForManagerDTO = managerDashboardService.getMonthlyRevenueForManager();
        model.addAttribute("monthlyRevenue", monthlyRevenueForManagerDTO);
        model.addAttribute("weeklyRevenueJson", mapper.writeValueAsString(monthlyRevenueForManagerDTO.getRevenueByPerWeek()));
        return "manager/revenue/revenue-list";
    }
}
