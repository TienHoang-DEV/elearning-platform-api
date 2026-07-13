package vn.edu.fpt.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.Optional;

import vn.edu.fpt.dto.user.StudentProfileDashboardDto;
import vn.edu.fpt.entity.User;
import vn.edu.fpt.repository.UserRepository;
import vn.edu.fpt.service.StudentProfileService;
import vn.edu.fpt.service.cloud.AzureBlobService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Controller
public class StudentProfileController {

    private final UserRepository userRepository;
    private final StudentProfileService studentProfileService;
    private final AzureBlobService azureBlobService;

    public StudentProfileController(UserRepository userRepository,
                                    StudentProfileService studentProfileService,
                                    AzureBlobService azureBlobService) {
        this.userRepository = userRepository;
        this.studentProfileService = studentProfileService;
        this.azureBlobService = azureBlobService;
    }

    private User getSessionUser() {
        return vn.edu.fpt.util.SecurityUtils.getCurrentUser();
    }


    @GetMapping("/student/profile")
    public String showStudentProfile(Model model) {
        User user = getSessionUser();
        if (user == null) {
            return "redirect:/login";
        }
        
        StudentProfileDashboardDto dashboardData = studentProfileService.getDashboardData(user);

        model.addAttribute("currentUser", dashboardData.getCurrentUser());
        model.addAttribute("enrollmentsCount", dashboardData.getEnrollmentsCount());
        model.addAttribute("certificatesCount", dashboardData.getCertificatesCount());
        model.addAttribute("studyHours", dashboardData.getStudyHours());
        
        return "student_profile/student_profile";
    }

    @PostMapping("/student/profile/update")
    public String updateStudentProfile(
            @RequestParam("firstName") String firstName,
            @RequestParam("lastName") String lastName,
            @RequestParam("email") String email,
            @RequestParam(value = "phone", required = false) String phone,
            @RequestParam(value = "avatarFile", required = false) MultipartFile avatarFile,
            @RequestParam(value = "deleteAvatar", required = false, defaultValue = "false") boolean deleteAvatar,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes) {
        
        User user = getSessionUser();
        if (user == null) {
            return "redirect:/login";
        }

        if (firstName == null || firstName.trim().isEmpty() || lastName == null || lastName.trim().isEmpty() || email == null || email.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Họ tên và địa chỉ email không được để trống!");
            return "redirect:/student/profile";
        }

        // Check email duplication
        Optional<User> existingUserOpt = userRepository.findByEmail(email.trim());
        if (existingUserOpt.isPresent() && !existingUserOpt.get().getId().equals(user.getId())) {
            redirectAttributes.addFlashAttribute("error", "Địa chỉ email này đã được sử dụng bởi một tài khoản khác!");
            return "redirect:/student/profile";
        }

        user.setFirstName(firstName.trim());
        user.setLastName(lastName.trim());
        user.setEmail(email.trim());
        user.setPhone(phone != null ? phone.trim() : null);

        if (deleteAvatar) {
            user.setAvatarUrl(null);
        } else if (avatarFile != null && !avatarFile.isEmpty()) {
            if (avatarFile.getSize() > 1024 * 1024) {
                redirectAttributes.addFlashAttribute("error", "Kích thước ảnh đại diện không được vượt quá 1MB!");
                return "redirect:/student/profile";
            }
            try {
                String uploadedUrl = azureBlobService.saveFile(avatarFile, vn.edu.fpt.util.AppConstants.AZURE_STORAGE_CONTAINER_USER_AVATARS);
                user.setAvatarUrl(uploadedUrl);
            } catch (Exception e) {
                redirectAttributes.addFlashAttribute("error", "Lỗi xảy ra trong quá trình lưu ảnh đại diện lên cloud storage.");
                return "redirect:/student/profile";
            }
        }

        userRepository.save(user);

        // Update session
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.setAttribute("user", user);
        }

        redirectAttributes.addFlashAttribute("success", "Cập nhật thông tin hồ sơ tài khoản thành công!");
        return "redirect:/student/profile";
    }


}
