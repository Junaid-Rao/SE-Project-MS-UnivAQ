package smartparking;

import smartparking.flow.AbstractBookingFlow;
import smartparking.flow.InteractiveBookingFlow;
import smartparking.model.*;
import smartparking.persistence.FilePersistentManager;
import smartparking.persistence.PersistentManager;
import smartparking.reporting.ReportGenerator;
import smartparking.service.MakeReservationService;
import smartparking.strategy.DefaultPaymentStrategyRegistry;
import smartparking.strategy.PaymentStrategyRegistry;
import smartparking.facade.BookingFacade;
import smartparking.ui.ConsoleInput;
import smartparking.ui.SystemConsoleInput;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

/**
 * Application entry point — interactive Smart Parking System (Iteration 1).
 * Design patterns: Facade (BookingFacade), Strategy (payment), Command (make/cancel reservation),
 * Template Method (booking flow), Builder (BookingRequest). Persistence: filing only.
 */
public class Application {

    public static void main(String[] args) {
        PersistentManager persistence = new FilePersistentManager();
        seedDataIfNeeded(persistence);

        Optional<PaymentGateway> gatewayOpt = persistence.getDefaultPaymentGateway();
        PaymentGateway gateway = gatewayOpt.orElseThrow(() -> new IllegalStateException("Payment gateway not available"));
        PaymentStrategyRegistry paymentRegistry = new DefaultPaymentStrategyRegistry(gateway);
        MakeReservationService makeReservationService = new MakeReservationService(persistence, paymentRegistry);
        BookingFacade facade = new BookingFacade(persistence, makeReservationService);

        ConsoleInput console = new SystemConsoleInput();

        console.println("=== Smart Parking System — Iteration 1 (Use Case: Reserve Parking Slot) ===");
        console.println("Interactive booking: select user → select slot → confirm → pay.\n");

        mainMenuLoop(facade, persistence, console);
    }

    private static void mainMenuLoop(BookingFacade facade, PersistentManager persistence, ConsoleInput console) {
        while (true) {
            console.println("\n--- Main Menu ---");
            int choice = console.selectOption("Choose an option:",
                    List.of(
                            "Book a parking slot",
                            "View available slots",
                            "View my reservations",
                            "Cancel a reservation",
                            "Generate report (file)",
                            "Exit"
                    ), false);

            switch (choice) {
                case 1 -> runBookingFlow(facade, console);
                case 2 -> showAvailableSlots(facade, console);
                case 3 -> showMyReservations(facade, console);
                case 4 -> cancelReservation(facade, console);
                case 5 -> generateReport(persistence, console);
                case 6 -> {
                    console.println("Goodbye.");
                    return;
                }
                default -> console.println("Invalid option.");
            }
        }
    }

    private static void runBookingFlow(BookingFacade facade, ConsoleInput console) {
        console.println("\n--- Book a Parking Slot ---");
        AbstractBookingFlow flow = new InteractiveBookingFlow(facade, console);
        AbstractBookingFlow.FlowResult result = flow.runFlow();

        if (result.isSuccess()) {
            console.println("\nSUCCESS: " + result.getMessage());
            console.println("  Reservation: " + result.getReservation());
            console.println("  Payment: " + result.getPayment());
            if (result.getPayment() != null) {
                console.println("  Receipt: " + result.getPayment().generateReceipt());
            }
        } else if (result.isCancelled()) {
            console.println("\nCancelled: " + result.getMessage());
        } else {
            console.println("\nFAILED: " + result.getMessage());
        }
    }

    private static void showAvailableSlots(BookingFacade facade, ConsoleInput console) {
        List<ParkingSlot> slots = facade.getAvailableSlots();
        console.println("\n--- Available Slots ---");
        if (slots.isEmpty()) {
            console.println("No slots available.");
            return;
        }
        for (ParkingSlot s : slots) {
            console.println("  " + s.getSlotId() + " | " + s.getSlotNumber() + " | " + s.getSlotType() + " | $" + s.getPricePerHour() + "/hr");
        }
    }

    private static void showMyReservations(BookingFacade facade, ConsoleInput console) {
        List<User> users = facade.getUsers();
        if (users.isEmpty()) {
            console.println("No users.");
            return;
        }
        List<String> options = new java.util.ArrayList<>();
        for (User u : users) {
            options.add(u.getUserId() + " - " + u.getName());
        }
        int userChoice = console.selectOption("Select user to view reservations:", options, true);
        if (userChoice == 0) return;
        User user = users.get(userChoice - 1);
        List<Reservation> reservations = facade.getReservationsByUser(user.getUserId());
        console.println("\n--- Reservations for " + user.getName() + " ---");
        if (reservations.isEmpty()) {
            console.println("No reservations.");
            return;
        }
        for (Reservation r : reservations) {
            console.println("  " + r);
        }
    }

    private static void cancelReservation(BookingFacade facade, ConsoleInput console) {
        List<Reservation> all = facade.getAllReservations().stream()
                .filter(r -> !Reservation.STATUS_CANCELLED.equals(r.getReservationStatus()))
                .toList();
        if (all.isEmpty()) {
            console.println("No active reservations to cancel.");
            return;
        }
        List<String> options = new java.util.ArrayList<>();
        for (Reservation r : all) {
            options.add(r.getReservationId() + " | " + r.getSlotId() + " | " + r.getReservationStatus());
        }
        int choice = console.selectOption("Select reservation to cancel:", options, true);
        if (choice == 0) return;
        Reservation selected = all.get(choice - 1);
        var result = facade.cancelReservation(selected.getReservationId());
        if (result.isSuccess()) {
            console.println("Cancelled: " + result.getMessage());
        } else {
            console.println("Failed: " + result.getMessage());
        }
    }

    private static void generateReport(PersistentManager persistence, ConsoleInput console) {
        ReportGenerator reportGenerator = new ReportGenerator(persistence);
        try {
            Path summary = reportGenerator.generateSummaryReport();
            Path reservations = reportGenerator.generateReservationsReport();
            console.println("Reports written: " + summary + ", " + reservations);
        } catch (Exception e) {
            console.println("Report generation failed: " + e.getMessage());
        }
    }

    private static void seedDataIfNeeded(PersistentManager p) {
        if (!p.findAllUsers().isEmpty()) return;

        User user = new User("U001", "Junaid", "junaid.aslam@student.univaq.it", "+393277766533", "pass1");
        user.register();
        p.saveUser(user);

        ParkingLot lot = new ParkingLot("L001", "Central Lot", "123 Main St");
        lot.getSlots().add(new ParkingSlot("S001", "A-01", "Standard", new BigDecimal("5.00")));
        lot.getSlots().add(new ParkingSlot("S002", "A-02", "EV", new BigDecimal("7.50")));
        lot.getSlots().add(new ParkingSlot("S003", "B-01", "Handicap", new BigDecimal("4.00")));
        p.saveParkingLot(lot);
    }
}
