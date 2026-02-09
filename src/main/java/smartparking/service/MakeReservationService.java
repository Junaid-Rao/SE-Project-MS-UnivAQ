package smartparking.service;

import smartparking.model.*;
import smartparking.persistence.PersistentManager;
import smartparking.strategy.PaymentContext;
import smartparking.strategy.PaymentStrategy;
import smartparking.strategy.PaymentStrategyRegistry;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Use Case 1: Reserve Parking Slot.
 * Coordinates: User, ParkingSlot (availability, reserve), Reservation (create, calculateCost),
 * Payment via Strategy (PaymentContext + PaymentStrategyRegistry). All persistence via PersistentManager.
 */
public class MakeReservationService {

    private final PersistentManager persistence;
    private final PaymentStrategyRegistry paymentRegistry;

    public MakeReservationService(PersistentManager persistence, PaymentStrategyRegistry paymentRegistry) {
        this.persistence = persistence;
        this.paymentRegistry = paymentRegistry;
    }

    /**
     * Main success scenario: make a reservation for a slot and pay.
     *
     * @param userId   user making the reservation
     * @param slotId   slot to reserve
     * @param startTime start of reservation
     * @param endTime   end of reservation
     * @param paymentMethod e.g. "Credit Card", "PayPal"
     * @return result with reservation and payment info, or error message
     */
    public MakeReservationResult makeReservation(String userId, String slotId,
                                                  LocalDateTime startTime, LocalDateTime endTime,
                                                  String paymentMethod) {
        // 1. Find user
        Optional<User> userOpt = persistence.findUserById(userId);
        if (userOpt.isEmpty()) {
            return MakeReservationResult.failure("User not found: " + userId);
        }
        User user = userOpt.get();

        // 2. Find slot (search in all lots)
        ParkingSlot slot = null;
        ParkingLot owningLot = null;
        for (ParkingLot lot : persistence.findAllParkingLots()) {
            slot = lot.getSlotById(slotId);
            if (slot != null) {
                owningLot = lot;
                break;
            }
        }
        if (slot == null || owningLot == null) {
            return MakeReservationResult.failure("Slot not found: " + slotId);
        }

        // 3. Check availability
        if (!slot.checkAvailability()) {
            return MakeReservationResult.failure("Slot is not available: " + slotId);
        }

        // 4. Create reservation
        Reservation reservation = new Reservation();
        reservation.setReservationId("RES-" + UUID.randomUUID().toString().substring(0, 8));
        reservation.setUserId(userId);
        reservation.setSlotId(slotId);
        reservation.setStartTime(startTime);
        reservation.setEndTime(endTime);
        if (!reservation.createReservation()) {
            return MakeReservationResult.failure("Invalid reservation times");
        }

        // 5. Calculate cost
        reservation.calculateCost(slot);
        BigDecimal totalCost = reservation.getTotalCost();

        // 6. Create and process payment (Strategy pattern: select strategy by payment method)
        Payment payment = new Payment();
        payment.setPaymentId("PAY-" + UUID.randomUUID().toString().substring(0, 8));
        payment.setReservationId(reservation.getReservationId());
        payment.setAmount(totalCost);
        String methodName = paymentMethod != null && !paymentMethod.isBlank() ? paymentMethod : "Credit Card";
        payment.setPaymentMethod(methodName);

        Optional<PaymentStrategy> strategyOpt = paymentRegistry.getStrategy(methodName);
        if (strategyOpt.isEmpty()) {
            return MakeReservationResult.failure("Payment method not supported: " + methodName);
        }
        PaymentContext paymentContext = new PaymentContext();
        paymentContext.setStrategy(strategyOpt.get());
        if (!paymentContext.executePayment(totalCost)) {
            payment.setPaymentStatus(Payment.STATUS_FAILED);
            return MakeReservationResult.failure("Payment failed");
        }
        payment.setPaymentStatus(Payment.STATUS_SUCCESS);
        payment.setPaymentTime(java.time.LocalDateTime.now());

        // 7. Reserve slot and confirm reservation
        slot.reserve();
        reservation.setReservationStatus(Reservation.STATUS_CONFIRMED);
        reservation.setPayment(payment);

        // 8. Persist via PersistentManager
        persistence.saveReservation(reservation);
        persistence.savePayment(payment);
        persistence.saveParkingLot(owningLot);

        return MakeReservationResult.success(reservation, payment);
    }

    /** List available slots across all lots (for reporting / UI). */
    public List<ParkingSlot> getAvailableSlots() {
        return persistence.findAllParkingLots().stream()
                .flatMap(lot -> lot.getAvailableSlots().stream())
                .toList();
    }

    /** List available slots by type. */
    public List<ParkingSlot> getAvailableSlotsByType(String type) {
        return persistence.findAllParkingLots().stream()
                .flatMap(lot -> lot.findSlotByType(type).stream())
                .filter(ParkingSlot::isAvailable)
                .toList();
    }

    /** Payment method names for UI (Strategy registry). */
    public List<String> getPaymentMethodNames() {
        return paymentRegistry.getAvailableMethodNames();
    }

    public static final class MakeReservationResult {
        private final boolean success;
        private final String message;
        private final Reservation reservation;
        private final Payment payment;

        private MakeReservationResult(boolean success, String message, Reservation reservation, Payment payment) {
            this.success = success;
            this.message = message;
            this.reservation = reservation;
            this.payment = payment;
        }

        public static MakeReservationResult success(Reservation reservation, Payment payment) {
            return new MakeReservationResult(true, "Reservation confirmed.", reservation, payment);
        }

        public static MakeReservationResult failure(String message) {
            return new MakeReservationResult(false, message, null, null);
        }

        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public Reservation getReservation() { return reservation; }
        public Payment getPayment() { return payment; }
    }
}
