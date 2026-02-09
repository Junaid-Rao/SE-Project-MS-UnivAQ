package smartparking.facade;

import smartparking.command.BookingCommand;
import smartparking.command.CancelReservationCommand;
import smartparking.command.CommandResult;
import smartparking.command.MakeReservationCommand;
import smartparking.model.ParkingLot;
import smartparking.model.ParkingSlot;
import smartparking.model.Reservation;
import smartparking.model.User;
import smartparking.persistence.PersistentManager;
import smartparking.service.MakeReservationService;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Facade pattern: provides a unified, simplified interface to the booking subsystem
 * (slots, users, reservations, payment methods, make/cancel commands). Clients (e.g. Application,
 * interactive flow) use only this facade instead of persistence + service + strategy registry.
 */
public class BookingFacade {

    private final PersistentManager persistence;
    private final MakeReservationService makeReservationService;

    public BookingFacade(PersistentManager persistence, MakeReservationService makeReservationService) {
        this.persistence = persistence;
        this.makeReservationService = makeReservationService;
    }

    public List<User> getUsers() {
        return persistence.findAllUsers();
    }

    public List<ParkingLot> getParkingLots() {
        return persistence.findAllParkingLots();
    }

    public List<ParkingSlot> getAvailableSlots() {
        return makeReservationService.getAvailableSlots();
    }

    public List<ParkingSlot> getAvailableSlotsByType(String type) {
        return makeReservationService.getAvailableSlotsByType(type);
    }

    public List<String> getPaymentMethodNames() {
        return makeReservationService.getPaymentMethodNames();
    }

    public List<Reservation> getReservationsByUser(String userId) {
        return persistence.findReservationsByUserId(userId);
    }

    public List<Reservation> getAllReservations() {
        return persistence.findAllReservations();
    }

    /** Create and return a MakeReservation command (Command pattern). Caller invokes command.execute(). */
    public BookingCommand createMakeReservationCommand(String userId, String slotId,
                                                        LocalDateTime startTime, LocalDateTime endTime,
                                                        String paymentMethod) {
        return new MakeReservationCommand(makeReservationService, userId, slotId, startTime, endTime, paymentMethod);
    }

    /** Create and return a CancelReservation command. */
    public BookingCommand createCancelReservationCommand(String reservationId) {
        return new CancelReservationCommand(persistence, reservationId);
    }

    /** Convenience: execute make reservation and return result. */
    public CommandResult makeReservation(String userId, String slotId,
                                         LocalDateTime startTime, LocalDateTime endTime,
                                         String paymentMethod) {
        return createMakeReservationCommand(userId, slotId, startTime, endTime, paymentMethod).execute();
    }

    /** Convenience: execute cancel reservation and return result. */
    public CommandResult cancelReservation(String reservationId) {
        return createCancelReservationCommand(reservationId).execute();
    }
}
