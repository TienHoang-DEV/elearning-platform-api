package vn.edu.fpt.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LessonDto {
    private Integer id;
    private String title;
    private String videoUrl;
    private Integer durationSeconds;
    private Integer position;
}
