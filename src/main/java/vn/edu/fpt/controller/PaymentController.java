package vn.edu.fpt.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.fpt.dto.payos.PayOsWebhookDTO;
import vn.edu.fpt.entity.Order;
import vn.edu.fpt.entity.Payment;
import vn.edu.fpt.entity.User;
import vn.edu.fpt.enums.OrderStatus;
import vn.edu.fpt.repository.PaymentRepository;
import vn.edu.fpt.service.CartService;
import vn.edu.fpt.service.OrderService;
import vn.edu.fpt.service.PayOsService;
import vn.edu.fpt.util.SecurityUtils;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final CartService cartService;
    private final OrderService orderService;
    private final PayOsService payOsService;
    private final PaymentRepository paymentRepository;

    /**
     * Initiate payment checkout from cart
     * POST /api/payments/checkout
     */
    @PostMapping("/checkout")
    public ResponseEntity<?> checkout() {
        try {
            // Get current user using SecurityUtils (handles both local and OAuth2 login)
            User user = SecurityUtils.getCurrentUser();
            if (user == null) {
                log.error("User not authenticated");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Vui lòng đăng nhập để tiếp tục"));
            }

            log.info("Checkout initiated by user: {}", user.getEmail());

            // Get cart
            var cart = cartService.getOrCreateCartForUser(user);
            if (cart.getItems() == null || cart.getItems().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Giỏ hàng trống"));
            }

            // Get cart details with total calculation
            var cartDetails = cartService.getCartPageDetails(user);
            if (cartDetails.getTotal() <= 0) {
                return ResponseEntity.badRequest().body(Map.of("error", "Không có khóa học nào được chọn"));
            }

            // Create order with final total amount (after discounts)
            BigDecimal totalAmount = BigDecimal.valueOf(cartDetails.getTotal());
            BigDecimal discountAmount = BigDecimal.valueOf(
                    cartDetails.getCourseDiscounts() + cartDetails.getInstructorDiscounts()
            );

            Order order = Order.builder()
                    .user(user)
                    .totalAmount(totalAmount)
                    .discountAmount(discountAmount)
                    .status(OrderStatus.PENDING)
                    .paymentMethod("PAYOS")
                    .build();

            order = orderService.save(order);

            // Call PayOS to generate QR code
            String returnUrl = "http://localhost:8080/payment/success";
            String cancelUrl = "http://localhost:8080/payment/cancel";

            Payment payment = payOsService.createPaymentOrder(order, returnUrl, cancelUrl);

            // Clear selected items from cart
            cartService.checkoutCart(user);

            // Response with payment info
            Map<String, Object> response = new HashMap<>();
            response.put("id", payment.getId());
            response.put("orderId", order.getId());
            response.put("amount", payment.getAmount());
            response.put("gatewayOrderCode", payment.getGatewayOrderCode());
            response.put("status", payment.getStatus());
            response.put("paymentUrl", payment.getPaymentUrl());
            response.put("qrCodeUrl", payment.getQrCodeUrl());
            response.put("expiredAt", payment.getExpiredAt());

            log.info("Payment created successfully: ID={}", payment.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (Exception e) {
            log.error("Checkout error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
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
     * Webhook endpoint for PayOS callbacks
     * POST /api/payments/webhook
     */
    @PostMapping("/webhook")
    public ResponseEntity<?> handleWebhook(@RequestBody PayOsWebhookDTO webhook) {
        try {
            log.info("Received PayOS webhook for order: {}", webhook.getData().getOrderCode());

            // Process webhook (verify signature & update status)
            payOsService.processWebhookCallback(webhook);

            // Return success response
            return ResponseEntity.ok(Map.of("success", true));

        } catch (Exception e) {
            log.error("Webhook processing error", e);
            return ResponseEntity.ok(Map.of("success", false, "error", e.getMessage()));
        }
    }
}
