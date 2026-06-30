package vn.edu.fpt.dto;

import com.azure.core.annotation.Get;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;


@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CourseCreateDto {

    private Integer id;

    @NotBlank(message = "Vui lòng nhập tiêu đề khóa học.")
    private String title;

    @NotBlank(message = "Vui lòng nhập mô tả khóa học.")
    private String description;

    private MultipartFile thumbnailFile;

    private String level;

    @NotNull(message = "Vui lòng nhập giá khóa học.")
    private BigDecimal price;

    @NotNull(message = "Vui lòng chọn danh mục khóa học.")
    private Integer categoryId;

    private String thumbnailUrl;
}