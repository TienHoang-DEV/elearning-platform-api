package vn.edu.fpt.dto.transaction_manager;

import lombok.Getter;
import lombok.Setter;
import vn.edu.fpt.enums.PaymentStatus;

import java.math.BigDecimal;

@Getter
@Setter
public class TransactionListDTO {

    private String transactionCode;
    private String customerName;
    private String customerEmail;
    private BigDecimal amount;
    private String description;
    private PaymentStatus paymentStatus;

    public TransactionListDTO(String transactionCode, String customerName, String customerEmail, BigDecimal amount, String description, PaymentStatus paymentStatus) {
        this.transactionCode = transactionCode;
        this.customerName = customerName;
        this.customerEmail = customerEmail;
        this.amount = amount;
        this.description = description;
        this.paymentStatus = paymentStatus;
    }

    public boolean isPositive() {
        return amount.compareTo(BigDecimal.ZERO) > 0;
    }
}
