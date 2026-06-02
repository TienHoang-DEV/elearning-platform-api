package vn.edu.fpt.dto;

import lombok.Getter;
import lombok.Setter;
import vn.edu.fpt.enums.InstructorRequestStatus;

import java.time.LocalDateTime;

@Getter
@Setter
public class InstructorRequestDTO {

    private Integer id;
    private String fullName;
    private String email;
    private String avatarUrl;
    private String bio;
    private String cvUrl;
    private String certificateUrl;
    private String description;
    private String rejectionReason;
    private InstructorRequestStatus status;
    private LocalDateTime createdAt;
}
