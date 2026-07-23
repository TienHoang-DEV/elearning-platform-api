package vn.edu.fpt.service.cloud;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import vn.edu.fpt.entity.CourseSection;
import vn.edu.fpt.entity.User;
import vn.edu.fpt.service.section.CourseSectionService;
import vn.edu.fpt.util.AppConstants;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class VideoUploadService {

    private final CourseSectionService courseSectionService;
    private final AzureBlobService azureBlobService;

    public Map<String, String> generateDirectUploadUrl(String fileName, Integer sectionId, User user) {
        validateInstructorOwnsSection(sectionId, user);
        String blobName = normalizeOriginalFileName(fileName);
        String uploadUrl = azureBlobService.generateUploadSasUrl(AppConstants.AZURE_STORAGE_CONTAINER_VIDEOS, blobName);
        return Map.of(
                "uploadUrl", uploadUrl,
                "blobName", blobName
        );
    }

    private String normalizeOriginalFileName(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            return "lesson-video.mp4";
        }
        return fileName.trim();
    }

    private void validateInstructorOwnsSection(Integer sectionId, User user) {
        if (user == null) {
            throw new AccessDeniedException("Bạn cần đăng nhập để tải video lên.");
        }

        CourseSection section = courseSectionService.findBySectionId(sectionId);
        if (section == null
                || section.getCourse() == null
                || section.getCourse().getInstructor() == null
                || !section.getCourse().getInstructor().getId().equals(user.getId())) {
            throw new AccessDeniedException("Bạn không có quyền tải video lên chương này.");
        }
    }
}
