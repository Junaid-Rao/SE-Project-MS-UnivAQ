package smartparking.model;

import java.math.BigDecimal;

/**
 * Domain model: ChargingMode (Iteration 2 - Charge Vehicle).
 * Defines charging type (e.g. fast, normal), price per unit (kWh), and charging speed.
 */
public class ChargingMode {
    private String modeId;
    private String modeType;       // e.g. "fast", "normal"
    private BigDecimal pricePerUnit;
    private double chargingSpeed;  // e.g. kW

    public ChargingMode() {
        this.pricePerUnit = BigDecimal.ZERO;
    }

    public ChargingMode(String modeId, String modeType, BigDecimal pricePerUnit, double chargingSpeed) {
        this();
        this.modeId = modeId;
        this.modeType = modeType;
        this.pricePerUnit = pricePerUnit != null ? pricePerUnit : BigDecimal.ZERO;
        this.chargingSpeed = chargingSpeed;
    }

    public String getModeId() { return modeId; }
    public void setModeId(String modeId) { this.modeId = modeId; }
    public String getModeType() { return modeType; }
    public void setModeType(String modeType) { this.modeType = modeType; }
    public BigDecimal getPricePerUnit() { return pricePerUnit; }
    public void setPricePerUnit(BigDecimal pricePerUnit) { this.pricePerUnit = pricePerUnit != null ? pricePerUnit : BigDecimal.ZERO; }
    public double getChargingSpeed() { return chargingSpeed; }
    public void setChargingSpeed(double chargingSpeed) { this.chargingSpeed = chargingSpeed; }

    @Override
    public String toString() {
        return String.format("ChargingMode{id='%s', type='%s', pricePerUnit=%s, speed=%.1f kW}",
                modeId, modeType, pricePerUnit, chargingSpeed);
    }
}
