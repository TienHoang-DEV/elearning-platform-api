package vn.edu.fpt.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import vn.edu.fpt.dto.manager.ManagerDashboardDTO;
import vn.edu.fpt.dto.transaction_manager.TransactionCountByStatusDTO;
import vn.edu.fpt.dto.revenue_manager.MonthlyRevenueForManagerDTO;
import vn.edu.fpt.dto.transaction_manager.TransactionDetailDTO;
import vn.edu.fpt.dto.transaction_manager.TransactionListDTO;
import vn.edu.fpt.enums.PaymentStatus;
import vn.edu.fpt.service.ManagerDashboardService;
import vn.edu.fpt.service.payment.PaymentService;
import vn.edu.fpt.util.AppConstants;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Controller
@RequestMapping("/manager")
@RequiredArgsConstructor
public class ManagerDashboardController {

    private final ManagerDashboardService managerDashboardService;

    @GetMapping({"", "/dashboard"})
    public String dashboard(Model model) {
        ManagerDashboardDTO data = managerDashboardService.getDashboardData();

        // Thêm các thuộc tính vào model
        model.addAttribute("totalInstructors", data.getTotalInstructors());
        model.addAttribute("totalLearners", data.getTotalLearners());
        model.addAttribute("pendingCourses", data.getPendingCourses());
        model.addAttribute("monthlyRevenue", data.getMonthlyRevenue());
        model.addAttribute("chartLabels", data.getChartLabels());
        model.addAttribute("chartData", data.getChartData());

        return "manager/dashboard/dashboard";
    }



    @GetMapping("/revenue/list")
    public String revenueList(Model model) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        MonthlyRevenueForManagerDTO monthlyRevenueForManagerDTO = managerDashboardService.getMonthlyRevenueForManager();
        Double growthRate = managerDashboardService.getGrowthRate(monthlyRevenueForManagerDTO.getMonthlyRevenue());
        LocalDate today = LocalDate.now();
        model.addAttribute("monthlyRevenue", monthlyRevenueForManagerDTO);
        model.addAttribute("growthRate", growthRate);
        model.addAttribute("today", today);
        model.addAttribute("weeklyRevenueJson", mapper.writeValueAsString(monthlyRevenueForManagerDTO.getRevenueByPerWeek()));
        return "manager/revenue/revenue-list";
    }


}
