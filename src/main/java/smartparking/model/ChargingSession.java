package smartparking.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Domain model: ChargingSession (Iteration 2 - Charge Vehicle).
 * Represents an active or completed vehicle charging event. Links Customer (User), ChargingMode, ChargingSlot, Payment.
 */
public class ChargingSession {
    public static final String SESSION_STATUS_ACTIVE = "active";
    public static final String SESSION_STATUS_COMPLETED = "completed";
    public static final String SESSION_STATUS_CANCELLED = "cancelled";

    private String sessionId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private BigDecimal totalAmount;
    private String sessionStatus;
    private BigDecimal pricePerUnit;
    private String userId;      // Customer/Driver
    private String slotId;
    private String modeType;
    private double energyUsedKwh;  // for endSession → energyUsed
    private LocalDateTime scheduledEndTime;  // auto-end and release slot when this time is reached

    public ChargingSession() {
        this.sessionStatus = SESSION_STATUS_ACTIVE;
        this.totalAmount = BigDecimal.ZERO;
        this.pricePerUnit = BigDecimal.ZERO;
    }

    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }
    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount != null ? totalAmount : BigDecimal.ZERO; }
    public String getSessionStatus() { return sessionStatus; }
    public void setSessionStatus(String sessionStatus) { this.sessionStatus = sessionStatus; }
    public BigDecimal getPricePerUnit() { return pricePerUnit; }
    public void setPricePerUnit(BigDecimal pricePerUnit) { this.pricePerUnit = pricePerUnit != null ? pricePerUnit : BigDecimal.ZERO; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getSlotId() { return slotId; }
    public void setSlotId(String slotId) { this.slotId = slotId; }
    public String getModeType() { return modeType; }
    public void setModeType(String modeType) { this.modeType = modeType; }
    public double getEnergyUsedKwh() { return energyUsedKwh; }
    public void setEnergyUsedKwh(double energyUsedKwh) { this.energyUsedKwh = energyUsedKwh; }
    public LocalDateTime getScheduledEndTime() { return scheduledEndTime; }
    public void setScheduledEndTime(LocalDateTime scheduledEndTime) { this.scheduledEndTime = scheduledEndTime; }

    /** Create a new session (sequence: createSession()). Returns this session for "session" return. */
    public static ChargingSession createSession() {
        ChargingSession s = new ChargingSession();
        s.setSessionId("CHG-" + java.util.UUID.randomUUID().toString().substring(0, 8));
        s.setStartTime(LocalDateTime.now());
        s.setSessionStatus(SESSION_STATUS_ACTIVE);
        return s;
    }

    /** End this session (sequence: endSession()). Returns endTime and energyUsed. */
    public EndSessionResult endSession() {
        if (SESSION_STATUS_COMPLETED.equals(sessionStatus) || SESSION_STATUS_CANCELLED.equals(sessionStatus)) {
            return new EndSessionResult(endTime, energyUsedKwh, false);
        }
        this.endTime = LocalDateTime.now();
        this.sessionStatus = SESSION_STATUS_COMPLETED;
        if (energyUsedKwh <= 0 && startTime != null && endTime != null) {
            long minutes = java.time.temporal.ChronoUnit.MINUTES.between(startTime, endTime);
            energyUsedKwh = Math.round(minutes * 0.5 * 100.0) / 100.0; // demo: ~0.5 kWh per minute
        }
        if (totalAmount == null || totalAmount.compareTo(BigDecimal.ZERO) == 0) {
            if (pricePerUnit != null && energyUsedKwh > 0) {
                totalAmount = pricePerUnit.multiply(BigDecimal.valueOf(energyUsedKwh));
            }
        }
        return new EndSessionResult(endTime, energyUsedKwh, true);
    }

    public static final class EndSessionResult {
        private final LocalDateTime endTime;
        private final double energyUsedKwh;
        private final boolean ended;

        public EndSessionResult(LocalDateTime endTime, double energyUsedKwh, boolean ended) {
            this.endTime = endTime;
            this.energyUsedKwh = energyUsedKwh;
            this.ended = ended;
        }
        public LocalDateTime getEndTime() { return endTime; }
        public double getEnergyUsedKwh() { return energyUsedKwh; }
        public boolean isEnded() { return ended; }
    }

    @Override
    public String toString() {
        return String.format("ChargingSession{sessionId='%s', userId='%s', slotId='%s', status='%s', start=%s, end=%s, energy=%.2f kWh}",
                sessionId, userId, slotId, sessionStatus, startTime, endTime, energyUsedKwh);
    }
}
