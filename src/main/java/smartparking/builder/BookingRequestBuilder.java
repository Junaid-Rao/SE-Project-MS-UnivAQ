package smartparking.builder;

import java.time.LocalDateTime;

/**
 * Builder pattern: constructs a BookingRequest step by step with validation.
 * Ensures a valid request before build(). Director (e.g. interactive flow) calls setters then build().
 */
public class BookingRequestBuilder {

    private String userId;
    private String slotId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String paymentMethod = "Credit Card";

    public BookingRequestBuilder userId(String userId) {
        this.userId = userId;
        return this;
    }

    public BookingRequestBuilder slotId(String slotId) {
        this.slotId = slotId;
        return this;
    }

    public BookingRequestBuilder startTime(LocalDateTime startTime) {
        this.startTime = startTime;
        return this;
    }

    public BookingRequestBuilder endTime(LocalDateTime endTime) {
        this.endTime = endTime;
        return this;
    }

    public BookingRequestBuilder paymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod != null && !paymentMethod.isBlank() ? paymentMethod : "Credit Card";
        return this;
    }

    /**
     * Build the request. Validates required fields and times.
     * @return built BookingRequest
     * @throws IllegalStateException if required fields missing or end before start
     */
    public BookingRequest build() {
        if (userId == null || userId.isBlank()) {
            throw new IllegalStateException("userId is required");
        }
        if (slotId == null || slotId.isBlank()) {
            throw new IllegalStateException("slotId is required");
        }
        if (startTime == null || endTime == null) {
            throw new IllegalStateException("startTime and endTime are required");
        }
        if (!endTime.isAfter(startTime)) {
            throw new IllegalStateException("endTime must be after startTime");
        }
        return new BookingRequest(userId, slotId, startTime, endTime, paymentMethod);
    }
}
