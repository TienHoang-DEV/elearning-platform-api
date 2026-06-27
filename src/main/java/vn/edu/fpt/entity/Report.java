package vn.edu.fpt.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import vn.edu.fpt.enums.ReportStatus;
import vn.edu.fpt.enums.ReportType;

import java.time.LocalDateTime;
import vn.edu.fpt.enums.LessonReportReason;
import vn.edu.fpt.enums.FeedbackReportReason;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Entity
@Table(name = "reports")
public class Report extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_id", nullable = false)
    private User reporter;

    @Enumerated(EnumType.STRING)
    @Column(name = "report_type", length = 20, nullable = false)
    private ReportType reportType;

    @Column(name = "target_id", nullable = false)
    private Integer targetId;

    @Column(name = "reason_type", length = 50, nullable = false)
    private String reasonType;

    @Column(length = 2000)
    private String description;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private ReportStatus status = ReportStatus.PENDING;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by")
    private User reviewedBy;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    public String getFriendlyReason() {
        if (reasonType == null) {
            return "";
        }
        if (reportType == ReportType.LESSON) {
            try {
                return LessonReportReason.valueOf(reasonType).getDisplayName();
            } catch (Exception e) {
                return reasonType;
            }
        } else if (reportType == ReportType.FEEDBACK) {
            try {
                return FeedbackReportReason.valueOf(reasonType).getDisplayName();
            } catch (Exception e) {
                return reasonType;
            }
        }
        return reasonType;
    }
}
