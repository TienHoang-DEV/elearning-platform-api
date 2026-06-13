package vn.edu.fpt.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.fpt.dto.CartPageDetailsDto;
import vn.edu.fpt.entity.*;
import vn.edu.fpt.enums.OrderStatus;
import vn.edu.fpt.enums.PaymentStatus;
import vn.edu.fpt.repository.PaymentRepository;
import vn.edu.fpt.service.CartService;
import vn.edu.fpt.service.OrderService;
import vn.edu.fpt.service.PayOsService;
import vn.edu.fpt.service.PaymentService;
import vn.edu.fpt.util.SecurityUtils;
import vn.payos.PayOS;
import vn.payos.model.webhooks.Webhook;
import vn.payos.model.webhooks.WebhookData;
import vn.payos.model.v2.paymentRequests.PaymentLink;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PayOsService payOsService;
    private final PaymentRepository paymentRepository;
    private final PayOS payOS;
    private final PaymentService paymentService;
    private final CartService cartService;
    private final OrderService orderService;

    /**
     * Initiate payment checkout from cart
     * POST /api/payments/checkout
     */
    @PostMapping("/checkout")
    public ResponseEntity<?> checkout() {
        return paymentService.checkout();
    }

    /**
     * Check payment status
     * GET /api/payments/{paymentId}/status
     */
    @GetMapping("/{paymentId}/status")
    public ResponseEntity<?> getPaymentStatus(@PathVariable Integer paymentId) {
        try {
            Payment payment = paymentRepository.findById(paymentId)
                    .orElseThrow(() -> new RuntimeException("Payment not found"));

            // If pending, check direct with PayOS to see if status has changed
            if (payment.getStatus() == PaymentStatus.PENDING) {
                try {
                    long orderCode = Long.parseLong(payment.getGatewayOrderCode());
                    PaymentLink info = payOS.paymentRequests().get(orderCode);
                    log.info("Direct PayOS status check for orderCode {}: {}", orderCode, info.getStatus());
                    
                    String payOsStatus = info.getStatus() != null ? info.getStatus().toString() : "";
                    if ("PAID".equalsIgnoreCase(payOsStatus)) {
                        payOsService.completePayment(payment);
                    } else if ("CANCELLED".equalsIgnoreCase(payOsStatus)) {
                        payOsService.cancelPayment(payment);
                    } else if ("EXPIRED".equalsIgnoreCase(payOsStatus)) {
                        payOsService.expirePayment(payment);
                    }
                } catch (Exception e) {
                    log.warn("Could not query PayOS status directly for payment id: {}, error: {}", paymentId, e.getMessage());
                }
            }

            Map<String, Object> response = new HashMap<>();
            response.put("id", payment.getId());
            response.put("status", payment.getStatus());
            response.put("amount", payment.getAmount());
            response.put("orderId", payment.getOrder().getId());
            response.put("paidAt", payment.getPaidAt());
            response.put("expiredAt", payment.getExpiredAt());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error checking payment status", e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Cancel payment manually by user request
     * POST /api/payments/{paymentId}/cancel
     */
    @PostMapping("/{paymentId}/cancel")
    public ResponseEntity<?> cancelPaymentManually(@PathVariable Integer paymentId) {
        try {
            Payment payment = paymentRepository.findById(paymentId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy thông tin thanh toán."));

            User user = SecurityUtils.getCurrentUser();
            if (user == null || !payment.getOrder().getUser().getId().equals(user.getId())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Không có quyền thực hiện hành động này."));
            }

            payOsService.cancelPaymentAndInvalidatePayOs(payment);
            return ResponseEntity.ok(Map.of("success", true, "message", "Hủy giao dịch thành công."));
        } catch (Exception e) {
            log.error("Error cancelling payment manually", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Lỗi khi hủy giao dịch: " + e.getMessage()));
        }
    }

    /**
     * Webhook endpoint for PayOS callbacks
     * POST /api/payments/webhook
     */
    @PostMapping("/webhook")
    public ResponseEntity<?> handleWebhook(@RequestBody Webhook webhook) {
        try {
            log.info("Received PayOS webhook with signature: {}", webhook.getSignature());

            // Verify webhook data using PayOS SDK
            WebhookData verifiedData = payOS.webhooks().verify(webhook);
            
            // Process verified webhook data
            payOsService.processWebhookCallback(verifiedData);

            // Return success response
            return ResponseEntity.ok(Map.of("success", true));

        } catch (Exception e) {
            log.error("Webhook processing error", e);
            return ResponseEntity.ok(Map.of("success", false, "error", e.getMessage()));
        }
    }
}
