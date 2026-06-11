package vn.edu.fpt.dto.payos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

/**
 * DTO for PayOS Create Order API Response
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PayOsCreateOrderResponse {

    @JsonProperty("code")
    private String code;

    @JsonProperty("message")
    private String message;

    @JsonProperty("data")
    private PayOsOrderData data;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PayOsOrderData {
        /**
         * ID đơn hàng tại PayOS
         */
        @JsonProperty("id")
        private String id;

        /**
         * Mã đơn hàng
         */
        @JsonProperty("orderCode")
        private String orderCode;

        /**
         * Amount in VND
         */
        @JsonProperty("amount")
        private Integer amount;

        /**
         * URL checkout - link thanh toán
         */
        @JsonProperty("checkoutUrl")
        private String checkoutUrl;

        /**
         * QR code URL
         */
        @JsonProperty("qrCode")
        private String qrCode;
    }
}
