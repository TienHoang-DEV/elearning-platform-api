package vn.edu.fpt.dto;

import lombok.*;
import java.math.BigDecimal;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartDto {
    private Integer id;
    private UserDto user;
    private Set<CartItemDto> items;
}
