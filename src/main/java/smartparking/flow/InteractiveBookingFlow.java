package smartparking.flow;

import smartparking.command.BookingCommand;
import smartparking.command.MakeReservationCommand;
import smartparking.facade.BookingFacade;
import smartparking.model.ParkingSlot;
import smartparking.model.User;
import smartparking.ui.ConsoleInput;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * Template Method: concrete flow that uses console I/O. Implements primitive operations
 * (select user, display slots, get times, confirm, select payment) using ConsoleInput and BookingFacade.
 */
public class InteractiveBookingFlow extends AbstractBookingFlow {

    private final BookingFacade facade;
    private final ConsoleInput console;

    public InteractiveBookingFlow(BookingFacade facade, ConsoleInput console) {
        this.facade = facade;
        this.console = console;
    }

    @Override
    protected List<ParkingSlot> getAvailableSlots() {
        return facade.getAvailableSlots();
    }

    @Override
    protected ParkingSlot displayAndSelectSlot(List<ParkingSlot> slots) {
        List<String> options = new ArrayList<>();
        for (ParkingSlot s : slots) {
            options.add(String.format("%s | %s | %s | $%s/hr", s.getSlotId(), s.getSlotNumber(), s.getSlotType(), s.getPricePerHour()));
        }
        int choice = console.selectOption("Available slots (select by number):", options, true);
        if (choice == 0) return null;
        return slots.get(choice - 1);
    }

    @Override
    protected LocalDateTime getStartTime() {
        return console.readDateTime("Start time", LocalDateTime.now());
    }

    @Override
    protected LocalDateTime getEndTime(LocalDateTime start) {
        console.println("Duration: enter hours (e.g. 1 or 2):");
        int hours = console.readIntInRange("Hours: ", 1, 24);
        return start.plusHours(hours);
    }

    @Override
    protected boolean confirmBooking(ParkingSlot slot, LocalDateTime start, LocalDateTime end) {
        long hours = ChronoUnit.HOURS.between(start, end);
        if (hours < 1) hours = 1;
        double cost = slot.calculatePrice((int) hours);
        console.println(String.format("Summary: Slot %s (%s), %s to %s, ~%.0f hour(s), Total: $%.2f",
                slot.getSlotId(), slot.getSlotType(), start, end, (double) hours, cost));
        int confirm = console.readIntInRange("Confirm? (1=Yes, 2=No): ", 1, 2);
        return confirm == 1;
    }

    @Override
    protected String selectPaymentMethod() {
        List<String> methods = facade.getPaymentMethodNames();
        if (methods.isEmpty()) return "Credit Card";
        List<String> options = new ArrayList<>(methods);
        int choice = console.selectOption("Select payment method:", options, false);
        return options.get(choice - 1);
    }

    @Override
    protected User selectUser() {
        List<User> users = facade.getUsers();
        if (users.isEmpty()) {
            console.println("No users. Please add a user first.");
            return null;
        }
        List<String> options = new ArrayList<>();
        for (User u : users) {
            options.add(u.getUserId() + " - " + u.getName() + " (" + u.getEmail() + ")");
        }
        int choice = console.selectOption("Select user (login):", options, true);
        if (choice == 0) return null;
        return users.get(choice - 1);
    }

    @Override
    protected BookingCommand createMakeReservationCommand(String userId, String slotId,
                                                           LocalDateTime start, LocalDateTime end,
                                                           String paymentMethod) {
        return facade.createMakeReservationCommand(userId, slotId, start, end, paymentMethod);
    }
}
