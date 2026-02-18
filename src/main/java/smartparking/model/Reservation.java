package smartparking.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * Domain model: Reservation. Links User, ParkingSlot, and optionally Payment.
 */
public class Reservation {
    public static final String STATUS_PENDING = "Pending";
    public static final String STATUS_CONFIRMED = "Confirmed";
    public static final String STATUS_CANCELLED = "Cancelled";
    public static final String STATUS_EXPIRED = "Expired";

    private String reservationId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String reservationStatus;  // Pending, Confirmed, Cancelled
    private BigDecimal totalCost;
    private String userId;
    private String slotId;
    private Payment payment;

    public Reservation() {
        this.reservationStatus = STATUS_PENDING;
        this.totalCost = BigDecimal.ZERO;
    }

    public String getReservationId() { return reservationId; }
    public void setReservationId(String reservationId) { this.reservationId = reservationId; }
    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
    public String getReservationStatus() { return reservationStatus; }
    public void setReservationStatus(String reservationStatus) { this.reservationStatus = reservationStatus; }
    public BigDecimal getTotalCost() { return totalCost; }
    public void setTotalCost(BigDecimal totalCost) { this.totalCost = totalCost != null ? totalCost : BigDecimal.ZERO; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getSlotId() { return slotId; }
    public void setSlotId(String slotId) { this.slotId = slotId; }
    @JsonIgnore
    public Payment getPayment() { return payment; }
    public void setPayment(Payment payment) { this.payment = payment; }

    /** Create reservation (validate and set state). Returns true if valid. */
    public boolean createReservation() {
        if (startTime == null || endTime == null || endTime.isBefore(startTime))
            return false;
        if (slotId == null || userId == null) return false;
        reservationStatus = STATUS_PENDING;
        return true;
    }

    /** Cancel this reservation. */
    public boolean cancelReservation() {
        if (STATUS_CANCELLED.equals(reservationStatus)) return false;
        reservationStatus = STATUS_CANCELLED;
        return true;
    }

    /** Calculate cost based on duration and slot price (slot and hours must be set). */
    public double calculateCost(ParkingSlot slot) {
        if (slot == null || startTime == null || endTime == null) return 0.0;
        long minutes = ChronoUnit.MINUTES.between(startTime, endTime);
        int hours = (int) Math.ceil(minutes / 60.0);
        if (hours <= 0) hours = 1;
        double cost = slot.calculatePrice(hours);
        this.totalCost = BigDecimal.valueOf(cost);
        return cost;
    }

    /** Extend reservation by given minutes. Returns true if extended. */
    public boolean extendTime(int minutes) {
        if (minutes <= 0 || endTime == null) return false;
        if (STATUS_CANCELLED.equals(reservationStatus)) return false;
        endTime = endTime.plusMinutes(minutes);
        return true;
    }

    @Override
    public String toString() {
        return String.format("Reservation{id='%s', slotId='%s', userId='%s', %s to %s, status='%s', totalCost=%s}",
                reservationId, slotId, userId, startTime, endTime, reservationStatus, totalCost);
    }
}
