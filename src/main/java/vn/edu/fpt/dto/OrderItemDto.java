package vn.edu.fpt.dto;

import lombok.*;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItemDto {
    private Integer id;
    private CourseDto course;
    private BigDecimal priceSnapshot;
    private BigDecimal discountAmount;
    private BigDecimal finalPrice;
    private String courseTitleSnapshot;
}
