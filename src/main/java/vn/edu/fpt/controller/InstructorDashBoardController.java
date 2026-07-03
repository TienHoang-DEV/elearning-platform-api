package vn.edu.fpt.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import vn.edu.fpt.dto.revenueInstructor.DashboardInstructorDto;
import vn.edu.fpt.dto.user.ProfileDto;
import vn.edu.fpt.entity.User;
import vn.edu.fpt.service.DashboardInstructorService;
import vn.edu.fpt.util.SecurityUtils;

import javax.swing.text.Utilities;

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
        ProfileDto instructor = new ProfileDto();
        instructor.setFirstname(currentUser.getFirstName());
        instructor.setLastname(currentUser.getLastName());
        instructor.setEmail(currentUser.getEmail());
        instructor.setBio(currentUser.getBio());
        instructor.setAvatar_url(currentUser.getAvatarUrl());
        instructor.setPhone(currentUser.getPhone());


        DashboardInstructorDto stats = service.getStats(currentUser.getId(), period, year, month);

        model.addAttribute("instructor", instructor);
        model.addAttribute("stats", stats);
        model.addAttribute("selectedPeriod", period);
        model.addAttribute("selectedYear", year);
        model.addAttribute("selectedMonth", month);

        return "instructor_course/dashboard";
    }

}
