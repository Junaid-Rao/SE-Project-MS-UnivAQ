package smartparking.strategy;

import java.math.BigDecimal;

/**
 * Context for Strategy pattern: holds the current payment strategy and delegates
 * processPayment to it. Client (e.g. service layer) sets strategy then calls process.
 */
public class PaymentContext {

    private PaymentStrategy strategy;

    public void setStrategy(PaymentStrategy strategy) {
        this.strategy = strategy;
    }

    public PaymentStrategy getStrategy() {
        return strategy;
    }

    /** Execute payment using the current strategy. */
    public boolean executePayment(BigDecimal amount) {
        if (strategy == null || amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return false;
        }
        return strategy.processPayment(amount);
    }

    public String getPaymentMethodName() {
        return strategy != null ? strategy.getDisplayName() : "Unknown";
    }
}
