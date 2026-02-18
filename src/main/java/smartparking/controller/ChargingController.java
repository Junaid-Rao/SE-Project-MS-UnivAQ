package smartparking.controller;

import smartparking.model.ChargingSession;
import smartparking.model.ChargingSlot;
import smartparking.model.Payment;
import smartparking.service.ChargingService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Charging Controller (Iteration 2 - Charge Vehicle).
 * Facade over ChargingService implementing the five system operations from the SSD.
 * Maps 1:1 to sequence diagram: startCharging, requestChargingSlot, selectChargingMode, processPayment, stopCharging.
 */
public class ChargingController {

    private final ChargingService chargingService;

    public ChargingController(ChargingService chargingService) {
        this.chargingService = chargingService;
    }

    /** 1. startCharging() — Driver initiates; system creates session and returns sessionStarted. */
    public ChargingService.StartChargingResult startCharging(String userId) {
        return chargingService.startCharging(userId);
    }

    /** 2. requestChargingSlot(location) — Driver requests; system returns available charging slots. */
    public List<ChargingSlot> requestChargingSlot(String location) {
        return chargingService.requestChargingSlot(location);
    }

    /** 3. selectChargingMode(fast, normal) — Driver selects mode; system returns charging details and price. */
    public ChargingSlot.ChargingDetails selectChargingMode(String modeType) {
        return chargingService.selectChargingMode(modeType);
    }

    /** 4. processPayment(paymentMethod) — Driver pays; system authorizes, creates payment record; returns start time, receipt, reward or failure. */
    public ChargingService.ProcessPaymentResult processPayment(String paymentMethod, BigDecimal amount,
                                                                ChargingSession session, String slotId, String modeType, BigDecimal pricePerUnit) {
        if (session == null) {
            return ChargingService.ProcessPaymentResult.failure("No active session.");
        }
        return chargingService.processPayment(paymentMethod, amount, session.getSessionId(), slotId, modeType, pricePerUnit);
    }

    /** 5. stopCharging() — Driver stops; system ends session and returns end time, energy used. */
    public ChargingService.StopChargingResult stopCharging(String sessionId) {
        return chargingService.stopCharging(sessionId);
    }

    public List<String> getChargingModeTypes() {
        return chargingService.getChargingModeTypes();
    }

    /** Auto-deallocate active charging sessions whose scheduled end time has passed. Returns count released. */
    public int releaseExpiredChargingSessions() {
        return chargingService.releaseExpiredChargingSessions();
    }
}
