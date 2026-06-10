package vn.edu.fpt.dto;

import lombok.Getter;
import lombok.Setter;
import vn.edu.fpt.enums.InstructorRequestStatus;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

@Getter
@Setter
public class InstructorRequestDTO {

    private Integer id;
    private String fullName;
    private String email;
    private String bio;
    private String cvUrl;
    private String description;
    private String rejectionReason;
    private InstructorRequestStatus status;
    private LocalDateTime createdAt;
    private String certificateUrl;
    private String nationalIdCardFront;
    private String nationalIdCardBack;

    /**
     * Trích tên file gốc từ URL blob.
     * URL format: https://.../container/UUID_originalFilename.pdf
     */
    public String getCvOriginalName() {
        return extractOriginalName(cvUrl);
    }

    public String getCertificateOriginalName() {
        return extractOriginalName(certificateUrl);
    }

    private static String extractOriginalName(String url) {
        if (url == null || url.isEmpty())
            return null;
        // Lấy phần sau dấu '/' cuối cùng
        String blobName = url.contains("/") ? url.substring(url.lastIndexOf('/') + 1) : url;
        // Bỏ query string (SAS token) nếu có
        if (blobName.contains("?")) {
            blobName = blobName.substring(0, blobName.indexOf('?'));
        }
        // URL decode (ví dụ: %20 → space, %E1%BB%93 → ồ)
        blobName = URLDecoder.decode(blobName, StandardCharsets.UTF_8);
        // Bỏ UUID prefix (format: xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx_)
        int underscoreIdx = blobName.indexOf('_');
        if (underscoreIdx >= 36) {
            return blobName.substring(underscoreIdx + 1);
        }
        return blobName;
    }
}
