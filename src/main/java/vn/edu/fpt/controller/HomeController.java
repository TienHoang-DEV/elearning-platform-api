package vn.edu.fpt.controller;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/home")
    public String home() {
        System.out.println("HOME AUTH = " +
                SecurityContextHolder.getContext().getAuthentication());
        return "home/home";
    }
}
