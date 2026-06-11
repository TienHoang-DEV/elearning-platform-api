package vn.edu.fpt.dto;

import lombok.*;
import vn.edu.fpt.enums.CouponStatus;
import vn.edu.fpt.enums.DiscountType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
public class CouponDto {

    private String title;

    private String code;

    private DiscountType discountType; // Sẽ nhận giá trị 'PERCENT' hoặc 'FIXED' từ <select>

    private BigDecimal discountValue; // Tương ứng DECIMAL(10,2) trong SQL Server

    private Integer usageLimit; // Để Integer (chấp nhận NULL nếu người dùng không nhập)

    // Thuộc tính xử lý ngày hết hạn từ thẻ <input type="date"> gửi lên
    private LocalDate expiredAt;

    private CouponStatus status; // 'ACTIVE' hoặc 'INACTIVE'


}
