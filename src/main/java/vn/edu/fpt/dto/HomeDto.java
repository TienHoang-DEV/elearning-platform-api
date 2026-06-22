package vn.edu.fpt.dto;

import lombok.*;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HomeDto {
    private boolean hasFavorites;
    private CategoryDto parentCategory;
    private List<CategoryDto> favoriteChildren;
    private Map<Integer, List<CourseListDto>> coursesMap;
}
