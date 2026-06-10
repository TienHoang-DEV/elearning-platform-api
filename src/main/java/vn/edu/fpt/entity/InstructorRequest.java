package vn.edu.fpt.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import vn.edu.fpt.enums.InstructorRequestStatus;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Entity
@Table(name = "instructor_requests")
public class InstructorRequest extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "cv_url", length = 500)
    private String cvUrl;

    @Column(name = "certificate_url", length = 500)
    private String certificateUrl;

    @Column(name = "national_id_card_front", length = 500)
    private String nationalIdCardFront;

    @Column(name = "national_id_card_back", length = 500)
    private String nationalIdCardBack;

    @Column(name = "rejection_reason", length = 1000)
    private String rejectionReason;

    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String description;

    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String bio;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private InstructorRequestStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by")
    private User reviewedBy;


}