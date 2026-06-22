package vn.edu.fpt.dto;

import lombok.*;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseListDto {
    private Integer id;
    private String title;
    private String thumbnailUrl;
    private BigDecimal price;
    private String level;
    private String instructorFirstName;
    private String instructorLastName;
    private Integer categoryId;
    private String categoryName;
    private Double averageRating;
    private Long ratingCount;
    private Long totalLessonsCount;
    private Long enrollmentsCount;

    // Helper method to keep compatibility with Thymeleaf template referring to course.instructor.lastName / firstName
    public UserDto getInstructor() {
        return UserDto.builder()
                .firstName(instructorFirstName)
                .lastName(instructorLastName)
                .build();
    }

    // Helper method to keep compatibility with Thymeleaf template referring to course.category.id
    public CategoryDto getCategory() {
        if (categoryId == null) {
            return null;
        }
        return CategoryDto.builder()
                .id(categoryId)
                .name(categoryName)
                .build();
    }

    // Helper method to keep compatibility with Thymeleaf template referring to course.getTotalLessonsCount()
    public int getTotalLessonsCount() {
        return totalLessonsCount != null ? totalLessonsCount.intValue() : 0;
    }

    // Helper method to keep compatibility with Thymeleaf template referring to course.averageRating
    public double getAverageRating() {
        return averageRating != null ? Math.round(averageRating * 10.0) / 10.0 : 0.0;
    }

    // Helper method to keep compatibility with Thymeleaf template referring to course.ratingCount
    public int getRatingCount() {
        return ratingCount != null ? ratingCount.intValue() : 0;
    }

    // Helper method to keep compatibility with Thymeleaf template referring to course.enrollmentsCount
    public int getEnrollmentsCount() {
        return enrollmentsCount != null ? enrollmentsCount.intValue() : 0;
    }

    // Resolved path for course thumbnail
    public String getThumbnailPath() {
        if (thumbnailUrl == null || thumbnailUrl.trim().isEmpty()) {
            return "/images/course_thumbnail.png";
        }
        if (thumbnailUrl.startsWith("http://") || thumbnailUrl.startsWith("https://")) {
            return thumbnailUrl;
        }
        
        String fileName = thumbnailUrl;
        if (thumbnailUrl.contains("/")) {
            fileName = thumbnailUrl.substring(thumbnailUrl.lastIndexOf("/") + 1);
        }

        if (fileName.equals("2aOboQZWp6Ov5iGTAZLOlCgmiOhOKsGgeQU1cI0O.jpg") || 
            fileName.equals("dsa-28tech.jpg") || 
            fileName.equals("spring-boot.jpg")) {
            return "/images/tech_course.png";
        }

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

        return vn.edu.fpt.util.AppConstants.AZURE_STORAGE_BASE_URL + "/" + 
               vn.edu.fpt.util.AppConstants.AZURE_STORAGE_CONTAINER_COURSE_THUMBNAILS + "/" + 
               thumbnailUrl;
    }
}
