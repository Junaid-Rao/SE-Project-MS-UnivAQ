package smartparking;

import smartparking.controller.ChargingController;
import smartparking.flow.AbstractBookingFlow;
import smartparking.flow.ChargingFlow;
import smartparking.flow.InteractiveBookingFlow;
import smartparking.model.*;
import smartparking.persistence.FilePersistentManager;
import smartparking.persistence.PersistentManager;
import smartparking.reporting.ReportGenerator;
import smartparking.service.ChargingService;
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
 * Application entry point — Smart Parking System (Iteration 1 + Iteration 2).
 * Iteration 1: Reserve Parking Slot. Iteration 2: Charge Vehicle.
 * Design patterns: Facade (BookingFacade), ChargingController, Strategy (payment), Command, Template Method, Builder.
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

        ChargingService chargingService = new ChargingService(persistence, gateway);
        ChargingController chargingController = new ChargingController(chargingService);

        ConsoleInput console = new SystemConsoleInput();

        console.println("=== Smart Parking System — Iteration 1 & 2 ===");
        console.println("Use Case 1: Reserve Parking Slot | Use Case 2: Charge Vehicle\n");

        mainMenuLoop(facade, chargingController, persistence, console);
    }

    private static void mainMenuLoop(BookingFacade facade, ChargingController chargingController,
                                    PersistentManager persistence, ConsoleInput console) {
        while (true) {
            facade.releaseExpiredReservations();
            chargingController.releaseExpiredChargingSessions();

            console.println("\n--- Main Menu ---");
            int choice = console.selectOption("Choose an option:",
                    List.of(
                            "Book a parking slot",
                            "View available slots",
                            "View my reservations",
                            "Cancel a reservation",
                            "Charge Vehicle (Use Case 2)",
                            "Stop Charging",
                            "View my charging sessions",
                            "Generate report (file)",
                            "Exit"
                    ), false);

            switch (choice) {
                case 1 -> runBookingFlow(facade, console);
                case 2 -> showAvailableSlots(facade, console);
                case 3 -> showMyReservations(facade, console);
                case 4 -> cancelReservation(facade, console);
                case 5 -> runChargingFlow(facade, chargingController, console);
                case 6 -> stopCharging(chargingController, persistence, facade, console);
                case 7 -> showMyChargingSessions(persistence, facade, console);
                case 8 -> generateReport(persistence, console);
                case 9 -> {
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
                .filter(r -> Reservation.STATUS_CONFIRMED.equals(r.getReservationStatus())
                        || Reservation.STATUS_PENDING.equals(r.getReservationStatus()))
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

    private static void runChargingFlow(BookingFacade facade, ChargingController chargingController, ConsoleInput console) {
        ChargingFlow flow = new ChargingFlow(chargingController, facade, console);
        ChargingFlow.ChargingFlowResult result = flow.runFlow();
        if (result.isSuccess()) {
            console.println("\nSUCCESS: " + result.getMessage());
            if (result.getSession() != null) console.println("  Session: " + result.getSession());
            if (result.getPayment() != null) console.println("  Payment: " + result.getPayment());
        } else if (result.isCancelled()) {
            console.println("\nCancelled: " + result.getMessage());
        } else {
            console.println("\nFAILED: " + result.getMessage());
        }
    }

    private static void stopCharging(ChargingController chargingController, PersistentManager persistence,
                                    BookingFacade facade, ConsoleInput console) {
        List<User> users = facade.getUsers();
        if (users.isEmpty()) {
            console.println("No users.");
            return;
        }
        List<ChargingSession> active = persistence.findAllChargingSessions().stream()
                .filter(s -> ChargingSession.SESSION_STATUS_ACTIVE.equals(s.getSessionStatus()))
                .toList();
        if (active.isEmpty()) {
            console.println("No active charging sessions to stop.");
            return;
        }
        List<String> options = new java.util.ArrayList<>();
        for (ChargingSession s : active) {
            options.add(s.getSessionId() + " | User: " + s.getUserId() + " | Slot: " + s.getSlotId());
        }
        int choice = console.selectOption("Select session to stop:", options, true);
        if (choice == 0) return;
        String sessionId = active.get(choice - 1).getSessionId();
        var result = chargingController.stopCharging(sessionId);
        if (result.isSuccess()) {
            console.println("Stopped. End time: " + result.getEndTime() + ", Energy used: " + result.getEnergyUsedKwh() + " kWh");
        } else {
            console.println("Failed: " + result.getMessage());
        }
    }

    private static void showMyChargingSessions(PersistentManager persistence, BookingFacade facade, ConsoleInput console) {
        List<User> users = facade.getUsers();
        if (users.isEmpty()) {
            console.println("No users.");
            return;
        }
        List<String> options = new java.util.ArrayList<>();
        for (User u : users) {
            options.add(u.getUserId() + " - " + u.getName());
        }
        int userChoice = console.selectOption("Select user to view charging sessions:", options, true);
        if (userChoice == 0) return;
        String userId = users.get(userChoice - 1).getUserId();
        List<ChargingSession> sessions = persistence.findChargingSessionsByUserId(userId);
        console.println("\n--- Charging sessions for " + userId + " ---");
        if (sessions.isEmpty()) {
            console.println("No charging sessions.");
            return;
        }
        for (ChargingSession s : sessions) {
            console.println("  " + s);
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
        if (p.findAllUsers().isEmpty()) {
            User user = new User("U001", "Junaid", "junaid.aslam@student.univaq.it", "+393277766533", "pass1");
            user.register();
            p.saveUser(user);

            ParkingLot lot = new ParkingLot("L001", "Central Lot", "123 Main St");
            lot.getSlots().add(new ParkingSlot("S001", "A-01", "Standard", new BigDecimal("5.00")));
            lot.getSlots().add(new ParkingSlot("S002", "A-02", "EV", new BigDecimal("7.50")));
            lot.getSlots().add(new ParkingSlot("S003", "B-01", "Handicap", new BigDecimal("4.00")));
            p.saveParkingLot(lot);
        }

        if (p.findAllChargingModes().isEmpty()) {
            p.saveChargingMode(new ChargingMode("M1", "normal", new BigDecimal("0.25"), 7.0));
            p.saveChargingMode(new ChargingMode("M2", "fast", new BigDecimal("0.40"), 22.0));
        }
        if (p.findAllChargingStations().isEmpty()) {
            ChargingStation station = new ChargingStation("ST1", "Central EV Station", "123 Main St");
            ChargingMode normal = p.findAllChargingModes().stream().filter(m -> "normal".equalsIgnoreCase(m.getModeType())).findFirst().orElse(null);
            ChargingMode fast = p.findAllChargingModes().stream().filter(m -> "fast".equalsIgnoreCase(m.getModeType())).findFirst().orElse(null);
            ChargingSlot cs1 = new ChargingSlot("CS1", "1", "ST1");
            if (normal != null) cs1.getSupportedModes().add(normal);
            if (fast != null) cs1.getSupportedModes().add(fast);
            ChargingSlot cs2 = new ChargingSlot("CS2", "2", "ST1");
            if (normal != null) cs2.getSupportedModes().add(normal);
            station.getSlots().add(cs1);
            station.getSlots().add(cs2);
            p.saveChargingStation(station);
        }
    }
}
