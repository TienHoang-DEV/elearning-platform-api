package vn.edu.fpt.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.fpt.dto.user.InstructorProfileViewDto;
import vn.edu.fpt.dto.user.ProfileDto;
import vn.edu.fpt.entity.User;
import vn.edu.fpt.repository.FeedbackRepository;
import vn.edu.fpt.util.AppConstants;
import vn.edu.fpt.util.SecurityUtils;

@Service
@Transactional
public class InstructorProfileService {
    private final UserService userService;
    private final FeedbackRepository feedbackRepository;

    public InstructorProfileService(UserService userService, FeedbackRepository feedbackRepository) {
        this.userService = userService;
        this.feedbackRepository = feedbackRepository;
    }

    public InstructorProfileViewDto getCurrentInstructorProfile(boolean editMode) {
        User currentUser = SecurityUtils.getCurrentUser();
        return buildProfileView(buildProfileDto(currentUser), currentUser, editMode);
    }

    public InstructorProfileViewDto getCurrentInstructorProfileForInvalidForm(ProfileDto profileDto) {
        User currentUser = SecurityUtils.getCurrentUser();
        profileDto.setAvatar_url(buildAvatarUrl(currentUser));
        profileDto.setEmail(currentUser.getEmail());
        return buildProfileView(profileDto, currentUser, true);
    }

    public void updateCurrentInstructorProfile(ProfileDto profileDto) {
        userService.updateProfileInstuctor(SecurityUtils.getCurrentUser(), profileDto);
    }

    private InstructorProfileViewDto buildProfileView(ProfileDto profileDto, User instructor, boolean editMode) {
        Double averageRating = feedbackRepository.getAverageRatingByInstructorId(instructor.getId());
        double safeAverage = averageRating != null ? averageRating : 0.0;
        double roundedAverage = Math.round(safeAverage * 10.0) / 10.0;
        int ratingStars = (int) Math.round(safeAverage);
        long ratingCount = feedbackRepository.countRatingsByInstructorId(instructor.getId());

        return new InstructorProfileViewDto(profileDto, roundedAverage, ratingStars, ratingCount, editMode);
    }

    private ProfileDto buildProfileDto(User user) {
        ProfileDto profileDto = new ProfileDto();
        profileDto.setFirstname(user.getFirstName());
        profileDto.setLastname(user.getLastName());
        profileDto.setBio(user.getBio());
        profileDto.setAvatar_url(buildAvatarUrl(user));
        profileDto.setEmail(user.getEmail());
        profileDto.setPhone(user.getPhone());
        return profileDto;
    }

    private String buildAvatarUrl(User user) {
        if (user.getAvatarUrl() == null || user.getAvatarUrl().isBlank()) {
            return null;
        }
        return AppConstants.AZURE_STORAGE_BASE_URL + "/"
                + AppConstants.AZURE_STORAGE_CONTAINER_USER_AVATARS + "/"
                + user.getAvatarUrl();
    }
}
