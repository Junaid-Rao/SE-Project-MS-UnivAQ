package smartparking.service;

import smartparking.model.*;
import smartparking.persistence.PersistentManager;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Use Case 2: Charge Vehicle.
 * Implements system operations: startCharging, requestChargingSlot, selectChargingMode, processPayment, stopCharging.
 * Coordinates ChargingSession, ChargingStation, ChargingSlot, Payment, PaymentGateway per sequence diagrams.
 */
public class ChargingService {

    /** Default charging window: after this time from start, session is auto-ended and slot released. */
    public static final int DEFAULT_CHARGING_DURATION_HOURS = 2;

    /** Rewards policy: base points per successful charge; bonus for loyal customers. */
    public static final int REWARD_BASE_POINTS = 10;
    public static final int REWARD_LOYAL_BONUS_POINTS = 5;
    /** Loyal customer: has at least this many completed charging sessions or confirmed reservations (combined). */
    public static final int LOYAL_CUSTOMER_THRESHOLD = 2;

    private final PersistentManager persistence;
    private final PaymentGateway paymentGateway;

    public ChargingService(PersistentManager persistence, PaymentGateway paymentGateway) {
        this.persistence = persistence;
        this.paymentGateway = paymentGateway;
    }

    /** 1. startCharging() — create session and return session started. */
    public StartChargingResult startCharging(String userId) {
        Optional<User> userOpt = persistence.findUserById(userId);
        if (userOpt.isEmpty()) {
            return StartChargingResult.failure("User not found: " + userId);
        }
        ChargingSession session = ChargingSession.createSession();
        session.setUserId(userId);
        persistence.saveChargingSession(session);
        return StartChargingResult.success(session);
    }

    /** 2. requestChargingSlot(location) — return available charging slots at location. Blank = any. */
    public List<ChargingSlot> requestChargingSlot(String location) {
        String filter = (location != null) ? location.trim() : "";
        List<ChargingSlot> available = new ArrayList<>();
        for (ChargingStation station : persistence.findAllChargingStations()) {
            List<ChargingSlot> slots = station.getAvailableSlots(filter);
            available.addAll(slots);
        }
        return available;
    }

    /** 3. selectChargingMode(fast, normal) — return charging details and price for mode. */
    public ChargingSlot.ChargingDetails selectChargingMode(String modeType) {
        for (ChargingStation station : persistence.findAllChargingStations()) {
            ChargingSlot.ChargingDetails details = station.getSlotDetails(modeType);
            if (details != null) return details;
        }
        return null;
    }

    /** 4. processPayment(paymentMethod, amount, sessionId, slotId, modeType, pricePerUnit) — authorize, create payment record, return receipt/reward. */
    public ProcessPaymentResult processPayment(String paymentMethod, BigDecimal amount, String sessionId,
                                                String slotId, String modeType, BigDecimal pricePerUnit) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return ProcessPaymentResult.failure("Invalid amount");
        }
        // Simulate unsuccessful payment for PayPal (demo: SSD alt [payment failed] path).
        boolean approved = !"PayPal".equalsIgnoreCase(paymentMethod != null ? paymentMethod.trim() : "")
                && paymentGateway.authorizePayment(amount.doubleValue());
        if (!approved) {
            return ProcessPaymentResult.failure("Payment denied by gateway.");
        }
        Payment payment = new Payment();
        payment.setPaymentId("PAY-" + UUID.randomUUID().toString().substring(0, 8));
        payment.setChargingSessionId(sessionId);
        payment.setAmount(amount);
        payment.setPaymentMethod(paymentMethod != null ? paymentMethod : "Credit Card");
        payment.setPaymentStatus(Payment.STATUS_SUCCESS);
        payment.setPaymentTime(LocalDateTime.now());
        persistence.savePayment(payment);

        Optional<ChargingSession> sessionOpt = persistence.findChargingSessionById(sessionId);
        if (sessionOpt.isPresent()) {
            ChargingSession session = sessionOpt.get();
            session.setSlotId(slotId);
            session.setModeType(modeType);
            session.setPricePerUnit(pricePerUnit);
            session.setTotalAmount(amount);
            if (session.getStartTime() != null && session.getScheduledEndTime() == null) {
                session.setScheduledEndTime(session.getStartTime().plusHours(DEFAULT_CHARGING_DURATION_HOURS));
            }
            persistence.saveChargingSession(session);
        }
        ChargingSlot chargingSlot = findChargingSlotById(slotId);
        if (chargingSlot != null) {
            chargingSlot.occupy();
            ChargingStation station = findStationBySlotId(slotId);
            if (station != null) persistence.saveChargingStation(station);
        }

        String receipt = payment.generateReceipt();
        String userId = sessionOpt.map(ChargingSession::getUserId).orElse(null);
        String reward = buildRewardMessage(userId);
        return ProcessPaymentResult.success(payment, receipt, reward);
    }

    /** Build reward message from loyalty policy: base points + bonus for loyal customers. */
    private String buildRewardMessage(String userId) {
        int points = REWARD_BASE_POINTS;
        boolean loyal = isLoyalCustomer(userId);
        if (loyal) {
            points += REWARD_LOYAL_BONUS_POINTS;
            return points + " reward points added (" + REWARD_LOYAL_BONUS_POINTS + " bonus for loyal customer).";
        }
        return points + " reward points added for this charge.";
    }

    /** Loyal customer: has at least LOYAL_CUSTOMER_THRESHOLD completed charging sessions or confirmed reservations. */
    private boolean isLoyalCustomer(String userId) {
        if (userId == null) return false;
        long completedSessions = persistence.findChargingSessionsByUserId(userId).stream()
                .filter(s -> ChargingSession.SESSION_STATUS_COMPLETED.equals(s.getSessionStatus()))
                .count();
        long confirmedReservations = persistence.findReservationsByUserId(userId).stream()
                .filter(r -> Reservation.STATUS_CONFIRMED.equals(r.getReservationStatus()))
                .count();
        return (completedSessions + confirmedReservations) >= LOYAL_CUSTOMER_THRESHOLD;
    }

    /** 5. stopCharging(sessionId) — end session, return end time and energy used. */
    public StopChargingResult stopCharging(String sessionId) {
        Optional<ChargingSession> sessionOpt = persistence.findChargingSessionById(sessionId);
        if (sessionOpt.isEmpty()) {
            return StopChargingResult.failure("Charging session not found: " + sessionId);
        }
        ChargingSession session = sessionOpt.get();
        ChargingSession.EndSessionResult result = session.endSession();
        if (!result.isEnded()) {
            return StopChargingResult.failure("Session already ended or invalid.");
        }
        persistence.saveChargingSession(session);

        ChargingSlot slotToRelease = findChargingSlotById(session.getSlotId());
        if (slotToRelease != null) {
            slotToRelease.release();
            ChargingStation station = findStationBySlotId(session.getSlotId());
            if (station != null) persistence.saveChargingStation(station);
        }

        return StopChargingResult.success(session.getEndTime(), session.getEnergyUsedKwh());
    }

    public List<ChargingStation> getAllChargingStations() {
        return persistence.findAllChargingStations();
    }

    public List<String> getChargingModeTypes() {
        List<String> types = new ArrayList<>();
        for (ChargingMode m : persistence.findAllChargingModes()) {
            if (m.getModeType() != null && !types.contains(m.getModeType())) {
                types.add(m.getModeType());
            }
        }
        return types.isEmpty() ? List.of("fast", "normal") : types;
    }

    private ChargingSlot findChargingSlotById(String slotId) {
        for (ChargingStation s : persistence.findAllChargingStations()) {
            ChargingSlot slot = s.getSlotById(slotId);
            if (slot != null) return slot;
        }
        return null;
    }

    private ChargingStation findStationBySlotId(String slotId) {
        for (ChargingStation s : persistence.findAllChargingStations()) {
            if (s.getSlotById(slotId) != null) return s;
        }
        return null;
    }

    public Optional<ChargingStation> findChargingStationContainingSlot(String slotId) {
        return Optional.ofNullable(findStationBySlotId(slotId));
    }

    /** Auto-deallocate: end active charging sessions whose scheduled end time has passed and release slots. */
    public int releaseExpiredChargingSessions() {
        LocalDateTime now = LocalDateTime.now();
        int released = 0;
        for (ChargingSession session : persistence.findAllChargingSessions()) {
            if (!ChargingSession.SESSION_STATUS_ACTIVE.equals(session.getSessionStatus())) continue;
            LocalDateTime scheduledEnd = session.getScheduledEndTime();
            if (scheduledEnd == null || !scheduledEnd.isBefore(now)) continue;  // not yet expired
            session.endSession();
            session.setSessionStatus(ChargingSession.SESSION_STATUS_COMPLETED);
            persistence.saveChargingSession(session);
            ChargingSlot slotToRelease = findChargingSlotById(session.getSlotId());
            if (slotToRelease != null) {
                slotToRelease.release();
                ChargingStation station = findStationBySlotId(session.getSlotId());
                if (station != null) persistence.saveChargingStation(station);
                released++;
            }
        }
        return released;
    }

    // --- Result DTOs ---
    public static final class StartChargingResult {
        private final boolean success;
        private final String message;
        private final ChargingSession session;

        private StartChargingResult(boolean success, String message, ChargingSession session) {
            this.success = success;
            this.message = message;
            this.session = session;
        }
        public static StartChargingResult success(ChargingSession session) {
            return new StartChargingResult(true, "Session started.", session);
        }
        public static StartChargingResult failure(String message) {
            return new StartChargingResult(false, message, null);
        }
        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public ChargingSession getSession() { return session; }
    }

    public static final class ProcessPaymentResult {
        private final boolean success;
        private final String message;
        private final Payment payment;
        private final String receipt;
        private final String reward;

        private ProcessPaymentResult(boolean success, String message, Payment payment, String receipt, String reward) {
            this.success = success;
            this.message = message;
            this.payment = payment;
            this.receipt = receipt;
            this.reward = reward;
        }
        public static ProcessPaymentResult success(Payment payment, String receipt, String reward) {
            return new ProcessPaymentResult(true, "Payment approved.", payment, receipt, reward);
        }
        public static ProcessPaymentResult failure(String message) {
            return new ProcessPaymentResult(false, message, null, null, null);
        }
        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public Payment getPayment() { return payment; }
        public String getReceipt() { return receipt; }
        public String getReward() { return reward; }
    }

    public static final class StopChargingResult {
        private final boolean success;
        private final String message;
        private final LocalDateTime endTime;
        private final double energyUsedKwh;

        private StopChargingResult(boolean success, String message, LocalDateTime endTime, double energyUsedKwh) {
            this.success = success;
            this.message = message;
            this.endTime = endTime;
            this.energyUsedKwh = energyUsedKwh;
        }
        public static StopChargingResult success(LocalDateTime endTime, double energyUsedKwh) {
            return new StopChargingResult(true, "Charging stopped.", endTime, energyUsedKwh);
        }
        public static StopChargingResult failure(String message) {
            return new StopChargingResult(false, message, null, 0);
        }
        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public LocalDateTime getEndTime() { return endTime; }
        public double getEnergyUsedKwh() { return energyUsedKwh; }
    }
}
