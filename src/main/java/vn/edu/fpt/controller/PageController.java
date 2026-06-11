package vn.edu.fpt.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Page Controller - Serves HTML pages for payment and checkout flows
 */
@Slf4j
@Controller
public class PageController {

    /**
     * GET /payment - Display payment page with QR code
     */
    @GetMapping("/payment")
    public String paymentPage() {
        log.info("Loading payment page");
        return "payment/payment";
    }

    /**
     * GET /payment/success - Payment success callback
     */
    @GetMapping("/payment/success")
    public String paymentSuccess() {
        log.info("Payment success");
        return "payment/success";
    }

    /**
     * GET /payment/cancel - Payment cancel callback
     */
    @GetMapping("/payment/cancel")
    public String paymentCancel() {
        log.info("Payment cancelled");
        return "payment/cancel";
    }
}
