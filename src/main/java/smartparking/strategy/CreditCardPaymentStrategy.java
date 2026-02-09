package smartparking.strategy;

import smartparking.model.PaymentGateway;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * Concrete strategy: Credit Card payment. Delegates to PaymentGateway.
 */
public class CreditCardPaymentStrategy implements PaymentStrategy {

    private final Optional<PaymentGateway> gateway;

    public CreditCardPaymentStrategy(PaymentGateway gateway) {
        this.gateway = Optional.ofNullable(gateway);
    }

    @Override
    public String getDisplayName() {
        return "Credit Card";
    }

    @Override
    public boolean processPayment(BigDecimal amount) {
        return gateway.map(g -> g.processTransaction(amount.doubleValue())).orElse(false);
    }
}
