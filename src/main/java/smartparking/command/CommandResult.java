package smartparking.command;

import smartparking.model.Payment;
import smartparking.model.Reservation;

/**
 * Result of executing a BookingCommand. Immutable.
 */
public class CommandResult {

    private final boolean success;
    private final String message;
    private final Reservation reservation;
    private final Payment payment;

    public CommandResult(boolean success, String message, Reservation reservation, Payment payment) {
        this.success = success;
        this.message = message;
        this.reservation = reservation;
        this.payment = payment;
    }

    public static CommandResult success(String message, Reservation reservation, Payment payment) {
        return new CommandResult(true, message, reservation, payment);
    }

    public static CommandResult failure(String message) {
        return new CommandResult(false, message, null, null);
    }

    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public Reservation getReservation() { return reservation; }
    public Payment getPayment() { return payment; }
}
