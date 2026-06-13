package vn.edu.fpt.dto.payos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.math.BigDecimal;

/**
 * DTO for PayOS Create Order API Request
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PayOsCreateOrderRequest {

    /**
     * Mã đơn hàng duy nhất (ví dụ: ORDER_123456)
     * Phải unique để tránh duplicate
     */
    @JsonProperty("orderCode")
    private String orderCode;

    /**
     * Số tiền cần thanh toán (VND)
     */
    @JsonProperty("amount")
    private Integer amount;

    /**
     * Mô tả đơn hàng
     */
    @JsonProperty("description")
    private String description;

    /**
     * Tên người mua
     */
    @JsonProperty("buyerName")
    private String buyerName;

    /**
     * Email người mua (dùng cho hóa đơn)
     */
    @JsonProperty("buyerEmail")
    private String buyerEmail;

    /**
     * Số điện thoại người mua
     */
    @JsonProperty("buyerPhone")
    private String buyerPhone;

    /**
     * URL để redirect sau khi thanh toán thành công
     */
    @JsonProperty("returnUrl")
    private String returnUrl;

    /**
     * URL để redirect khi người dùng hủy thanh toán
     */
    @JsonProperty("cancelUrl")
    private String cancelUrl;
}
