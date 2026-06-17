package vn.edu.fpt.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.fpt.dto.CartPageDetailsDto;
import vn.edu.fpt.entity.*;
import vn.edu.fpt.enums.OrderStatus;
import vn.edu.fpt.enums.PaymentStatus;
import vn.edu.fpt.repository.PaymentRepository;
import vn.edu.fpt.util.AppConstants;
import vn.edu.fpt.util.SecurityUtils;
import vn.payos.PayOS;
import vn.payos.model.v2.paymentRequests.PaymentLink;
import vn.payos.model.webhooks.Webhook;
import vn.payos.model.webhooks.WebhookData;

import java.math.BigDecimal;
import java.util.*;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository repository;
    private final CartService cartService;
    private final PayOsService payOsService;
    private final OrderService orderService;
    private final PayOS payOS;

    // ==================== CRUD cơ bản ====================

    public List<Payment> findAll() {
        return repository.findAll();
    }

    public Optional<Payment> findById(Integer id) {
        return repository.findById(id);
    }

    public Payment save(Payment entity) {
        return repository.save(entity);
    }

    public void deleteById(Integer id) {
        repository.deleteById(id);
    }

    public boolean existsById(Integer id) {
        return repository.existsById(id);
    }

    // ==================== Checkout ====================

    /**
     * Khởi tạo thanh toán từ giỏ hàng của người dùng hiện tại.
     * Tạo Order, OrderItems, sau đó gọi PayOS để lấy QR code.
     */
    public ResponseEntity<?> checkout() {
        try {
            // Lấy user hiện tại
            User user = SecurityUtils.getCurrentUser();

            // Lấy giỏ hàng
            Cart cart = cartService.getOrCreateCartForUser(user);
            if (cart.getItems() == null || cart.getItems().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Giỏ hàng trống"));
            }

            // Lọc các sản phẩm được chọn
            List<CartItem> selectedItems = new ArrayList<>();
            for (CartItem item : cart.getItems()) {
                if (item.isSelected()) {
                    selectedItems.add(item);
                }
            }

            if (selectedItems.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Vui lòng chọn ít nhất một khóa học để thanh toán"));
            }

            // Tính tổng tiền
            CartPageDetailsDto cartDetails = cartService.getCartPageDetails(user);
            if (cartDetails.getTotal() <= 0) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Không có khóa học nào được chọn"));
            }

            BigDecimal totalAmount = BigDecimal.valueOf(cartDetails.getTotal());

            // Tạo Order
            Order order = Order.builder()
                    .user(user)
                    .totalAmount(totalAmount)
                    .status(OrderStatus.PENDING)
                    .paymentMethod(AppConstants.PAYMENT_GATEWAY)
                    .build();

            // Tạo OrderItem cho từng khóa học được chọn
            for (CartItem item : selectedItems) {
                Course course = item.getCourse();
                long coursePrice = course.getPrice().longValue();

                OrderItem orderItem = OrderItem.builder()
                        .order(order)
                        .course(course)
                        .priceSnapshot(BigDecimal.valueOf(coursePrice))
                        .courseTitleSnapshot(course.getTitle())
                        .build();

                order.addItem(orderItem);
            }

            order = orderService.save(order);

            // Gọi PayOS để tạo link thanh toán và QR code
            String returnUrl = "https://learninghubswp391.eastasia.cloudapp.azure.com/payment/success";
            String cancelUrl = "https://learninghubswp391.eastasia.cloudapp.azure.com/payment/cancel";

            Payment payment = payOsService.createPaymentOrder(order, returnUrl, cancelUrl);

            // Trả về thông tin thanh toán
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

    // ==================== Kiểm tra trạng thái thanh toán ====================

    /**
     * Kiểm tra trạng thái của một payment.
     * Nếu đang PENDING, hỏi thẳng PayOS để cập nhật trạng thái mới nhất.
     */
    public ResponseEntity<?> getPaymentStatus(Integer paymentId) {
        try {
            // Tìm payment trong database
            Payment payment = repository.findById(paymentId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy thông tin thanh toán"));

            // Nếu đang chờ thanh toán, hỏi PayOS để cập nhật
            if (payment.getStatus() == PaymentStatus.PENDING) {
                syncStatusFromPayOs(payment);
            }

            // Build response
            Map<String, Object> response = new HashMap<>();
            response.put("id", payment.getId());
            response.put("status", payment.getStatus());
            response.put("amount", payment.getAmount());
            response.put("orderId", payment.getOrder().getId());
            response.put("paidAt", payment.getPaidAt());
            response.put("expiredAt", payment.getExpiredAt());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error checking payment status for id={}", paymentId, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Gọi PayOS để lấy trạng thái mới nhất và cập nhật vào database.
     * Chỉ dùng khi payment đang ở trạng thái PENDING.
     */
    private void syncStatusFromPayOs(Payment payment) {
        try {
            long orderCode = Long.parseLong(payment.getGatewayOrderCode());
            PaymentLink info = payOS.paymentRequests().get(orderCode);

            log.info("Direct PayOS status check for orderCode {}: {}", orderCode, info.getStatus());

            String payOsStatus = (info.getStatus() != null) ? info.getStatus().toString() : "";

            if ("PAID".equalsIgnoreCase(payOsStatus)) {
                payOsService.completePayment(payment);
            } else if ("CANCELLED".equalsIgnoreCase(payOsStatus)) {
                payOsService.cancelPayment(payment);
            } else if ("EXPIRED".equalsIgnoreCase(payOsStatus)) {
                payOsService.expirePayment(payment);
            }

        } catch (Exception e) {
            log.warn("Could not query PayOS status for payment id={}, error: {}", payment.getId(), e.getMessage());
            // Không ném lỗi — trả về trạng thái hiện tại trong DB
        }
    }

    // ==================== Hủy thanh toán ====================

    /**
     * Người dùng chủ động hủy giao dịch.
     * Kiểm tra quyền sở hữu trước khi cho phép hủy.
     */
    public ResponseEntity<?> cancelPaymentManually(Integer paymentId) {
        try {
            // Tìm payment
            Payment payment = repository.findById(paymentId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy thông tin thanh toán."));

            // Kiểm tra người dùng hiện tại có phải chủ giao dịch không
            User currentUser = SecurityUtils.getCurrentUser();
            User orderOwner = payment.getOrder().getUser();

            if (currentUser == null || !orderOwner.getId().equals(currentUser.getId())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Không có quyền thực hiện hành động này."));
            }

            // Hủy ở cả local lẫn PayOS
            payOsService.cancelPaymentAndInvalidatePayOs(payment);

            return ResponseEntity.ok(Map.of("success", true, "message", "Hủy giao dịch thành công."));

        } catch (Exception e) {
            log.error("Error cancelling payment id={}", paymentId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Lỗi khi hủy giao dịch: " + e.getMessage()));
        }
    }

    // ==================== Webhook PayOS ====================

    /**
     * Xử lý webhook gửi về từ PayOS.
     * Xác minh chữ ký rồi đẩy sang PayOsService để cập nhật trạng thái.
     */
    public ResponseEntity<?> handleWebhook(Webhook webhook) {
        try {
            log.info("Received PayOS webhook with signature: {}", webhook.getSignature());

            // Xác minh webhook bằng PayOS SDK
            WebhookData verifiedData = payOS.webhooks().verify(webhook);

            // Xử lý dữ liệu đã xác minh
            payOsService.processWebhookCallback(verifiedData);

            return ResponseEntity.ok(Map.of("success", true));

        } catch (Exception e) {
            log.error("Webhook processing error", e);
            return ResponseEntity.ok(Map.of("success", false, "error", e.getMessage()));
        }
    }
}
