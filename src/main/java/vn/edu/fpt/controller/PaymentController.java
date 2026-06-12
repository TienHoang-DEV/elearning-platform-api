package vn.edu.fpt.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.fpt.entity.*;
import vn.edu.fpt.enums.OrderStatus;
import vn.edu.fpt.enums.PaymentStatus;
import vn.edu.fpt.repository.PaymentRepository;
import vn.edu.fpt.service.CartService;
import vn.edu.fpt.service.OrderService;
import vn.edu.fpt.service.PayOsService;
import vn.edu.fpt.util.SecurityUtils;
import vn.payos.PayOS;
import vn.payos.model.webhooks.Webhook;
import vn.payos.model.webhooks.WebhookData;
import vn.payos.model.v2.paymentRequests.PaymentLink;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final CartService cartService;
    private final OrderService orderService;
    private final PayOsService payOsService;
    private final PaymentRepository paymentRepository;
    private final PayOS payOS;

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
            Cart cart = cartService.getOrCreateCartForUser(user);
            if (cart.getItems() == null || cart.getItems().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Giỏ hàng trống"));
            }

            List<CartItem> selectedItems = cart.getItems().stream()
                    .filter(CartItem::isSelected)
                    .collect(Collectors.toList());

            if (selectedItems.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Vui lòng chọn ít nhất một khóa học để thanh toán"));
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

            // Create OrderItems from selected cart items
            for (CartItem item : selectedItems) {
                Course course = item.getCourse();
                
                // Find if there is an applied coupon for this instructor
                CartInstructorCoupon appliedCoupon = cart.getInstructorCoupons().stream()
                        .filter(cic -> cic.getInstructor().getId().equals(course.getInstructor().getId()))
                        .findFirst()
                        .orElse(null);
                
                Coupon coupon = (appliedCoupon != null) ? appliedCoupon.getCoupon() : null;
                
                // Calculate prices
                long coursePrice = course.getPrice().longValue();
                long courseDiscount = Math.round(coursePrice * 0.3);
                long instItemDiscount = 0;
                
                if (coupon != null) {
                    long instSubtotal = cart.getItems().stream()
                            .filter(ci -> ci.isSelected() && ci.getCourse().getInstructor().getId().equals(course.getInstructor().getId()))
                            .mapToLong(ci -> ci.getCourse().getPrice().longValue())
                            .sum();
                    long instCourseDiscounts = Math.round(instSubtotal * 0.3);
                    long instSubtotalAfterDiscount = instSubtotal - instCourseDiscounts;
                    
                    long instDiscountAmount = 0;
                    if ("PERCENT".equalsIgnoreCase(coupon.getDiscountType())) {
                        double rate = coupon.getDiscountValue().doubleValue() / 100.0;
                        instDiscountAmount = Math.round(instSubtotalAfterDiscount * rate);
                    } else if ("FIXED".equalsIgnoreCase(coupon.getDiscountType())) {
                        instDiscountAmount = coupon.getDiscountValue().longValue();
                        if (instDiscountAmount > instSubtotalAfterDiscount) {
                            instDiscountAmount = instSubtotalAfterDiscount;
                        }
                    }
                    
                    long itemSubtotalAfterDiscount = coursePrice - courseDiscount;
                    if (instSubtotalAfterDiscount > 0) {
                        instItemDiscount = Math.round((double) itemSubtotalAfterDiscount / instSubtotalAfterDiscount * instDiscountAmount);
                    }
                }
                
                long itemTotalDiscount = courseDiscount + instItemDiscount;
                long finalPrice = coursePrice - itemTotalDiscount;
                if (finalPrice < 0) {
                    finalPrice = 0;
                }
                
                OrderItem orderItem = OrderItem.builder()
                        .order(order)
                        .course(course)
                        .coupon(coupon)
                        .priceSnapshot(BigDecimal.valueOf(coursePrice))
                        .discountAmount(BigDecimal.valueOf(itemTotalDiscount))
                        .finalPrice(BigDecimal.valueOf(finalPrice))
                        .courseTitleSnapshot(course.getTitle())
                        .build();
                
                order.addItem(orderItem);
            }

            order = orderService.save(order);

            // Call PayOS to generate QR code
            String returnUrl = "https://learninghubswp391.eastasia.cloudapp.azure.com/payment/success";
            String cancelUrl = "https://learninghubswp391.eastasia.cloudapp.azure.com/payment/cancel";

            Payment payment = payOsService.createPaymentOrder(order, returnUrl, cancelUrl);

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
