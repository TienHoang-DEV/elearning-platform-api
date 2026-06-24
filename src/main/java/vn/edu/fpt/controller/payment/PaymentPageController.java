package vn.edu.fpt.controller.payment;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import vn.edu.fpt.entity.Order;
import vn.edu.fpt.entity.OrderItem;
import vn.edu.fpt.entity.Payment;
import vn.edu.fpt.entity.User;
import vn.edu.fpt.repository.PaymentRepository;
import vn.edu.fpt.util.AppConstants;
import vn.edu.fpt.util.SecurityUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Page Controller - Serves HTML pages for payment and checkout flows
 */
@Slf4j
@Controller
@RequiredArgsConstructor
public class PaymentPageController {

    private final PaymentRepository paymentRepository;

    /**
     * GET /payment - Display payment page with QR code
     */
    @GetMapping("/payment")
    public String paymentPage(@RequestParam(value = "id", required = false) Integer paymentId, Model model) {
        log.info("Loading payment page with paymentId: {}", paymentId);

        // Fetch current user
        User currentUser = SecurityUtils.getCurrentUser();
        model.addAttribute("currentUser", currentUser);

        if (paymentId != null) {
            Payment payment = paymentRepository.findById(paymentId).orElse(null);
            if (payment != null) {
                model.addAttribute("payment", payment);
                
                Order order = payment.getOrder();
                model.addAttribute("order", order);

                if (order != null && order.getItems() != null) {
                    // Group order items by course's instructor
                    Map<User, List<OrderItem>> itemsByInstructor = order.getItems().stream()
                            .collect(Collectors.groupingBy(item -> item.getCourse().getInstructor()));
                    model.addAttribute("itemsByInstructor", itemsByInstructor);
                }

                // Pass account number and description from DB first (saved at creation time)
                model.addAttribute("qrCode", (payment.getPaymentUrl() != null) ? AppConstants.QR_CODE_BASE_URL + payment.getPaymentUrl() : null);
                model.addAttribute("payOsAccountNumber", payment.getAccountNumber());
                model.addAttribute("payOsDescription", payment.getDescription());
                model.addAttribute("payOsBankName", payment.getBankName());
                model.addAttribute("payOsAccountHolder", payment.getAccountHolder());
            }
        }

        return "payment/payment";
    }
}
