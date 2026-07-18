package vn.edu.fpt.controller;

import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.edu.fpt.dto.user.ProfileDto;
import vn.edu.fpt.entity.User;
import vn.edu.fpt.exception.UserValidationException;
import vn.edu.fpt.service.UserService;
import vn.edu.fpt.util.AppConstants;
import vn.edu.fpt.util.SecurityUtils;

@RequestMapping("/instructor")
@Controller
public class InstructorProfileController {

    private final UserService userService;

    public InstructorProfileController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/sidebar")
    public String viewProfile(Model model) {
        // Profile view: man Thong tin cua toi mac dinh chi hien thi thong tin, khong hien form edit.
        model.addAttribute("instructor", buildProfileDto(SecurityUtils.getCurrentUser()));
        model.addAttribute("editMode", false);
        return "instructor_course/profile";
    }

    @GetMapping("/profile/edit")
    public String editProfile(Model model) {
        // Profile edit: bam nut Chinh sua moi vao man form edit rieng, khong dung popup.
        model.addAttribute("instructor", buildProfileDto(SecurityUtils.getCurrentUser()));
        model.addAttribute("editMode", true);
        return "instructor_course/profile";
    }

    private ProfileDto buildProfileDto(User user) {
        ProfileDto profileDto = new ProfileDto();
        profileDto.setFirstname(user.getFirstName());
        profileDto.setLastname(user.getLastName());
        profileDto.setBio(user.getBio());
        if (user.getAvatarUrl() != null && !user.getAvatarUrl().isBlank()) {
            profileDto.setAvatar_url(AppConstants.AZURE_STORAGE_BASE_URL + "/" + AppConstants.AZURE_STORAGE_CONTAINER_USER_AVATARS + "/" + user.getAvatarUrl());
        }
        profileDto.setEmail(user.getEmail());
        profileDto.setPhone(user.getPhone());
        return profileDto;
    }

    @PostMapping("/profiles")
    public String updateProfile(
            @Valid @ModelAttribute("instructor") ProfileDto profileDto,
            BindingResult result,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (result.hasErrors()) {
            // Profile edit: neu validate loi thi giu du lieu da nhap va chi bao loi tai field do.
            profileDto.setAvatar_url(buildProfileDto(SecurityUtils.getCurrentUser()).getAvatar_url());
            model.addAttribute("editMode", true);
            model.addAttribute("error", "Vui lòng kiểm tra lại thông tin chưa hợp lệ.");
            return "instructor_course/profile";
        }

        try {
            userService.updateProfileInstuctor(SecurityUtils.getCurrentUser(), profileDto);
            redirectAttributes.addFlashAttribute("success", "Thay đổi thành công!");
            return "redirect:/instructor/sidebar";
        } catch (UserValidationException e) {
            result.rejectValue(e.getField(), "error", e.getMessage());
            profileDto.setAvatar_url(buildProfileDto(SecurityUtils.getCurrentUser()).getAvatar_url());
            model.addAttribute("editMode", true);
            model.addAttribute("error", e.getMessage());
            return "instructor_course/profile";
        }
    }
}
