package vn.edu.fpt.service.payment;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

import vn.edu.fpt.util.AppConstants;

import java.math.BigDecimal;
import java.time.LocalDateTime;

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

    /**
     * Tạo đơn hàng thanh toán trên cổng thanh toán PayOS sử dụng thư viện SDK chính thức.
     * 
     * @param order Đơn hàng cần thanh toán.
     * @param returnUrl URL chuyển hướng khi thanh toán thành công.
     * @param cancelUrl URL chuyển hướng khi người dùng hủy thanh toán.
     * @return Đối tượng Payment chứa thông tin liên kết thanh toán đã được lưu vào CSDL.
     */
    public Payment createPaymentOrder(Order order, String returnUrl, String cancelUrl) {
        try {
            log.info("Đang tạo đơn hàng thanh toán cổng {} cho Order ID: {}", AppConstants.PAYMENT_GATEWAY, order.getId());

            long amountVND = order.getTotalAmount().longValue();
            long orderCode = System.currentTimeMillis() / 1000;
            long expirationTimeUnix = (System.currentTimeMillis() / 1000) + (AppConstants.PAYMENT_EXPIRATION_MINUTES * 60L);

            // Xây dựng request gửi lên PayOS
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

            // Gọi API của PayOS để tạo link thanh toán
            CreatePaymentLinkResponse response;
            try {
                response = payOS.paymentRequests().create(request);
            } catch (PayOSException e) {
                log.error("{} API error: {}", AppConstants.PAYMENT_GATEWAY, e.getMessage());
                throw new RuntimeException(AppConstants.PAYMENT_GATEWAY + " API call failed: " + e.getMessage());
            }

            log.info("Tạo liên kết thanh toán thành công: {}", response.toString());
            log.info("Tên tài khoản thụ hưởng: {}", response.getAccountName());
            log.info("Mã QR: {}", response.getQrCode());
            log.info("Đường dẫn thanh toán: {}", response.getCheckoutUrl());
            log.info("Số tài khoản thụ hưởng: {}", response.getAccountNumber());

            // Lấy thông tin phản hồi từ PayOS
            String qrCodeUrl = response.getQrCode();
            String checkoutUrl = response.getCheckoutUrl();
            String accountNumber = response.getAccountNumber();
            String bankName = AppConstants.BANK_NAMES.get(response.getBin());
            String accountHolder = response.getAccountName();
            String description = String.valueOf(orderCode);

            // Log cảnh báo nếu thiếu thông tin từ cổng thanh toán
            if (qrCodeUrl == null || qrCodeUrl.isEmpty()) {
                log.warn("QR Code URL từ cổng thanh toán rỗng hoặc null.");
            }
            if (checkoutUrl == null || checkoutUrl.isEmpty()) {
                log.warn("Checkout URL từ cổng thanh toán rỗng hoặc null.");
            }

            // Tạo đối tượng Payment để lưu trữ lịch sử giao dịch trong hệ thống
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
                    .build();

            return paymentRepository.save(payment);

        } catch (Exception e) {
            log.error("Lỗi khi tạo đơn hàng PayOS", e);
            throw new RuntimeException("Lỗi khi khởi tạo thanh toán: " + e.getMessage());
        }
    }

    /**
     * Hoàn tất giao dịch thanh toán: cập nhật trạng thái thanh toán, trạng thái đơn hàng,
     * tự động ghi danh (enroll) người dùng vào các khóa học tương ứng và xóa các khóa học đó khỏi giỏ hàng.
     * 
     * @param payment Giao dịch thanh toán cần hoàn tất.
     */
    public void completePayment(Payment payment) {
        if (payment.getStatus() == PaymentStatus.PAID) {
            return;
        }

        log.info("Hoàn tất thanh toán cho Payment ID: {}, Mã đơn hàng cổng: {}", payment.getId(), payment.getGatewayOrderCode());
        payment.setStatus(PaymentStatus.PAID);
        payment.setPaidAt(LocalDateTime.now());
        paymentRepository.save(payment);

        Order order = payment.getOrder();
        if (order != null) {
            order.setStatus(OrderStatus.COMPLETED);
            orderRepository.save(order);

            User user = order.getUser();
            if (user != null) {
                Cart cart = cartRepository.findByUser(user).orElse(null);

                // Ghi danh người dùng vào từng khóa học trong hóa đơn thanh toán thành công
                for (OrderItem item : order.getItems()) {
                    Course course = item.getCourse();
                    if (course != null) {
                        boolean alreadyEnrolled = enrollmentRepository.existsByUserAndCourse(user, course);
                        if (!alreadyEnrolled) {
                            Enrollment enrollment = Enrollment.builder()
                                    .user(user)
                                    .course(course)
                                    .progressPercent(BigDecimal.ZERO)
                                    .build();
                            enrollmentRepository.save(enrollment);
                            log.info("Ghi danh thành công cho người dùng {} vào khóa học {}", user.getEmail(), course.getTitle());
                        }

                        // Loại bỏ khóa học đã thanh toán khỏi giỏ hàng
                        if (cart != null && cart.getItems() != null) {
                            cart.getItems().removeIf(ci -> ci.getCourse().getId().equals(course.getId()));
                        }
                    }
                }

                if (cart != null) {
                    cartRepository.save(cart);
                }
            }
        }
    }

    /**
     * Hủy liên kết thanh toán phía cổng thanh toán PayOS đồng thời hủy giao dịch tương ứng trên hệ thống cục bộ.
     * 
     * @param payment Giao dịch thanh toán cần hủy.
     */
    public void cancelPaymentAndInvalidatePayOs(Payment payment) {
        if (payment.getStatus() == PaymentStatus.CANCELLED) {
            return;
        }
        log.info("Yêu cầu hủy link thanh toán phía cổng PayOS cho Payment ID: {}", payment.getId());
        try {
            long orderCode = Long.parseLong(payment.getGatewayOrderCode());
            try {
                payOS.paymentRequests().cancel(orderCode, "Người dùng yêu cầu hủy giao dịch");
                log.info("Hủy thành công liên kết thanh toán trên PayOS cho mã đơn hàng: {}", orderCode);
            } catch (Exception e) {
                log.warn("Gọi cổng PayOS hủy đơn hàng thất bại (có thể đơn hàng đã quá hạn hoặc đã hủy trước đó): {}", e.getMessage());
            }
        } catch (Exception e) {
            log.error("Lỗi khi phân tích mã đơn hàng {}: {}", payment.getGatewayOrderCode(), e.getMessage());
        }
        // Luôn cập nhật trạng thái hủy cục bộ bất chấp kết quả gọi API hủy của PayOS
        cancelPayment(payment);
    }

    /**
     * Hủy giao dịch thanh toán trên hệ thống cục bộ và cập nhật trạng thái đơn hàng thành CANCELLED.
     * 
     * @param payment Giao dịch thanh toán cần hủy cục bộ.
     */
    public void cancelPayment(Payment payment) {
        if (payment.getStatus() == PaymentStatus.CANCELLED) {
            return;
        }
        log.info("Đang hủy cục bộ giao dịch ID: {}", payment.getId());
        payment.setStatus(PaymentStatus.CANCELLED);
        paymentRepository.save(payment);

        Order order = payment.getOrder();
        if (order != null) {
            order.setStatus(OrderStatus.CANCELLED);
            orderRepository.save(order);
        }
    }

    /**
     * Hết hạn giao dịch thanh toán trên hệ thống cục bộ (hết thời gian thanh toán) và cập nhật đơn hàng thành EXPIRED.
     * 
     * @param payment Giao dịch thanh toán hết hạn.
     */
    public void expirePayment(Payment payment) {
        if (payment.getStatus() == PaymentStatus.EXPIRED) {
            return;
        }
        log.info("Đang hết hạn giao dịch ID: {}", payment.getId());
        payment.setStatus(PaymentStatus.EXPIRED);
        paymentRepository.save(payment);

        Order order = payment.getOrder();
        if (order != null) {
            order.setStatus(OrderStatus.EXPIRED);
            orderRepository.save(order);
        }
    }
}
