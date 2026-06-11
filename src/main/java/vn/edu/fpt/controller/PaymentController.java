package vn.edu.fpt.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/payments")
public class PaymentController {

    @GetMapping
    public String getPayment() {
        return "payment/payment";
    }
}
