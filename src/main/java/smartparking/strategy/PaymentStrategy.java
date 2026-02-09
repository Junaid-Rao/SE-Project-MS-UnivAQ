package smartparking.strategy;

import java.math.BigDecimal;

/**
 * Strategy pattern: defines a family of payment algorithms (Credit Card, PayPal, etc.).
 * Encapsulates each one and makes them interchangeable. Open/Closed: new payment methods
 * added by new strategy classes without changing client code.
 */
public interface PaymentStrategy {

    /** Display name for UI (e.g. "Credit Card", "PayPal"). */
    String getDisplayName();

    /**
     * Process payment for the given amount.
     * @return true if payment succeeded, false otherwise
     */
    boolean processPayment(BigDecimal amount);
}
