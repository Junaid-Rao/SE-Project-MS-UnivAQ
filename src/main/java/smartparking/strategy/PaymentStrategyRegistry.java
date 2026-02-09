package smartparking.strategy;

import java.util.Optional;

/**
 * Registry of payment strategies by display name. Supports Open/Closed:
 * new payment methods registered without changing client code.
 */
public interface PaymentStrategyRegistry {

    Optional<PaymentStrategy> getStrategy(String displayName);

    /** All registered method names for UI (e.g. menu options). */
    java.util.List<String> getAvailableMethodNames();
}
