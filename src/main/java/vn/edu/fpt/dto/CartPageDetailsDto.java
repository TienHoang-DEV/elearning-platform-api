package vn.edu.fpt.dto;

import lombok.*;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartPageDetailsDto {
    private CartDto cart;
    private Map<UserDto, List<CartItemDto>> itemsByInstructor;
    private int cartSize;
    
    private long subtotal;
    private long courseDiscounts;
    private long instructorDiscounts;
    private long total;
    private long selectedItemsCount;
    
    private Map<Integer, String> appliedVoucherCodes;
    private Map<Integer, Long> appliedVoucherDiscounts;
    private Map<Integer, Boolean> voucherSuccess;
    private Map<Integer, String> instructorCheckboxState;
    private String globalCheckboxState;
}
