package vn.edu.fpt.dto;

import lombok.*;
import org.springframework.web.multipart.MultipartFile;

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
    private Boolean isFreePreview;
    private java.util.List<LessonMaterialDto> materials;
}
