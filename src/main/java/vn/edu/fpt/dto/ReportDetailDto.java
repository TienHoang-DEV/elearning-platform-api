package vn.edu.fpt.dto;

import lombok.*;
import vn.edu.fpt.entity.Report;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportDetailDto {
    private ReportDto report;
    
    // Lesson fields
    private String lessonTitle;
    private String sectionTitle;
    private String courseTitle;
    private String instructorName;
    private String videoUrl;
    
    // Feedback fields
    private String feedbackComment;
    private Integer feedbackRating;
    private String feedbackUser;
    private String feedbackStatus;

    public String getStatusLabel() {
        return (report != null && report.getStatus() != null) ? report.getStatus().getLabel() : "";
    }

    public String getReportTypeLabel() {
        return (report != null && report.getReportType() != null) ? report.getReportType().getLabel() : "";
    }
}
