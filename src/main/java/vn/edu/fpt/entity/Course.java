package vn.edu.fpt.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Entity
@Table(name = "courses")
public class Course extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "instructor_id", nullable = false)
    private User instructor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(columnDefinition = "NVARCHAR(255)", nullable = false)
    private String title;

    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String description;

    @Column(name = "thumbnail_url", length = 500)
    private String thumbnailUrl;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(length = 20)
    private String level;

    @Column(length = 20)
    private String status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by")
    private User approvedBy;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "rejection_reason", columnDefinition = "NVARCHAR(1000)")
    private String rejectionReason;

    @Builder.Default
    @OneToMany(mappedBy = "course", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("position ASC")
    private Set<CourseSection> sections = new LinkedHashSet<>();

    @Builder.Default
    @OneToMany(mappedBy = "course", fetch = FetchType.LAZY)
    private Set<Enrollment> enrollments = new HashSet<>();

    @Builder.Default
    @OneToMany(mappedBy = "course", fetch = FetchType.LAZY)
    private Set<Feedback> feedbacks = new HashSet<>();

    public void addSection(CourseSection section) {
        sections.add(section);
        section.setCourse(this);
    }

    public void removeSection(CourseSection section) {
        sections.remove(section);
        section.setCourse(null);
    }

    public void addEnrollment(Enrollment enrollment) {
        enrollments.add(enrollment);
        enrollment.setCourse(this);
    }

    public void removeEnrollment(Enrollment enrollment) {
        enrollments.remove(enrollment);
        enrollment.setCourse(null);
    }

    public void addFeedback(Feedback feedback) {
        feedbacks.add(feedback);
        feedback.setCourse(this);
    }

    public void removeFeedback(Feedback feedback) {
        feedbacks.remove(feedback);
        feedback.setCourse(null);
    }

    public double getAverageRating() {
        if (feedbacks == null || feedbacks.isEmpty()) {
            return 0.0;
        }
        double sum = 0;
        int count = 0;
        for (Feedback fb : feedbacks) {
            if (fb.getRating() != null) {
                sum += fb.getRating();
                count++;
            }
        }
        if (count == 0) {
            return 0.0;
        }
        double avg = sum / count;
        return Math.round(avg * 10.0) / 10.0;
    }

    public int getRatingCount() {
        if (feedbacks == null) {
            return 0;
        }
        int count = 0;
        for (Feedback fb : feedbacks) {
            if (fb.getRating() != null) {
                count++;
            }
        }
        return count;
    }

    public int getStarCount(int star) {
        if (feedbacks == null) {
            return 0;
        }
        int count = 0;
        for (Feedback fb : feedbacks) {
            if (fb.getRating() != null && fb.getRating() == star) {
                count++;
            }
        }
        return count;
    }

    public double getStarPercentage(int star) {
        int total = getRatingCount();
        if (total == 0) {
            return 0.0;
        }
        double pct = (double) getStarCount(star) * 100.0 / total;
        return Math.round(pct * 10.0) / 10.0;
    }

    public int getTotalLessonsCount() {
        if (sections == null) {
            return 0;
        }
        int count = 0;
        for (CourseSection sec : sections) {
            if (sec.getLessons() != null) {
                count += sec.getLessons().size();
            }
        }
        return count;
    }

    public String getFirstLessonVideoUrl() {
        if (sections == null || sections.isEmpty()) return null;
        for (CourseSection sec : sections) {
            if (sec.getLessons() != null && !sec.getLessons().isEmpty()) {
                for (Lesson lesson : sec.getLessons()) {
                    if (lesson.getVideoUrl() != null && !lesson.getVideoUrl().trim().isEmpty()) {
                        return lesson.getVideoUrl();
                    }
                }
            }
        }
        return null;
    }

    public Integer getFirstLessonId() {
        if (sections == null || sections.isEmpty()) return null;
        for (CourseSection sec : sections) {
            if (sec.getLessons() != null && !sec.getLessons().isEmpty()) {
                for (Lesson lesson : sec.getLessons()) {
                    if (lesson.getVideoUrl() != null && !lesson.getVideoUrl().trim().isEmpty()) {
                        return lesson.getId();
                    }
                }
            }
        }
        return null;
    }

    public int getEnrollmentsCount() {
        return enrollments != null ? enrollments.size() : 0;
    }

    public String getThumbnailPath() {
        if (thumbnailUrl == null || thumbnailUrl.trim().isEmpty()) {
            return "/images/course_thumbnail.png";
        }
        if (thumbnailUrl.startsWith("http://") || thumbnailUrl.startsWith("https://")) {
            return thumbnailUrl;
        }
        
        // Trích xuất tên tệp tin nếu thumbnailUrl là một đường dẫn
        String fileName = thumbnailUrl;
        if (thumbnailUrl.contains("/")) {
            fileName = thumbnailUrl.substring(thumbnailUrl.lastIndexOf("/") + 1);
        }

        // Ánh xạ các hình ảnh mẫu trong database seed về ảnh local để hiển thị đẹp mắt
        if (fileName.equals("2aOboQZWp6Ov5iGTAZLOlCgmiOhOKsGgeQU1cI0O.jpg") || 
            fileName.equals("dsa-28tech.jpg") || 
            fileName.equals("spring-boot.jpg")) {
            return "/images/tech_course.png";
        }

        // Check if it is a known local image in static/images
        if (fileName.equals("acoustic_course.png") || 
            fileName.equals("course_thumbnail.png") ||
            fileName.equals("cuisine_course.png") ||
            fileName.equals("dome_hero.png") ||
            fileName.equals("eric_clapton_fan.png") ||
            fileName.equals("guitar_bolero_classical.png") ||
            fileName.equals("guitar_expert.png") ||
            fileName.equals("guitar_les_paul.png") ||
            fileName.equals("guitar_natural_acoustic.png") ||
            fileName.equals("guitar_stratocaster_sunburst.png") ||
            fileName.equals("guitar_sunburst_acoustic.png") ||
            fileName.equals("tech_course.png")) {
            return "/images/" + fileName;
        }

        if (thumbnailUrl.startsWith(vn.edu.fpt.util.AppConstants.AZURE_STORAGE_CONTAINER_COURSE_THUMBNAILS + "/")) {
            return vn.edu.fpt.util.AppConstants.AZURE_STORAGE_BASE_URL + "/" + thumbnailUrl;
        }

        // Default fallback: resolve from Azure Blob Storage course-thumbnails container
        return vn.edu.fpt.util.AppConstants.AZURE_STORAGE_BASE_URL + "/" + 
               vn.edu.fpt.util.AppConstants.AZURE_STORAGE_CONTAINER_COURSE_THUMBNAILS + "/" + 
               thumbnailUrl;
    }
}
