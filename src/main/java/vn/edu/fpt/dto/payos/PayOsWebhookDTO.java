package vn.edu.fpt.dto.payos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.time.LocalDateTime;

/**
 * DTO for PayOS Webhook Callback
 * Sent by PayOS when payment status changes
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PayOsWebhookDTO {

    @JsonProperty("code")
    private String code;

    @JsonProperty("message")
    private String message;

    @JsonProperty("data")
    private PayOsWebhookData data;

    @JsonProperty("signature")
    private String signature;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PayOsWebhookData {

        @JsonProperty("id")
        private String id;

        @JsonProperty("orderCode")
        private String orderCode;

        @JsonProperty("amount")
        private Integer amount;

        /**
         * Payment status: PENDING, PAID, FAILED, EXPIRED, CANCELLED
         */
        @JsonProperty("status")
        private String status;

        @JsonProperty("createdAt")
        private String createdAt;

        @JsonProperty("canceledAt")
        private String canceledAt;

        /**
         * Paid timestamp
         */
        @JsonProperty("paidAt")
        private String paidAt;

        @JsonProperty("referenceCode")
        private String referenceCode;

        @JsonProperty("transactionDateTime")
        private String transactionDateTime;
    }
}
