package smartparking.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Domain model: Payment. Associated with a Reservation, processed via PaymentGateway.
 */
public class Payment {
    public static final String STATUS_SUCCESS = "Success";
    public static final String STATUS_FAILED = "Failed";
    public static final String STATUS_REFUNDED = "Refunded";

    private String paymentId;
    private String paymentStatus;   // Success, Failed, Refunded
    private String paymentMethod;  // e.g. Credit Card, PayPal
    private BigDecimal amount;
    private LocalDateTime paymentTime;
    private String reservationId;

    public Payment() {
        this.amount = BigDecimal.ZERO;
    }

    public String getPaymentId() { return paymentId; }
    public void setPaymentId(String paymentId) { this.paymentId = paymentId; }
    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount != null ? amount : BigDecimal.ZERO; }
    public LocalDateTime getPaymentTime() { return paymentTime; }
    public void setPaymentTime(LocalDateTime paymentTime) { this.paymentTime = paymentTime; }
    public String getReservationId() { return reservationId; }
    public void setReservationId(String reservationId) { this.reservationId = reservationId; }

    /**
     * Process payment via gateway. Gateway is injected by service layer.
     * Returns true if gateway reports success.
     */
    public boolean processPayment(PaymentGateway gateway) {
        if (gateway == null || amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            paymentStatus = STATUS_FAILED;
            return false;
        }
        paymentTime = LocalDateTime.now();
        boolean success = gateway.processTransaction(amount.doubleValue());
        paymentStatus = success ? STATUS_SUCCESS : STATUS_FAILED;
        return success;
    }

    /** Refund this payment. */
    public boolean refund() {
        if (STATUS_REFUNDED.equals(paymentStatus)) return false;
        paymentStatus = STATUS_REFUNDED;
        return true;
    }

    /** Generate a simple receipt string for reporting. */
    public String generateReceipt() {
        return String.format("Receipt --- PaymentId: %s | Amount: %s | Method: %s | Time: %s | Status: %s",
                paymentId, amount, paymentMethod, paymentTime, paymentStatus);
    }

    @Override
    public String toString() {
        return String.format("Payment{paymentId='%s', amount=%s, method='%s', status='%s', time=%s}",
                paymentId, amount, paymentMethod, paymentStatus, paymentTime);
    }
}
