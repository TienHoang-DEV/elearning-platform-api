package vn.edu.fpt.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import vn.edu.fpt.dto.course.CourseGrantDTO;
import vn.edu.fpt.dto.user.LearnerInfomationGrantAccessDTO;
import vn.edu.fpt.entity.Course;
import vn.edu.fpt.entity.User;
import vn.edu.fpt.enums.EnrollmentGrantReason;
import vn.edu.fpt.repository.UserRepository;
import vn.edu.fpt.service.CourseService;
import vn.edu.fpt.service.UserService;
import vn.edu.fpt.util.AppConstants;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class ManagerGrantAccessController {

    private final UserService userService;
    private final CourseService courseService;

    @GetMapping("/manager/grant-access")
    public String managerGrantAccess(Model model, @RequestParam(value = "keyword", required = false) String keyword, @RequestParam(value = "page", required = false, defaultValue = "0") Integer page) {

        Page<LearnerInfomationGrantAccessDTO> learners = userService.findAllLearnerByFilter(keyword, page);
        Integer startPage = (learners.getNumber() / AppConstants.NUMBER_PAGE_PER_BLOCK) * AppConstants.NUMBER_PAGE_PER_BLOCK;
        Integer endPage = Math.min(startPage + AppConstants.NUMBER_PAGE_PER_BLOCK - 1, learners.getTotalPages() - 1);

        List<CourseGrantDTO> courses = courseService.findAllCourseGrant();

        model.addAttribute("reasons", EnrollmentGrantReason.values());
        model.addAttribute("startPage", startPage);
        model.addAttribute("endPage", endPage);
        model.addAttribute("learners", learners);
        model.addAttribute("courses", courses);
        return "manager/grant-access/grant-access";
    }


}
