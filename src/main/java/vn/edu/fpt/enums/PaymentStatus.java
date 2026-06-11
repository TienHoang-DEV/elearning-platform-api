package vn.edu.fpt.enums;

public enum PaymentStatus {
    /**
     * Payment link created, waiting for payment
     */
    PENDING,

    /**
     * Payment successful (from PayOS webhook)
     */
    PAID,

    /**
     * Payment failed
     */
    FAILED,

    /**
     * Payment link expired
     */
    EXPIRED,

    /**
     * Payment cancelled by user or system
     */
    CANCELLED
}
