package smartparking.command;

import smartparking.model.Reservation;
import smartparking.persistence.PersistentManager;

import java.util.Optional;

/**
 * Command pattern: encapsulates "cancel reservation" request. Supports undo semantics
 * (cancel is the inverse of make; full undo would require storing prior state).
 */
public class CancelReservationCommand implements BookingCommand {

    private final PersistentManager persistence;
    private final String reservationId;

    public CancelReservationCommand(PersistentManager persistence, String reservationId) {
        this.persistence = persistence;
        this.reservationId = reservationId;
    }

    @Override
    public CommandResult execute() {
        Optional<Reservation> opt = persistence.findReservationById(reservationId);
        if (opt.isEmpty()) {
            return CommandResult.failure("Reservation not found: " + reservationId);
        }
        Reservation r = opt.get();
        if (Reservation.STATUS_CANCELLED.equals(r.getReservationStatus())) {
            return CommandResult.failure("Reservation is already cancelled.");
        }
        r.cancelReservation();
        // Release the slot
        persistence.findAllParkingLots().stream()
                .filter(lot -> lot.getSlotById(r.getSlotId()) != null)
                .findFirst()
                .ifPresent(lot -> {
                    var slot = lot.getSlotById(r.getSlotId());
                    if (slot != null) slot.release();
                    persistence.saveParkingLot(lot);
                });
        persistence.saveReservation(r);
        return CommandResult.success("Reservation cancelled.", r, null);
    }

    @Override
    public String getDescription() {
        return "CancelReservation(id=" + reservationId + ")";
    }
}
