package vn.edu.fpt.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.edu.fpt.dto.ForgotPasswordRequestDto;
import vn.edu.fpt.dto.ResetPasswordRequestDto;
import vn.edu.fpt.service.PasswordResetService;

@Controller
@RequiredArgsConstructor
public class ForgotPasswordController {

    private final PasswordResetService passwordResetService;

    @GetMapping("/forgot-password")
    public String forgotPasswordPage(Model model) {

        model.addAttribute("forgotPasswordRequest",
                new ForgotPasswordRequestDto());

        return "auth/forgot-password";
    }

    @PostMapping("/forgot-password")
    public String processForgotPassword(
            @Valid @ModelAttribute ForgotPasswordRequestDto request,
            BindingResult result,
            RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            return "auth/forgot-password";
        }

        passwordResetService.sendResetLink(request.getEmail());

        redirectAttributes.addFlashAttribute(
                "message",
                "If email exists, reset link has been sent"
        );

        return "redirect:/forgot-password";
    }

    @GetMapping("/reset-password")
    public String resetPasswordPage(
            @RequestParam String token,
            Model model) {

        passwordResetService.validateToken(token);

        ResetPasswordRequestDto request = new ResetPasswordRequestDto();
        request.setToken(token);

        model.addAttribute("resetPasswordRequest", request);

        return "auth/reset-password";
    }

    @PostMapping("/reset-password")
    public String processResetPassword(
            @Valid @ModelAttribute ResetPasswordRequestDto request,
            BindingResult result) {

        if (result.hasErrors()) {
            return "auth/eset-password";
        }

        passwordResetService.resetPassword(request);

        return "redirect:/login_no?resetSuccess";
    }

}
