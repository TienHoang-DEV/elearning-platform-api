package vn.edu.fpt.dto;

import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LessonMaterialDto {
    private Integer id;
    private String fileName;   // Tên file gốc
    private String fileUrl;    // URL Azure blob
    private String fileType;   // pdf, docx, pptx, mp4...
    private Long fileSize;     // bytes
    private LocalDateTime createdAt;

    // Context: khóa học / bài giảng gắn với
    private Integer courseId;
    private String  courseTitle;
    private Integer lessonId;
    private String  lessonTitle;
    private String  sectionTitle;
    private String  courseStatus;
    private String  courseStatusLabel;
    private boolean deleteAllowed;
    private String  deleteReason;
}
