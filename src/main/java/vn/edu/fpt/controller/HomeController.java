package vn.edu.fpt.controller;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import vn.edu.fpt.entity.User;
import vn.edu.fpt.util.SecurityUtils;

@Controller
public class HomeController {

    @GetMapping("/home")
    public String home(Model model) {
        User currentUser = SecurityUtils.getCurrentUser();

        System.out.println(currentUser);

        model.addAttribute("currentUser", currentUser);

        return "home/home";
    }
}
