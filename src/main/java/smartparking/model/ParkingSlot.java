package smartparking.model;

import java.math.BigDecimal;

/**
 * Domain model: ParkingSlot (contained by ParkingLot).
 * Represents a single bookable slot with type and hourly rate.
 */
public class ParkingSlot {
    private String slotId;
    private String slotNumber;
    private String slotType;   // e.g. Standard, EV, Handicap
    private BigDecimal pricePerHour;
    private boolean available;

    public ParkingSlot() {
        this.available = true;
    }

    public ParkingSlot(String slotId, String slotNumber, String slotType, BigDecimal pricePerHour) {
        this();
        this.slotId = slotId;
        this.slotNumber = slotNumber;
        this.slotType = slotType;
        this.pricePerHour = pricePerHour != null ? pricePerHour : BigDecimal.ZERO;
    }

    public String getSlotId() { return slotId; }
    public void setSlotId(String slotId) { this.slotId = slotId; }
    public String getSlotNumber() { return slotNumber; }
    public void setSlotNumber(String slotNumber) { this.slotNumber = slotNumber; }
    public String getSlotType() { return slotType; }
    public void setSlotType(String slotType) { this.slotType = slotType; }
    public BigDecimal getPricePerHour() { return pricePerHour; }
    public void setPricePerHour(BigDecimal pricePerHour) { this.pricePerHour = pricePerHour != null ? pricePerHour : BigDecimal.ZERO; }
    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }

    /** Check if this slot is available for reservation. */
    public boolean checkAvailability() {
        return available;
    }

    /** Mark slot as reserved. */
    public void reserve() {
        this.available = false;
    }

    /** Mark slot as released (available again). */
    public void release() {
        this.available = true;
    }

    /** Calculate price for given number of hours. */
    public double calculatePrice(int hours) {
        if (pricePerHour == null || hours <= 0) return 0.0;
        return pricePerHour.multiply(BigDecimal.valueOf(hours)).doubleValue();
    }

    @Override
    public String toString() {
        return String.format("ParkingSlot{slotId='%s', slotNumber='%s', type='%s', pricePerHour=%s, available=%s}",
                slotId, slotNumber, slotType, pricePerHour, available);
    }
}
