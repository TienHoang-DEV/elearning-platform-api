package vn.edu.fpt.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/manager")
public class ManagerDashboardController {

    @GetMapping({"", "/dashboard"})
    public String dashboard() {
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
