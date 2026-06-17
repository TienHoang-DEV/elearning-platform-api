package vn.edu.fpt.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.fpt.service.PaymentService;
import vn.payos.model.webhooks.Webhook;

@Slf4j
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    /**
     * Khởi tạo thanh toán từ giỏ hàng
     * POST /api/payments/checkout
     */
    @PostMapping("/checkout")
    public ResponseEntity<?> checkout() {
        return paymentService.checkout();
    }

    /**
     * Kiểm tra trạng thái thanh toán
     * GET /api/payments/{paymentId}/status
     */
    @GetMapping("/{paymentId}/status")
    public ResponseEntity<?> getPaymentStatus(@PathVariable Integer paymentId) {
        return paymentService.getPaymentStatus(paymentId);
    }

    /**
     * Người dùng chủ động hủy giao dịch
     * POST /api/payments/{paymentId}/cancel
     */
    @PostMapping("/{paymentId}/cancel")
    public ResponseEntity<?> cancelPayment(@PathVariable Integer paymentId) {
        return paymentService.cancelPaymentManually(paymentId);
    }

    /**
     * Nhận webhook callback từ PayOS
     * POST /api/payments/webhook
     */
    @PostMapping("/webhook")
    public ResponseEntity<?> handleWebhook(@RequestBody Webhook webhook) {
        return paymentService.handleWebhook(webhook);
    }
}
