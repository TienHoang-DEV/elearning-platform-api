package vn.edu.fpt.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import vn.edu.fpt.dto.user.StudentProfileDashboardDto;
import vn.edu.fpt.entity.Enrollment;
import vn.edu.fpt.entity.User;
import vn.edu.fpt.mapper.DtoMapper;
import vn.edu.fpt.repository.UserRepository;
import vn.edu.fpt.service.cloud.AzureBlobService;
import vn.edu.fpt.util.AppConstants;

import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class StudentProfileService {

    private final DtoMapper dtoMapper;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AzureBlobService azureBlobService;

    public StudentProfileDashboardDto getDashboardData(User user) {
        int enrollmentsCount = user.getEnrollments().size();
        long certificatesCount = 0;
        for (Enrollment e : user.getEnrollments()) {
            if (e.getProgressPercent() != null && e.getProgressPercent().doubleValue() >= 100) {
                certificatesCount++;
            }
        }
                
        int totalHours = 0;
        for (Enrollment en : user.getEnrollments()) {
            double pct = en.getProgressPercent() != null ? en.getProgressPercent().doubleValue() : 0.0;
            totalHours += (int) (pct * 8.0 / 100.0);
        }
        if (totalHours == 0 && enrollmentsCount > 0) {
            totalHours = 2;
        }

        return StudentProfileDashboardDto.builder()
                .currentUser(dtoMapper.toUserDto(user))
                .enrollmentsCount(enrollmentsCount)
                .certificatesCount(certificatesCount)
                .studyHours(totalHours)
                .build();
    }

    public User updateProfile(User user, String firstName, String lastName, String email, String phone, 
                              MultipartFile avatarFile, boolean deleteAvatar) throws Exception {
        if (firstName == null || firstName.trim().isEmpty() || lastName == null || lastName.trim().isEmpty() || email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Họ tên và địa chỉ email không được để trống!");
        }

        // Check email duplication
        Optional<User> existingUserOpt = userRepository.findByEmail(email.trim());
        if (existingUserOpt.isPresent() && !existingUserOpt.get().getId().equals(user.getId())) {
            throw new IllegalArgumentException("Địa chỉ email này đã được sử dụng bởi một tài khoản khác!");
        }

        user.setFirstName(firstName.trim());
        user.setLastName(lastName.trim());
        user.setEmail(email.trim());
        user.setPhone(phone != null ? phone.trim() : null);

        if (deleteAvatar) {
            user.setAvatarUrl(null);
        } else if (avatarFile != null && !avatarFile.isEmpty()) {
            if (avatarFile.getSize() > 1024 * 1024) {
                throw new IllegalArgumentException("Kích thước ảnh đại diện không được vượt quá 1MB!");
            }
            try {
                String uploadedUrl = azureBlobService.saveFile(avatarFile, AppConstants.AZURE_STORAGE_CONTAINER_USER_AVATARS);
                user.setAvatarUrl(uploadedUrl);
            } catch (Exception e) {
                throw new Exception("Lỗi xảy ra trong quá trình lưu ảnh đại diện lên cloud storage.");
            }
        }

        return userRepository.save(user);
    }

    public void changePassword(User user, String oldPassword, String newPassword, String confirmPassword) {
        user = userRepository.findById(user.getId()).orElseThrow(() -> new IllegalArgumentException("Không tìm thấy thông tin tài khoản."));

        if (user.getPasswordHash() == null) {
            throw new IllegalArgumentException("Tài khoản của bạn không sử dụng mật khẩu.");
        }

        if (oldPassword == null || oldPassword.isEmpty() || newPassword == null || newPassword.isEmpty() || confirmPassword == null || confirmPassword.isEmpty()) {
            throw new IllegalArgumentException("Vui lòng nhập đầy đủ thông tin mật khẩu.");
        }

        if (!passwordEncoder.matches(oldPassword, user.getPasswordHash())) {
            throw new IllegalArgumentException("Mật khẩu hiện tại không chính xác.");
        }

        if (!newPassword.equals(confirmPassword)) {
            throw new IllegalArgumentException("Mật khẩu mới và xác nhận mật khẩu không khớp.");
        }

        if (newPassword.length() < 6) {
            throw new IllegalArgumentException("Mật khẩu mới phải có ít nhất 6 ký tự.");
        }

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }
}
