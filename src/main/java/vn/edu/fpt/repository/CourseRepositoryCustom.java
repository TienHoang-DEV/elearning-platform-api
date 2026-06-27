package vn.edu.fpt.repository;

import org.springframework.data.domain.Page;
import vn.edu.fpt.dto.course.CourseListDto;
import java.util.List;

public interface CourseRepositoryCustom {
    Page<CourseListDto> getPagedCoursesSummary(
            String search,
            Integer categoryId,
            List<Double> ratings,
            List<String> prices,
            String sort,
            int page,
            int size);
}
