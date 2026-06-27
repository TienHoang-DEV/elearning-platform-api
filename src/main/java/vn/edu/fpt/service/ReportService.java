package vn.edu.fpt.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.fpt.dto.ReportDetailDto;
import vn.edu.fpt.dto.ReportDto;
import vn.edu.fpt.mapper.DtoMapper;
import vn.edu.fpt.entity.Feedback;
import vn.edu.fpt.entity.Lesson;
import vn.edu.fpt.entity.Report;
import vn.edu.fpt.entity.User;
import vn.edu.fpt.enums.ReportStatus;
import vn.edu.fpt.enums.ReportType;
import vn.edu.fpt.repository.FeedbackRepository;
import vn.edu.fpt.repository.LessonRepository;
import vn.edu.fpt.repository.ReportRepository;
import vn.edu.fpt.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class ReportService {

    private final ReportRepository reportRepository;
    private final LessonRepository lessonRepository;
    private final FeedbackRepository feedbackRepository;
    private final UserRepository userRepository;
    private final DtoMapper dtoMapper;

    public Page<ReportDto> searchReports(ReportStatus status, ReportType type, String keyword, Pageable pageable) {
        return reportRepository.searchReports(status, type, keyword, pageable)
                .map(dtoMapper::toReportDto);
    }

    public Optional<Report> findById(Integer id) {
        return reportRepository.findById(id);
    }

    public ReportDetailDto getReportDetail(Integer id) {
        Report report = reportRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy báo cáo vi phạm với ID: " + id));

        ReportDetailDto.ReportDetailDtoBuilder builder = ReportDetailDto.builder().report(dtoMapper.toReportDto(report));

        if (report.getReportType() == ReportType.LESSON) {
            try {
                Lesson lesson = lessonRepository.findDetailById(report.getTargetId());
                if (lesson != null) {
                    builder.lessonTitle(lesson.getTitle())
                           .sectionTitle(lesson.getCourseSection().getTitle())
                           .courseTitle(lesson.getCourseSection().getCourse().getTitle())
                           .instructorName(lesson.getCourseSection().getCourse().getInstructor().getLastName() + " " + lesson.getCourseSection().getCourse().getInstructor().getFirstName())
                           .videoUrl(lesson.getVideoUrl());
                } else {
                    // Fallback if not found via fetch join
                    Optional<Lesson> optLesson = lessonRepository.findById(report.getTargetId());
                    if (optLesson.isPresent()) {
                        Lesson l = optLesson.get();
                        builder.lessonTitle(l.getTitle());
                        if (l.getCourseSection() != null) {
                            builder.sectionTitle(l.getCourseSection().getTitle());
                            if (l.getCourseSection().getCourse() != null) {
                                builder.courseTitle(l.getCourseSection().getCourse().getTitle());
                                if (l.getCourseSection().getCourse().getInstructor() != null) {
                                    builder.instructorName(l.getCourseSection().getCourse().getInstructor().getLastName() + " " + l.getCourseSection().getCourse().getInstructor().getFirstName());
                                }
                            }
                        }
                        builder.videoUrl(l.getVideoUrl());
                    }
                }
            } catch (Exception e) {
                // Handle fallback if lazy initialization issue happens
                Optional<Lesson> optLesson = lessonRepository.findById(report.getTargetId());
                optLesson.ifPresent(l -> builder.lessonTitle(l.getTitle()).videoUrl(l.getVideoUrl()));
            }
        } else if (report.getReportType() == ReportType.FEEDBACK) {
            Optional<Feedback> optFeedback = feedbackRepository.findById(report.getTargetId());
            if (optFeedback.isPresent()) {
                Feedback fb = optFeedback.get();
                builder.feedbackComment(fb.getComment())
                       .feedbackRating(fb.getRating())
                       .feedbackStatus(fb.getStatus())
                       .feedbackUser(fb.getUser().getLastName() + " " + fb.getUser().getFirstName())
                       .courseTitle(fb.getCourse().getTitle());
            }
        }

        return builder.build();
    }

    public void processReport(Integer id, ReportStatus status, Integer reviewerId) {
        Report report = reportRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy báo cáo vi phạm với ID: " + id));

        User reviewer = userRepository.findById(reviewerId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người kiểm duyệt với ID: " + reviewerId));

        report.setStatus(status);
        report.setReviewedBy(reviewer);
        report.setReviewedAt(LocalDateTime.now());
        reportRepository.save(report);

        // If the report is RESOLVED (approved violation), we take appropriate action on the target
        if (status == ReportStatus.RESOLVED) {
            if (report.getReportType() == ReportType.FEEDBACK) {
                Optional<Feedback> optFeedback = feedbackRepository.findById(report.getTargetId());
                if (optFeedback.isPresent()) {
                    Feedback fb = optFeedback.get();
                    fb.setStatus("HIDDEN");
                    feedbackRepository.save(fb);
                }
            } else if (report.getReportType() == ReportType.LESSON) {
                Optional<Lesson> optLesson = lessonRepository.findById(report.getTargetId());
                if (optLesson.isPresent()) {
                    Lesson lesson = optLesson.get();
                    lesson.setPublished(false);
                    lesson.setModerationStatus("REJECTED");
                    lessonRepository.save(lesson);
                }
            }
        }
    }
}
