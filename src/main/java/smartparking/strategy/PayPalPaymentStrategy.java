package smartparking.strategy;

import smartparking.model.PaymentGateway;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * Concrete strategy: PayPal payment. Delegates to PaymentGateway (same gateway, different "method" for reporting).
 * Configured to simulate an unsuccessful payment scenario for demo/exam (sequence diagram alt [payment failed]).
 */
public class PayPalPaymentStrategy implements PaymentStrategy {

    private final Optional<PaymentGateway> gateway;

    public PayPalPaymentStrategy(PaymentGateway gateway) {
        this.gateway = Optional.ofNullable(gateway);
    }

    @Override
    public String getDisplayName() {
        return "PayPal";
    }

    @Override
    public boolean processPayment(BigDecimal amount) {
        // Simulate unsuccessful payment for demo: allows testing "payment failure notification" path (SSD).
        return false;
    }
}
