package smartparking.model;

/**
 * Domain model: PaymentGateway. Processes transactions (simulated for filing-only persistence).
 */
public class PaymentGateway {
    private String gatewayId;
    private String gatewayName;
    private String provider;   // e.g. Stripe, PayPal
    private String status;     // Active, Inactive

    public String getGatewayId() { return gatewayId; }
    public void setGatewayId(String gatewayId) { this.gatewayId = gatewayId; }
    public String getGatewayName() { return gatewayName; }
    public void setGatewayName(String gatewayName) { this.gatewayName = gatewayName; }
    public String getProvider() { return provider; }
    public void setProvider(String provider) { this.provider = provider; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public boolean connect() {
        if ("Active".equalsIgnoreCase(status)) return true;
        status = "Active";
        return true;
    }

    /** Process a transaction (simulated: always succeeds for demo). */
    public boolean processTransaction(double amount) {
        if (!"Active".equalsIgnoreCase(status)) return false;
        return amount > 0;
    }

    public void disconnect() {
        status = "Inactive";
    }

    @Override
    public String toString() {
        return String.format("PaymentGateway{id='%s', name='%s', provider='%s', status='%s'}",
                gatewayId, gatewayName, provider, status);
    }
}
