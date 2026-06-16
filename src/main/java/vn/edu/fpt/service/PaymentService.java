package vn.edu.fpt.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.fpt.dto.CartPageDetailsDto;
import vn.edu.fpt.entity.*;
import vn.edu.fpt.enums.DiscountType;
import vn.edu.fpt.enums.OrderStatus;
import vn.edu.fpt.repository.PaymentRepository;
import vn.edu.fpt.util.AppConstants;
import vn.edu.fpt.util.SecurityUtils;

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

    public List<Payment> findAll() { return repository.findAll(); }
    public Optional<Payment> findById(Integer id) { return repository.findById(id); }
    public Payment save(Payment entity) { return repository.save(entity); }
    public void deleteById(Integer id) { repository.deleteById(id); }
    public boolean existsById(Integer id) { return repository.existsById(id); }

    public ResponseEntity<?> checkout() {
        try {
            // Get current user using SecurityUtils (handles both local and OAuth2 login)
            User user = SecurityUtils.getCurrentUser();

            // Get cart
            Cart cart = cartService.getOrCreateCartForUser(user);
            if (cart.getItems() == null || cart.getItems().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Giỏ hàng trống"));
            }

            List<CartItem> selectedItems = new ArrayList<>();
            for (CartItem item : cart.getItems()) {
                if (item.isSelected()) {
                    selectedItems.add(item);
                }
            }

            if (selectedItems.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Vui lòng chọn ít nhất một khóa học để thanh toán"));
            }

            // Get cart details with total calculation
            CartPageDetailsDto cartDetails = cartService.getCartPageDetails(user);
            if (cartDetails.getTotal() <= 0) {
                return ResponseEntity.badRequest().body(Map.of("error", "Không có khóa học nào được chọn"));
            }

            // Create order with final total amount (after discounts)
            BigDecimal totalAmount = BigDecimal.valueOf(cartDetails.getTotal());


            Order order = Order.builder()
                    .user(user)
                    .totalAmount(totalAmount)
                    .status(OrderStatus.PENDING)
                    .paymentMethod(AppConstants.PAYMENT_GATEWAY)
                    .build();

            // Create OrderItems from selected cart items
            for (CartItem item : selectedItems) {
                Course course = item.getCourse();

                // Find if there is an applied coupon for this instructor

                // Calculate prices
                long coursePrice = course.getPrice().longValue();
                long courseDiscount = Math.round(coursePrice * AppConstants.DEFAULT_DISCOUNT);
                long instItemDiscount = 0;

                long itemTotalDiscount = courseDiscount + instItemDiscount;
                long finalPrice = coursePrice - itemTotalDiscount;
                if (finalPrice < 0) {
                    finalPrice = 0;
                }

                OrderItem orderItem = OrderItem.builder()
                        .order(order)
                        .course(course)
                        .priceSnapshot(BigDecimal.valueOf(coursePrice))
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

}
