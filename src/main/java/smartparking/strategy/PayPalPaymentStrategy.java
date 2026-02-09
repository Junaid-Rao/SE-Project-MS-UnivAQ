package smartparking.strategy;

import smartparking.model.PaymentGateway;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * Concrete strategy: PayPal payment. Delegates to PaymentGateway (same gateway, different "method" for reporting).
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
        return gateway.map(g -> g.processTransaction(amount.doubleValue())).orElse(false);
    }
}
