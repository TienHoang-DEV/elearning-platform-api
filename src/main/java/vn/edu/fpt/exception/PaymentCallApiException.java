package vn.edu.fpt.exception;

public class PaymentCallApiException extends ApplicationException {
    private static final long serialVersionUID = 1L;

    public PaymentCallApiException() {
        super();
    }

    public PaymentCallApiException(String message) {
        super(message);
    }

    public PaymentCallApiException(String message, Throwable cause) {
        super(message, cause);
    }

    public PaymentCallApiException(Throwable cause) {
        super(cause);
    }
}
