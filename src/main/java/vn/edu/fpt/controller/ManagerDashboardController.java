package vn.edu.fpt.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import vn.edu.fpt.dto.ManagerDashboardDTO;
import vn.edu.fpt.dto.transaction_manager.TransactionCountByStatusDTO;
import vn.edu.fpt.dto.revenue_manager.MonthlyRevenueForManagerDTO;
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
    private final PaymentService paymentService;

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
        Double growthRate = managerDashboardService.getGrowthRate(monthlyRevenueForManagerDTO.getMonthlyRevenue());
        LocalDate today = LocalDate.now();
        model.addAttribute("monthlyRevenue", monthlyRevenueForManagerDTO);
        model.addAttribute("growthRate", growthRate);
        model.addAttribute("today", today);
        model.addAttribute("weeklyRevenueJson", mapper.writeValueAsString(monthlyRevenueForManagerDTO.getRevenueByPerWeek()));
        return "manager/revenue/revenue-list";
    }

    @GetMapping("/transaction-history/list")
    public String showTransaction(Model model, @RequestParam(required = false) String status, @RequestParam(required = false) LocalDate fromDate, @RequestParam(required = false) LocalDate toDate, @RequestParam(required = false) String keyword, @RequestParam(defaultValue = "0") int page) {

        TransactionCountByStatusDTO transactionCountByStatusDTO = paymentService.gettransactionCountByStatusDTO();
        Integer totalTransaction = transactionCountByStatusDTO.getAllTransaction();

        Page<TransactionListDTO> pageTransaction = paymentService.getTransactionByFilter(status, (fromDate == null ? null : fromDate.atStartOfDay()), (toDate == null ? null : toDate.atStartOfDay()), keyword, page);

        int startPage = (pageTransaction.getNumber() / AppConstants.NUMBER_PAGE_PER_BLOCK) * AppConstants.NUMBER_PAGE_PER_BLOCK;
        int endPage = Math.min(startPage + AppConstants.NUMBER_PAGE_PER_BLOCK - 1, pageTransaction.getTotalPages() - 1);

        model.addAttribute("status", status);
        model.addAttribute("fromDate", fromDate);
        model.addAttribute("toDate", toDate);
        model.addAttribute("keyword", keyword);

        model.addAttribute("pageTransaction", pageTransaction);
        model.addAttribute("startPage", startPage);
        model.addAttribute("endPage", endPage);
        model.addAttribute("transactionCountByStatusDTO", transactionCountByStatusDTO);
        model.addAttribute("totalTransaction", totalTransaction);
        return "manager/transaction-history/transaction-history";
    }
}
