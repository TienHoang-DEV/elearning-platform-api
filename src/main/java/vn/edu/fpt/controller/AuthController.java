package vn.edu.fpt.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import vn.edu.fpt.dto.LoginRequest;
import vn.edu.fpt.dto.RegisterRequest;
import vn.edu.fpt.entity.User;
import vn.edu.fpt.service.AuthService;

import java.util.Collections;
import java.util.List;

@Controller
public class AuthController {
    private final AuthService authService;

    public AuthController(
            AuthService authService) {

        this.authService = authService;
    }

    @GetMapping("/login_no")
    public String loginPage(Model model) {

        model.addAttribute(
                "loginRequest",
                new LoginRequest());
        System.out.println("LOGIN HIT");
        return "auth/login";
    }


    @GetMapping("/register")
    public String registerPage(
            Model model) {

        model.addAttribute(
                "registerRequest",
                new RegisterRequest());

        return "auth/register";
    }

    @PostMapping("/register")
    public String register(

            @Valid
            @ModelAttribute("registerRequest")
            RegisterRequest request,
            BindingResult result) {

        if(result.hasErrors()) {
            return "auth/register";
        }


        if (authService.existsByEmail(request.getEmail())) {
            result.rejectValue(
                    "email",
                    "duplicate",
                    "Email đã tồn tại");
        }

        if (authService.existsByPhone(request.getPhoneNumber())) {
            result.rejectValue(
                    "phoneNumber",
                    "duplicate",
                    "Số điện thoại đã tồn tại");
        }

        if (!request.isPasswordMatched()) {
            result.rejectValue(
                    "confirmPassword",
                    "mismatch",
                    "Mật khẩu xác nhận không khớp");
        }

        if (result.hasErrors()) {
            return "auth/register";
        }

        authService.register(request);

        return "redirect:/login_no";
    }

    @GetMapping("/view-current-role")
    @ResponseBody
    public String debug(Authentication authentication) {

        authentication.getAuthorities()
                .forEach(System.out::println);

        return authentication.getAuthorities().toString();
    }
}
