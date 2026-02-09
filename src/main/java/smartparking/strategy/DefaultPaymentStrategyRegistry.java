package smartparking.strategy;

import smartparking.model.PaymentGateway;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Default registry: Credit Card and PayPal. Gateway injected (e.g. from PersistentManager).
 */
public class DefaultPaymentStrategyRegistry implements PaymentStrategyRegistry {

    private final Map<String, PaymentStrategy> strategies = new HashMap<>();

    public DefaultPaymentStrategyRegistry(PaymentGateway gateway) {
        register(new CreditCardPaymentStrategy(gateway));
        register(new PayPalPaymentStrategy(gateway));
    }

    public void register(PaymentStrategy strategy) {
        if (strategy != null) {
            strategies.put(strategy.getDisplayName().toLowerCase(), strategy);
        }
    }

    @Override
    public Optional<PaymentStrategy> getStrategy(String displayName) {
        if (displayName == null || displayName.isBlank()) return Optional.empty();
        PaymentStrategy s = strategies.get(displayName.trim().toLowerCase());
        if (s != null) return Optional.of(s);
        // Fallback: first strategy (e.g. Credit Card)
        return strategies.values().stream().findFirst();
    }

    @Override
    public List<String> getAvailableMethodNames() {
        return strategies.values().stream()
                .map(PaymentStrategy::getDisplayName)
                .sorted()
                .toList();
    }
}
