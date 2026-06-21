package vn.edu.fpt.service.payment;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.fpt.dto.CartPageDetailsDto;
import vn.edu.fpt.dto.transaction_manager.TransactionCountByStatusDTO;
import vn.edu.fpt.dto.transaction_manager.TransactionListDTO;
import vn.edu.fpt.entity.*;
import vn.edu.fpt.enums.OrderStatus;
import vn.edu.fpt.enums.PaymentStatus;
import vn.edu.fpt.exception.BadRequestException;
import vn.edu.fpt.exception.ResourceNotFoundException;
import vn.edu.fpt.repository.PaymentRepository;
import vn.edu.fpt.service.CartService;
import vn.edu.fpt.service.OrderService;
import vn.edu.fpt.util.AppConstants;
import vn.edu.fpt.util.SecurityUtils;
import vn.payos.PayOS;
import vn.payos.model.v2.paymentRequests.PaymentLink;

import java.math.BigDecimal;
import java.time.LocalDate;
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

    // ==================== Nghiệp vụ Thanh toán / Checkout ====================

    /**
     * Khởi tạo thanh toán dựa trên các mặt hàng được chọn trong giỏ hàng của người dùng hiện tại.
     * Tạo hóa đơn (Order), các chi tiết hóa đơn (OrderItem), sau đó gọi PayOS để lấy thông tin thanh toán (QR code, URL).
     * 
     * @return Đối tượng Payment chứa chi tiết thanh toán vừa khởi tạo.
     * @throws BadRequestException Nếu giỏ hàng trống hoặc không chọn khóa học nào.
     */
    public Payment checkout() {
        // Lấy thông tin người dùng đang đăng nhập
        User user = SecurityUtils.getCurrentUser();

        // Lấy hoặc tạo mới giỏ hàng của người dùng
        Cart cart = cartService.getOrCreateCartForUser(user);
        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            throw new BadRequestException("Giỏ hàng trống");
        }

        // Lọc ra các sản phẩm đã được người dùng tích chọn để thanh toán
        List<CartItem> selectedItems = new ArrayList<>();
        for (CartItem item : cart.getItems()) {
            if (item.isSelected()) {
                selectedItems.add(item);
            }
        }

        if (selectedItems.isEmpty()) {
            throw new BadRequestException("Vui lòng chọn ít nhất một khóa học để thanh toán");
        }

        // Kiểm tra tổng số tiền của các khóa học được chọn
        CartPageDetailsDto cartDetails = cartService.getCartPageDetails(user);
        if (cartDetails.getTotal() <= 0) {
            throw new BadRequestException("Không có khóa học nào được chọn");
        }

        BigDecimal totalAmount = BigDecimal.valueOf(cartDetails.getTotal());

        // Khởi tạo đối tượng đơn hàng (Order) ở trạng thái PENDING
        Order order = Order.builder()
                .user(user)
                .totalAmount(totalAmount)
                .status(OrderStatus.PENDING)
                .paymentMethod(AppConstants.PAYMENT_GATEWAY)
                .build();

        // Lưu thông tin chi tiết các khóa học được mua (OrderItem) vào đơn hàng
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

        // Lưu đơn hàng vào cơ sở dữ liệu
        order = orderService.save(order);

        // Định nghĩa đường dẫn phản hồi khi người dùng thao tác trên giao diện cổng thanh toán
        String returnUrl = "https://learninghubswp391.eastasia.cloudapp.azure.com/payment/success";
        String cancelUrl = "https://learninghubswp391.eastasia.cloudapp.azure.com/payment/cancel";

        // Gọi PayOsService để kết nối cổng và sinh thông tin QR code/link thanh toán
        Payment payment = payOsService.createPaymentOrder(order, returnUrl, cancelUrl);

        log.info("Khởi tạo thanh toán thành công cho Payment ID: {}", payment.getId());
        return payment;
    }

    // ==================== Trạng thái thanh toán ====================

    /**
     * Truy vấn thông tin giao dịch thanh toán.
     * Nếu giao dịch đang ở trạng thái PENDING (chờ thanh toán), hệ thống sẽ chủ động truy vấn trạng thái
     * mới nhất từ PayOS để đồng bộ dữ liệu.
     * 
     * @param paymentId ID của bản ghi Payment cần kiểm tra.
     * @return Đối tượng Payment sau khi đã được cập nhật trạng thái mới nhất.
     * @throws ResourceNotFoundException Nếu không tìm thấy thông tin giao dịch thanh toán trong hệ thống.
     */
    public Payment getPaymentStatus(Integer paymentId) {
        // Tìm kiếm giao dịch trong cơ sở dữ liệu
        Payment payment = repository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thông tin thanh toán"));

        // Nếu trạng thái cục bộ đang là PENDING, gọi PayOS kiểm tra và cập nhật trạng thái mới nhất
        if (payment.getStatus() == PaymentStatus.PENDING) {
            syncStatusFromPayOs(payment);
        }

        return payment;
    }

    /**
     * Đồng bộ trạng thái giao dịch hiện tại từ cổng thanh toán PayOS.
     * 
     * @param payment Bản ghi thanh toán cần đồng bộ.
     */
    private void syncStatusFromPayOs(Payment payment) {
        try {
            long orderCode = Long.parseLong(payment.getGatewayOrderCode());
            PaymentLink info = payOS.paymentRequests().get(orderCode);

            log.info("Kiểm tra trực tiếp trạng thái trên PayOS cho mã đơn hàng {}: {}", orderCode, info.getStatus());

            String payOsStatus = (info.getStatus() != null) ? info.getStatus().toString() : "";

            if ("PAID".equalsIgnoreCase(payOsStatus)) {
                payOsService.completePayment(payment);
            } else if ("CANCELLED".equalsIgnoreCase(payOsStatus)) {
                payOsService.cancelPayment(payment);
            } else if ("EXPIRED".equalsIgnoreCase(payOsStatus)) {
                payOsService.expirePayment(payment);
            }

        } catch (Exception e) {
            log.warn("Không thể truy vấn trạng thái từ PayOS cho Payment ID: {}, Lỗi: {}", payment.getId(), e.getMessage());
        }
    }

    // ==================== Hủy thanh toán thủ công ====================

    /**
     * Người dùng chủ động hủy giao dịch thanh toán từ màn hình giao diện.
     * Kiểm tra quyền sở hữu đơn hàng của người dùng hiện tại trước khi hủy giao dịch trên PayOS và cục bộ.
     * 
     * @param paymentId ID của bản ghi Payment cần hủy.
     * @throws ResourceNotFoundException Nếu không tìm thấy giao dịch.
     * @throws BadRequestException Nếu người dùng hiện tại không phải chủ sở hữu của giao dịch này.
     */
    public void cancelPaymentManually(Integer paymentId) {
        // Tìm kiếm giao dịch thanh toán
        Payment payment = repository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thông tin thanh toán."));

        // Kiểm tra quyền sở hữu giao dịch
        User currentUser = SecurityUtils.getCurrentUser();
        User orderOwner = payment.getOrder().getUser();

        if (currentUser == null || !orderOwner.getId().equals(currentUser.getId())) {
            throw new BadRequestException("Không có quyền thực hiện hành động này.");
        }

        // Hủy liên kết thanh toán phía cổng PayOS và cập nhật trạng thái cục bộ thành CANCELLED
        payOsService.cancelPaymentAndInvalidatePayOs(payment);
    }

    public Integer getNumberAllPayment() {
        return repository.getNumberAllPayment();
    }

    public Integer getNumberAllPaymentWithStatus(PaymentStatus paymentStatus) {
        return repository.getNumberAllPaymentWithStatus(paymentStatus);
    }

    public TransactionCountByStatusDTO gettransactionCountByStatusDTO() {
        List<Object[]> result = repository.gettransactionCountByStatusDTO();
        TransactionCountByStatusDTO dto = new TransactionCountByStatusDTO(0,0,0,0,0);
        for (Object[] o : result) {
            PaymentStatus paymentStatus = (PaymentStatus) o[0];
            Long count = (Long) o[1];
            if (paymentStatus == PaymentStatus.PENDING) {
                dto.setNumberOfTransactionPending(count.intValue());
            } else if (paymentStatus == PaymentStatus.PAID) {
                dto.setNumberOfTransactionSuccess(count.intValue());
            } else if (paymentStatus == PaymentStatus.CANCELLED) {
                dto.setNumberOfTransactionCanceled(count.intValue());
            }  else if (paymentStatus == PaymentStatus.EXPIRED) {
                dto.setNumberOfTransactionExpired(count.intValue());
            } else if (paymentStatus == PaymentStatus.FAILED) {
                dto.setNumberOfTransactionFailed(count.intValue());
            }
        }
        return dto;
    }

    public Page<TransactionListDTO> getTransactionByFilter(PaymentStatus status, LocalDate fromDate, LocalDate toDate, String keyword, int page) {
        Pageable pageable = PageRequest.of(page, AppConstants.NUMBER_PAYMENT_RECORD_PER_PAGE);
        return repository.getTransactionByFilter(status, fromDate, toDate, keyword, pageable);
    }
}
