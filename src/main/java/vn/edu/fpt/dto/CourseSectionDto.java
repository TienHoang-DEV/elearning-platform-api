package vn.edu.fpt.dto;

import lombok.*;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseSectionDto {
    private Integer id;
    private String title;
    private Integer position;
    private Set<LessonDto> lessons;
}
