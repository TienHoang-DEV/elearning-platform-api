package vn.edu.fpt.controller;


import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.edu.fpt.dto.user.ProfileDto;
import vn.edu.fpt.entity.User;
import vn.edu.fpt.exception.UserValidationException;
import vn.edu.fpt.service.CategoryService;
import vn.edu.fpt.service.UserService;
import vn.edu.fpt.util.SecurityUtils;

@RequestMapping("/instructor")
@Controller
public class InstructorProfileController {

    private final UserService userService;
    private final CategoryService categoryService;
    public InstructorProfileController(UserService userService, CategoryService categoryService) {
        this.userService = userService;
        this.categoryService = categoryService;
    }

    @GetMapping("/sidebar")
    public String Sidebar(Model model) {
        User user = SecurityUtils.getCurrentUser();
        ProfileDto profileDto = new ProfileDto();
        profileDto.setFirstname(user.getFirstName());
        profileDto.setLastname(user.getLastName());
        profileDto.setBio(user.getBio());
        profileDto.setAvatar_url(user.getAvatarUrl());
        profileDto.setEmail(user.getEmail());
        profileDto.setPhone(user.getPhone());
        model.addAttribute("instructor", profileDto);
        return "instructor_course/profile";
    }


    @PostMapping("/profiles")
    public String updateProfile(
            @Valid
            @ModelAttribute("instructor") ProfileDto profileDto,
            BindingResult result,
            RedirectAttributes redirectAttributes
    ) {
        if (result.hasErrors()) {
            return "instructor_course/profile";
        }
        try {

            User tmp = SecurityUtils.getCurrentUser();
            userService.updateProfileInstuctor(tmp, profileDto);
            redirectAttributes.addFlashAttribute("success", "Thay đổi thành công!!!");
            return "redirect:/instructor/sidebar";
        } catch (UserValidationException e) {
            result.rejectValue(e.getField(), "error", e.getMessage());
            return "instructor_course/profile";
        }

    }

}
