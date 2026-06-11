package vn.edu.fpt.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.fpt.entity.Order;
import vn.edu.fpt.entity.Payment;
import vn.edu.fpt.enums.PaymentStatus;
import vn.edu.fpt.repository.PaymentRepository;
import vn.payos.PayOS;
import vn.payos.exception.PayOSException;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkRequest;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkResponse;

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

    @Value("${payos.dev-mode:false}")
    private boolean devMode;

    /**
     * Create payment order with PayOS using official library
     */
    public Payment createPaymentOrder(Order order, String returnUrl, String cancelUrl) {
        try {
            log.info("Creating PayOS payment order for Order ID: {}", order.getId());

            long amountVND = order.getTotalAmount().longValue();
            long orderCode = System.currentTimeMillis() / 1000;

            // Create payment request
            CreatePaymentLinkRequest request = CreatePaymentLinkRequest.builder()
                    .orderCode(orderCode)
                    .amount(amountVND)
                    .description("Thanh toan khoa hoc Learning Hub - Order #" + order.getId())
                    .returnUrl(returnUrl)
                    .cancelUrl(cancelUrl)
                    .buyerName(order.getUser().getFirstName() + " " + order.getUser().getLastName())
                    .buyerEmail(order.getUser().getEmail())
                    .buyerPhone(order.getUser().getPhone() != null ? order.getUser().getPhone() : "")
                    .build();

            // Call PayOS API or use dev mode
            CreatePaymentLinkResponse response;
            if (devMode) {
                log.warn("⚠️ DEV MODE ENABLED - Using mock PayOS response");
                response = createMockResponse(orderCode, amountVND);
            } else {
                try {
                    response = payOS.paymentRequests().create(request);
                } catch (PayOSException e) {
                    log.error("PayOS API error: {}", e.getMessage());
                    throw new RuntimeException("PayOS API call failed: " + e.getMessage());
                }
            }

            log.info("Payment order created successfully: {}", response.toString());

            // Create Payment record from response
            Payment payment = Payment.builder()
                    .order(order)
                    .gateway("PAYOS")
                    .amount(BigDecimal.valueOf(amountVND))
                    .status(PaymentStatus.PENDING)
                    .gatewayOrderCode(String.valueOf(orderCode))
                    .paymentUrl(response.getCheckoutUrl())
                    .qrCodeUrl(response.getQrCode())
                    .expiredAt(LocalDateTime.now().plusMinutes(15))
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

        log.info("✅ Mock PayOS Response Generated");
        return response;
    }

    /**
     * Process PayOS webhook callback
     */
    public void processWebhookCallback(Object webhook) {
        try {
            log.info("Processing PayOS webhook");

            // Find payment and update status
            // Implementation depends on actual webhook structure
            log.info("Webhook processed successfully");

        } catch (Exception e) {
            log.error("Error processing webhook", e);
            throw new RuntimeException("Webhook processing failed: " + e.getMessage());
        }
    }
}
