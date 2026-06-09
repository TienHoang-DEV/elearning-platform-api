package vn.edu.fpt.controller;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import vn.edu.fpt.dto.InstructorRequestDTO;
import vn.edu.fpt.entity.User;
import vn.edu.fpt.service.InstructorRequestService;
import vn.edu.fpt.util.SecurityUtils;

import java.util.List;

@Controller
public class HomeController {

    private final InstructorRequestService instructorRequestService;

    public HomeController(InstructorRequestService instructorRequestService){
        this.instructorRequestService = instructorRequestService;
    }
    @GetMapping("/home")
    public String home(Model model) {
        User currentUser = SecurityUtils.getCurrentUser();

        System.out.println(currentUser);

        model.addAttribute("currentUser", currentUser);

        return "home/home";
    }

    @GetMapping("/instructor-request")
    public String instructorRequestPage(Model model) {
        User currentUser = SecurityUtils.getCurrentUser();

        System.out.println(currentUser);

        model.addAttribute("currentUser", currentUser);
        model.addAttribute("instructorRequest", new InstructorRequestDTO());
        return "home/instructor-request";
    }

    @PostMapping("/instructor-request")
    public String submitRequest(
            @ModelAttribute("instructorRequest") InstructorRequestDTO dto,
            @RequestParam("cvFile") MultipartFile cvFile,
            @RequestParam("idFront") MultipartFile idFront,
            @RequestParam("idBack") MultipartFile idBack,
            @RequestParam("certificateFiles") MultipartFile certificateFiles
    ) {

        instructorRequestService.submitRequest(
                dto,
                cvFile,
                idFront,
                idBack,
                certificateFiles
        );

        return "home/request-success";
    }
}
