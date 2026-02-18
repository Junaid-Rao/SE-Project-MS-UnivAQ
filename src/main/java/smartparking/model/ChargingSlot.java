package smartparking.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Domain model: ChargingSlot (Iteration 2 - Charge Vehicle).
 * An individual charging point within a ChargingStation. Status: available, occupied, reserved, faulty.
 */
public class ChargingSlot {
    public static final String STATUS_AVAILABLE = "available";
    public static final String STATUS_OCCUPIED = "occupied";
    public static final String STATUS_RESERVED = "reserved";
    public static final String STATUS_FAULTY = "faulty";

    private String slotId;
    private String slotNumber;
    private String status;
    private String stationId;
    private List<ChargingMode> supportedModes;

    public ChargingSlot() {
        this.status = STATUS_AVAILABLE;
        this.supportedModes = new ArrayList<>();
    }

    public ChargingSlot(String slotId, String slotNumber, String stationId) {
        this();
        this.slotId = slotId;
        this.slotNumber = slotNumber;
        this.stationId = stationId;
    }

    public String getSlotId() { return slotId; }
    public void setSlotId(String slotId) { this.slotId = slotId; }
    public String getSlotNumber() { return slotNumber; }
    public void setSlotNumber(String slotNumber) { this.slotNumber = slotNumber; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getStationId() { return stationId; }
    public void setStationId(String stationId) { this.stationId = stationId; }
    public List<ChargingMode> getSupportedModes() { return supportedModes; }
    public void setSupportedModes(List<ChargingMode> supportedModes) { this.supportedModes = supportedModes != null ? supportedModes : new ArrayList<>(); }

    /** Check if this slot is available at the given location (location match is done by station). */
    public boolean isAvailable() {
        return STATUS_AVAILABLE.equalsIgnoreCase(status);
    }

    /** Get charging details and price for a mode (used by sequence: getChargingDetails(mode)). */
    public ChargingDetails getChargingDetails(String modeType) {
        for (ChargingMode m : supportedModes) {
            if (m.getModeType() != null && m.getModeType().equalsIgnoreCase(modeType)) {
                return new ChargingDetails(m.getModeType(), m.getPricePerUnit(), m.getChargingSpeed(), slotNumber, slotId);
            }
        }
        return null;
    }

    public void reserve() { this.status = STATUS_RESERVED; }
    public void occupy() { this.status = STATUS_OCCUPIED; }
    public void release() { this.status = STATUS_AVAILABLE; }

    /** DTO for charging details and price returned to driver. */
    public static class ChargingDetails {
        private final String modeType;
        private final BigDecimal pricePerUnit;
        private final double chargingSpeed;
        private final String slotNumber;
        private final String slotId;

        public ChargingDetails(String modeType, BigDecimal pricePerUnit, double chargingSpeed, String slotNumber, String slotId) {
            this.modeType = modeType;
            this.pricePerUnit = pricePerUnit != null ? pricePerUnit : BigDecimal.ZERO;
            this.chargingSpeed = chargingSpeed;
            this.slotNumber = slotNumber;
            this.slotId = slotId;
        }
        public String getModeType() { return modeType; }
        public BigDecimal getPricePerUnit() { return pricePerUnit; }
        public double getChargingSpeed() { return chargingSpeed; }
        public String getSlotNumber() { return slotNumber; }
        public String getSlotId() { return slotId; }
    }

    @Override
    public String toString() {
        return String.format("ChargingSlot{slotId='%s', slotNumber='%s', status='%s', stationId='%s'}",
                slotId, slotNumber, status, stationId);
    }
}
