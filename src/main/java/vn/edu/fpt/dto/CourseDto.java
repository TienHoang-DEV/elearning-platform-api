package vn.edu.fpt.dto;

import lombok.*;
import java.math.BigDecimal;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseDto {
    private Integer id;
    private UserDto instructor;
    private CategoryDto category;
    private String title;
    private String description;
    private String thumbnailUrl;
    private BigDecimal price;
    private String level;
    private String status;
    private Set<CourseSectionDto> sections;
    private Set<FeedbackDto> feedbacks;

    private double averageRating;
    private int ratingCount;
    private int totalLessonsCount;
    private int enrollmentsCount;
    private String firstLessonVideoUrl;
    private Integer firstLessonId;
    private String thumbnailPath;
    private java.time.LocalDateTime createdAt;
    private java.time.LocalDateTime updatedAt;

    public int getStarCount(int star) {
        if (feedbacks == null) {
            return 0;
        }
        int count = 0;
        for (FeedbackDto fb : feedbacks) {
            if (fb.getRating() != null && fb.getRating() == star) {
                count++;
            }
        }
        return count;
    }

    public double getStarPercentage(int star) {
        int total = ratingCount;
        if (total == 0) {
            return 0.0;
        }
        double pct = (double) getStarCount(star) * 100.0 / total;
        return Math.round(pct * 10.0) / 10.0;
    }
}
