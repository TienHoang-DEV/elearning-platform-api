package vn.edu.fpt.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.fpt.entity.*;
import vn.edu.fpt.enums.OrderStatus;
import vn.edu.fpt.enums.PaymentStatus;
import vn.edu.fpt.repository.*;
import vn.payos.PayOS;
import vn.payos.exception.PayOSException;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkRequest;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkResponse;
import vn.payos.model.webhooks.WebhookData;

import vn.edu.fpt.util.AppConstants;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class PayOsService {

    private final PayOS payOS;
    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final EnrollmentRepository enrollmentRepository;

    @Value("${payos.dev-mode:false}")
    private boolean devMode;

    /**
     * Create payment order with PayOS using official library
     */
    public Payment createPaymentOrder(Order order, String returnUrl, String cancelUrl) {
        try {
            log.info("Creating {} payment order for Order ID: {}",AppConstants.PAYMENT_GATEWAY, order.getId());

            long amountVND = order.getTotalAmount().longValue();
            long orderCode = System.currentTimeMillis() / 1000;
            long expirationTimeUnix = (System.currentTimeMillis() / 1000) + (AppConstants.PAYMENT_EXPIRATION_MINUTES * 60L);

            // Create payment request
            // Description must be the order code for VietQR content
            CreatePaymentLinkRequest request = CreatePaymentLinkRequest.builder()
                    .orderCode(orderCode)
                    .amount(amountVND)
                    .description(String.valueOf(orderCode))
                    .returnUrl(returnUrl)
                    .cancelUrl(cancelUrl)
                    .buyerName(order.getUser().getFirstName() + " " + order.getUser().getLastName())
                    .buyerEmail(order.getUser().getEmail())
                    .buyerPhone(order.getUser().getPhone() != null ? order.getUser().getPhone() : "")
                    .expiredAt(expirationTimeUnix)
                    .build();

            // Call PayOS API or use dev mode
            CreatePaymentLinkResponse response;
            if (devMode) {
                log.warn("DEV MODE ENABLED - Using mock {} response", AppConstants.PAYMENT_GATEWAY);
                response = createMockResponse(orderCode, amountVND);
            } else {
                try {
                    response = payOS.paymentRequests().create(request);
                } catch (PayOSException e) {
                    log.error("{} API error: {}",AppConstants.PAYMENT_GATEWAY, e.getMessage());
                    throw new RuntimeException(AppConstants.PAYMENT_GATEWAY + " API call failed: " + e.getMessage());
                }
            }

            log.info("Payment order created successfully: {}", response.toString());
            log.info("Account name: {}", response.getAccountName());
            
            // Log detailed response fields for debugging
            log.info("QrCode from {}: {}",AppConstants.PAYMENT_GATEWAY, response.getQrCode());
            log.info("CheckoutUrl from {}: {}",AppConstants.PAYMENT_GATEWAY,  response.getCheckoutUrl());
            log.info("AccountNumber from {}: {}",AppConstants.PAYMENT_GATEWAY,  response.getAccountNumber());
            log.info("Description from {}: {}",AppConstants.PAYMENT_GATEWAY,  response.getDescription());

            // Create Payment record from response
            String qrCodeUrl = response.getQrCode();
            String checkoutUrl = response.getCheckoutUrl();
            String accountNumber = response.getAccountNumber();
            
            // PayOS SDK doesn't provide bank name and account holder - use empty/default
            String bankName = AppConstants.BANK_NAMES.get(response.getBin());  // Will be set to default in fallback section
            String accountHolder = response.getAccountName();  // Will be set to default in fallback section
            
            // Use gateway order code as description
            String description = String.valueOf(orderCode);
            
            // Provide fallback values if response fields are null
            if (qrCodeUrl == null || qrCodeUrl.isEmpty()) {
                log.warn("QR Code URL is null or empty from " + AppConstants.PAYMENT_GATEWAY);
            }
            if (checkoutUrl == null || checkoutUrl.isEmpty()) {
                log.warn("Checkout URL is null or empty from " + AppConstants.PAYMENT_GATEWAY);
            }
            if (bankName == null || bankName.isEmpty()) {
                log.warn("Bank name is null or empty from " + AppConstants.PAYMENT_GATEWAY);
            }
            if (accountHolder == null || accountHolder.isEmpty()) {
                log.warn("Account holder is null or empty from " + AppConstants.PAYMENT_GATEWAY);
            }
            if (accountNumber == null || accountNumber.isEmpty()) {
                log.warn("Account number is null or empty from " + AppConstants.PAYMENT_GATEWAY);
            }

            Payment payment = Payment.builder()
                    .order(order)
                    .gateway(AppConstants.PAYMENT_GATEWAY)
                    .amount(BigDecimal.valueOf(amountVND))
                    .status(PaymentStatus.PENDING)
                    .gatewayOrderCode(String.valueOf(orderCode))
                    .paymentUrl(checkoutUrl)
                    .qrCodeUrl(qrCodeUrl)
                    .accountNumber(accountNumber)
                    .description(description)
                    .bankName(bankName)
                    .accountHolder(accountHolder)
                    .expiredAt(LocalDateTime.now().plusMinutes(AppConstants.PAYMENT_EXPIRATION_MINUTES))
                    .webhookReceived(false)
                    .build();

            return paymentRepository.save(payment);

        } catch (Exception e) {
            log.error("Error creating PayOS payment order", e);
            throw new RuntimeException("Lỗi khi tạo đơn hàng: " + e.getMessage());
        }
    }

    /**
     * Mock PayOS response for development testing
     */
    private CreatePaymentLinkResponse createMockResponse(long orderCode, long amount) {
        CreatePaymentLinkResponse response = new CreatePaymentLinkResponse();
        response.setCheckoutUrl("https://pay.payos.vn/web/" + UUID.randomUUID());
        response.setQrCode("https://api.payos.vn/mock/qr/" + UUID.randomUUID());
        response.setOrderCode(orderCode);
        response.setAmount(amount);
        response.setAccountNumber("1234567890123");
        response.setDescription("EDULEARN" + orderCode);

        log.info("✅ Mock PayOS Response Generated");
        return response;
    }

    /**
     * Complete payment: update statuses, enroll user, and clear cart
     */
    public void completePayment(Payment payment) {
        if (payment.getStatus() == PaymentStatus.PAID) {
            return;
        }

        log.info("Completing payment ID: {}, Gateway Order Code: {}", payment.getId(), payment.getGatewayOrderCode());
        payment.setStatus(PaymentStatus.PAID);
        payment.setPaidAt(LocalDateTime.now());
        paymentRepository.save(payment);

        Order order = payment.getOrder();
        if (order != null) {
            order.setStatus(OrderStatus.COMPLETED);
            orderRepository.save(order);

            User user = order.getUser();
            if (user != null) {
                // Enroll user and clear items from cart
                Cart cart = cartRepository.findByUser(user).orElse(null);

                for (OrderItem item : order.getItems()) {
                    Course course = item.getCourse();
                    if (course != null) {
                        // Create enrollment if not exists
                        boolean alreadyEnrolled = enrollmentRepository.existsByUserAndCourse(user, course);
                        if (!alreadyEnrolled) {
                            Enrollment enrollment = Enrollment.builder()
                                    .user(user)
                                    .course(course)
                                    .progressPercent(BigDecimal.ZERO)
                                    .build();
                            enrollmentRepository.save(enrollment);
                            log.info("Enrolled user {} to course {}", user.getEmail(), course.getTitle());
                        }

                        // Remove from cart
                        if (cart != null && cart.getItems() != null) {
                            cart.getItems().removeIf(ci -> ci.getCourse().getId().equals(course.getId()));
                        }
                    }
                }

                if (cart != null) {
                    // Also clean up coupons that are no longer valid
                    java.util.Set<User> remainingInstructors = cart.getItems().stream()
                            .map(item -> item.getCourse().getInstructor())
                            .collect(java.util.stream.Collectors.toSet());

                    cartRepository.save(cart);
                }
            }
        }
    }

    /**
     * Cancel payment and invalidate at PayOS
     */
    public void cancelPaymentAndInvalidatePayOs(Payment payment) {
        if (payment.getStatus() == PaymentStatus.CANCELLED) {
            return;
        }
        log.info("Cancelling payment ID: {} and invalidating at PayOS", payment.getId());
        try {
            long orderCode = Long.parseLong(payment.getGatewayOrderCode());
            try {
                payOS.paymentRequests().cancel(orderCode, "Người dùng yêu cầu hủy giao dịch");
                log.info(" Successfully cancelled payment link on PayOS for orderCode: {}", orderCode);
            } catch (Exception e) {
                log.warn(" PayOS cancel failed (may already be cancelled or expired): {}", e.getMessage());
                // Continue anyway - mark as cancelled locally
            }
        } catch (Exception e) {
            log.error(" Error parsing gateway order code {}: {}", payment.getGatewayOrderCode(), e.getMessage());
        }
        // Always cancel locally regardless of PayOS API result
        cancelPayment(payment);
    }

    /**
     * Cancel payment
     */
    public void cancelPayment(Payment payment) {
        if (payment.getStatus() == PaymentStatus.CANCELLED) {
            return;
        }
        log.info("Cancelling payment ID: {}", payment.getId());
        payment.setStatus(PaymentStatus.CANCELLED);
        paymentRepository.save(payment);

        Order order = payment.getOrder();
        if (order != null) {
            order.setStatus(OrderStatus.CANCELLED);
            orderRepository.save(order);
        }
    }

    /**
     * Expire payment
     */
    public void expirePayment(Payment payment) {
        if (payment.getStatus() == PaymentStatus.EXPIRED) {
            return;
        }
        log.info("Expiring payment ID: {}", payment.getId());
        payment.setStatus(PaymentStatus.EXPIRED);
        paymentRepository.save(payment);

        Order order = payment.getOrder();
        if (order != null) {
            order.setStatus(OrderStatus.EXPIRED);
            orderRepository.save(order);
        }
    }

    /**
     * Process PayOS webhook callback
     */
    public void processWebhookCallback(WebhookData webhookData) {
        try {
            log.info("Processing PayOS webhook for order code: {}", webhookData.getOrderCode());

            String gatewayOrderCode = String.valueOf(webhookData.getOrderCode());
            Payment payment = paymentRepository.findByGatewayOrderCode(gatewayOrderCode)
                    .orElseThrow(() -> new RuntimeException("Payment not found for code: " + gatewayOrderCode));

            payment.setWebhookReceived(true);
            payment.setWebhookReceivedAt(LocalDateTime.now());
            paymentRepository.save(payment);

            // PayOS sends webhook with description containing status or data.status
            // Let's print webhook details for debugging
            log.info("Webhook Data Status: {}", webhookData.toString());

            // By default, webhook trigger implies payment success
            completePayment(payment);

            log.info("Webhook processed successfully");

        } catch (Exception e) {
            log.error("Error processing webhook", e);
            throw new RuntimeException("Webhook processing failed: " + e.getMessage());
        }
    }
}
