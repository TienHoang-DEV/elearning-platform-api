package vn.edu.fpt.dto;

import lombok.*;
import vn.edu.fpt.enums.ReportStatus;
import vn.edu.fpt.enums.ReportType;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportDto {
    private Integer id;
    private UserDto reporter;
    private ReportType reportType;
    private Integer targetId;
    private String reasonType;
    private String description;
    private ReportStatus status;
    private UserDto reviewedBy;
    private LocalDateTime reviewedAt;
    private LocalDateTime createdAt;
    private String friendlyReason;

    public String getStatusLabel() {
        return status != null ? status.getLabel() : "";
    }

    public String getReportTypeLabel() {
        return reportType != null ? reportType.getLabel() : "";
    }
}
