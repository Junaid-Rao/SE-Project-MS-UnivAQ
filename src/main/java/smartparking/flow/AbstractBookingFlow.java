package smartparking.flow;

import smartparking.builder.BookingRequest;
import smartparking.command.BookingCommand;
import smartparking.command.CommandResult;
import smartparking.model.ParkingSlot;
import smartparking.model.User;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Template Method pattern: defines the skeleton of the booking flow (select slot -> booking -> payment).
 * Subclasses implement the primitive operations (e.g. display slots, read user input). The template
 * method runFlow() orchestrates the steps and is invariant; only the primitives vary (console vs GUI).
 */
public abstract class AbstractBookingFlow {

    /**
     * Template method: run the full flow. Steps are fixed; primitive ops are abstract.
     * @return result of the flow (success/failure and reservation/payment if success)
     */
    public final FlowResult runFlow() {
        // 1. Select user (login / choose user)
        User user = selectUser();
        if (user == null) {
            return FlowResult.cancelled("No user selected.");
        }

        // 2. Display available slots and let user select one
        List<ParkingSlot> available = getAvailableSlots();
        if (available.isEmpty()) {
            return FlowResult.failure("No available slots.");
        }
        ParkingSlot selected = displayAndSelectSlot(available);
        if (selected == null) {
            return FlowResult.cancelled("No slot selected.");
        }

        // 3. Get booking times (start, end)
        LocalDateTime start = getStartTime();
        LocalDateTime end = getEndTime(start);
        if (start == null || end == null || !end.isAfter(start)) {
            return FlowResult.failure("Invalid booking times.");
        }

        // 4. Confirm booking (show summary, ask yes/no)
        if (!confirmBooking(selected, start, end)) {
            return FlowResult.cancelled("Booking cancelled by user.");
        }

        // 5. Select payment method
        String paymentMethod = selectPaymentMethod();
        if (paymentMethod == null || paymentMethod.isBlank()) {
            return FlowResult.cancelled("No payment method selected.");
        }

        // 6. Execute command (MakeReservationCommand)
        BookingCommand command = createMakeReservationCommand(user.getUserId(), selected.getSlotId(), start, end, paymentMethod);
        CommandResult result = command.execute();

        if (result.isSuccess()) {
            return FlowResult.success(result.getMessage(), result.getReservation(), result.getPayment());
        }
        return FlowResult.failure(result.getMessage());
    }

    /** Primitive: return the list of available slots (from facade). */
    protected abstract List<ParkingSlot> getAvailableSlots();

    /** Primitive: let user select a slot; return null if cancelled. */
    protected abstract ParkingSlot displayAndSelectSlot(List<ParkingSlot> slots);

    /** Primitive: get start time from user. */
    protected abstract LocalDateTime getStartTime();

    /** Primitive: get end time (e.g. start + duration). */
    protected abstract LocalDateTime getEndTime(LocalDateTime start);

    /** Primitive: show summary and ask for confirmation. Return true to proceed. */
    protected abstract boolean confirmBooking(ParkingSlot slot, LocalDateTime start, LocalDateTime end);

    /** Primitive: let user select payment method; return display name or null. */
    protected abstract String selectPaymentMethod();

    /** Primitive: select/logged-in user; null if cancelled. */
    protected abstract User selectUser();

    /** Factory: create the MakeReservation command (delegates to facade/service). */
    protected abstract BookingCommand createMakeReservationCommand(String userId, String slotId,
                                                                     LocalDateTime start, LocalDateTime end,
                                                                     String paymentMethod);

    /** Result of the flow (success, failure, or cancelled). */
    public static final class FlowResult {
        private final boolean success;
        private final boolean cancelled;
        private final String message;
        private final smartparking.model.Reservation reservation;
        private final smartparking.model.Payment payment;

        private FlowResult(boolean success, boolean cancelled, String message,
                          smartparking.model.Reservation reservation, smartparking.model.Payment payment) {
            this.success = success;
            this.cancelled = cancelled;
            this.message = message;
            this.reservation = reservation;
            this.payment = payment;
        }

        public static FlowResult success(String message, smartparking.model.Reservation r, smartparking.model.Payment p) {
            return new FlowResult(true, false, message, r, p);
        }
        public static FlowResult failure(String message) {
            return new FlowResult(false, false, message, null, null);
        }
        public static FlowResult cancelled(String message) {
            return new FlowResult(false, true, message, null, null);
        }

        public boolean isSuccess() { return success; }
        public boolean isCancelled() { return cancelled; }
        public String getMessage() { return message; }
        public smartparking.model.Reservation getReservation() { return reservation; }
        public smartparking.model.Payment getPayment() { return payment; }
    }
}
