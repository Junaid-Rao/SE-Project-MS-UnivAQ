package smartparking.builder;

import java.time.LocalDateTime;

/**
 * Immutable DTO for a booking request. Built by BookingRequestBuilder (Builder pattern).
 */
public class BookingRequest {

    private final String userId;
    private final String slotId;
    private final LocalDateTime startTime;
    private final LocalDateTime endTime;
    private final String paymentMethod;

    public BookingRequest(String userId, String slotId, LocalDateTime startTime, LocalDateTime endTime, String paymentMethod) {
        this.userId = userId;
        this.slotId = slotId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.paymentMethod = paymentMethod;
    }

    public String getUserId() { return userId; }
    public String getSlotId() { return slotId; }
    public LocalDateTime getStartTime() { return startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public String getPaymentMethod() { return paymentMethod; }
}
