package vn.edu.fpt.controller.payment;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.fpt.entity.Payment;
import vn.edu.fpt.service.payment.PaymentService;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    /**
     * Khởi tạo thanh toán cho các sản phẩm đã chọn từ giỏ hàng.
     * POST /api/payments/checkout
     * 
     * @return ResponseEntity chứa thông tin giao dịch thanh toán vừa khởi tạo (ID, link thanh toán, mã QR,...)
     */
    @PostMapping("/checkout")
    public ResponseEntity<?> checkout() {
        log.info("Nhận yêu cầu checkout từ giỏ hàng.");
        Payment payment = paymentService.checkout();

        // Xây dựng DTO phản hồi trả về Client
        Map<String, Object> response = new HashMap<>();
        response.put("id", payment.getId());
        response.put("orderId", payment.getOrder().getId());
        response.put("amount", payment.getAmount());
        response.put("gatewayOrderCode", payment.getGatewayOrderCode());
        response.put("status", payment.getStatus());
        response.put("paymentUrl", payment.getPaymentUrl());
        response.put("qrCodeUrl", payment.getQrCodeUrl());
        response.put("expiredAt", payment.getExpiredAt());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Lấy thông tin trạng thái mới nhất của giao dịch thanh toán.
     * GET /api/payments/{paymentId}/status
     * 
     * @param paymentId ID giao dịch cần kiểm tra.
     * @return ResponseEntity chứa trạng thái thanh toán mới nhất (PENDING, PAID, CANCELLED,...)
     */
    @GetMapping("/{paymentId}/status")
    public ResponseEntity<?> getPaymentStatus(@PathVariable Integer paymentId) {
        log.info("Nhận yêu cầu kiểm tra trạng thái cho Payment ID: {}", paymentId);
        Payment payment = paymentService.getPaymentStatus(paymentId);

        // Xây dựng DTO phản hồi trạng thái thanh toán
        Map<String, Object> response = new HashMap<>();
        response.put("id", payment.getId());
        response.put("status", payment.getStatus());
        response.put("amount", payment.getAmount());
        response.put("orderId", payment.getOrder().getId());
        response.put("paidAt", payment.getPaidAt());
        response.put("expiredAt", payment.getExpiredAt());

        return ResponseEntity.ok(response);
    }

    /**
     * Yêu cầu hủy giao dịch thanh toán thủ công từ phía người dùng.
     * POST /api/payments/{paymentId}/cancel
     * 
     * @param paymentId ID giao dịch cần hủy.
     * @return ResponseEntity thông báo kết quả hủy thành công hay thất bại.
     */
    @PostMapping("/{paymentId}/cancel")
    public ResponseEntity<?> cancelPayment(@PathVariable Integer paymentId) {
        log.info("Nhận yêu cầu hủy thanh toán thủ công cho Payment ID: {}", paymentId);
        paymentService.cancelPaymentManually(paymentId);
        
        return ResponseEntity.ok(Map.of("success", true, "message", "Hủy giao dịch thành công."));
    }
}
