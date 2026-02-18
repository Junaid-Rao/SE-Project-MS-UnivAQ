package smartparking.flow;

import smartparking.controller.ChargingController;
import smartparking.facade.BookingFacade;
import smartparking.model.ChargingSession;
import smartparking.model.ChargingSlot;
import smartparking.model.User;
import smartparking.service.ChargingService;
import smartparking.ui.ConsoleInput;

import java.math.BigDecimal;
import java.util.List;

/**
 * Interactive flow for Use Case 2: Charge Vehicle.
 * Steps: select user → startCharging → requestChargingSlot(location) → selectChargingMode → processPayment → (later) stopCharging.
 */
public class ChargingFlow {

    private final ChargingController controller;
    private final BookingFacade bookingFacade;
    private final ConsoleInput console;

    public ChargingFlow(ChargingController controller, BookingFacade bookingFacade, ConsoleInput console) {
        this.controller = controller;
        this.bookingFacade = bookingFacade;
        this.console = console;
    }

    public ChargingFlowResult runFlow() {
        console.println("\n--- Charge Vehicle (Use Case 2) ---");

        List<User> users = bookingFacade.getUsers();
        if (users == null || users.isEmpty()) {
            return ChargingFlowResult.failure("No users in system.");
        }
        List<String> userOptions = new java.util.ArrayList<>();
        for (User u : users) {
            userOptions.add(u.getUserId() + " - " + u.getName());
        }
        int userChoice = console.selectOption("Select driver (user):", userOptions, true);
        if (userChoice == 0) return ChargingFlowResult.cancelled();
        String userId = users.get(userChoice - 1).getUserId();

        ChargingService.StartChargingResult startResult = controller.startCharging(userId);
        if (!startResult.isSuccess()) {
            return ChargingFlowResult.failure(startResult.getMessage());
        }
        ChargingSession session = startResult.getSession();
        console.println("Session started: " + session.getSessionId() + " at " + session.getStartTime());

        String location = console.readLine("Enter location (or press Enter for any): ", true);
        location = (location != null) ? location.trim() : "";
        List<ChargingSlot> slots = controller.requestChargingSlot(location);
        if (slots.isEmpty()) {
            return ChargingFlowResult.failure("No available charging slots at this location.");
        }
        List<String> slotOptions = new java.util.ArrayList<>();
        for (ChargingSlot s : slots) {
            slotOptions.add(s.getSlotId() + " | " + s.getSlotNumber() + " | " + s.getStationId());
        }
        int slotChoice = console.selectOption("Select charging slot:", slotOptions, true);
        if (slotChoice == 0) return ChargingFlowResult.cancelled();
        ChargingSlot selectedSlot = slots.get(slotChoice - 1);

        List<String> modeTypes = controller.getChargingModeTypes();
        List<String> modeOptions = new java.util.ArrayList<>(modeTypes);
        int modeChoice = console.selectOption("Select charging mode (fast / normal):", modeOptions, true);
        if (modeChoice == 0) return ChargingFlowResult.cancelled();
        String modeType = modeTypes.get(modeChoice - 1);

        ChargingSlot.ChargingDetails details = controller.selectChargingMode(modeType);
        if (details == null) {
            return ChargingFlowResult.failure("Charging mode not available: " + modeType);
        }
        BigDecimal pricePerUnit = details.getPricePerUnit();
        console.println("Charging details: " + details.getModeType() + " | " + pricePerUnit + " per kWh | Speed " + details.getChargingSpeed() + " kW");
        String estInput = console.readLine("Estimate energy (kWh) to charge (e.g. 20): ", true);
        double estKwh = 20.0;
        if (estInput != null && !estInput.isBlank()) {
            try {
                estKwh = Double.parseDouble(estInput.trim());
            } catch (NumberFormatException ignored) {}
        }
        BigDecimal amount = pricePerUnit.multiply(BigDecimal.valueOf(estKwh));

        List<String> paymentMethods = List.of("Credit Card", "PayPal");
        int payChoice = console.selectOption("Select payment method:", paymentMethods, true);
        if (payChoice == 0) return ChargingFlowResult.cancelled();
        String paymentMethod = paymentMethods.get(payChoice - 1);

        ChargingService.ProcessPaymentResult payResult = controller.processPayment(paymentMethod, amount, session,
                selectedSlot.getSlotId(), modeType, pricePerUnit);
        if (!payResult.isSuccess()) {
            return ChargingFlowResult.failure("Payment failed: " + payResult.getMessage());
        }
        console.println("Payment approved.");
        console.println("Start time: " + session.getStartTime());
        console.println("Receipt: " + payResult.getReceipt());
        if (payResult.getReward() != null) console.println("Reward: " + payResult.getReward());

        String stopNow = console.readLine("Stop charging now? (y/n): ", true);
        if (stopNow != null && stopNow.trim().equalsIgnoreCase("y")) {
            ChargingService.StopChargingResult stopResult = controller.stopCharging(session.getSessionId());
            if (stopResult.isSuccess()) {
                console.println("Charging stopped. End time: " + stopResult.getEndTime() + ", Energy used: " + stopResult.getEnergyUsedKwh() + " kWh");
            } else {
                console.println("Stop failed: " + stopResult.getMessage());
            }
        } else {
            console.println("You can stop charging later from the main menu (Stop Charging).");
        }

        return ChargingFlowResult.success(session, payResult.getPayment());
    }

    public static final class ChargingFlowResult {
        private final boolean success;
        private final boolean cancelled;
        private final String message;
        private final ChargingSession session;
        private final smartparking.model.Payment payment;

        private ChargingFlowResult(boolean success, boolean cancelled, String message, ChargingSession session, smartparking.model.Payment payment) {
            this.success = success;
            this.cancelled = cancelled;
            this.message = message;
            this.session = session;
            this.payment = payment;
        }
        public static ChargingFlowResult success(ChargingSession session, smartparking.model.Payment payment) {
            return new ChargingFlowResult(true, false, "Charging session completed.", session, payment);
        }
        public static ChargingFlowResult failure(String message) {
            return new ChargingFlowResult(false, false, message, null, null);
        }
        public static ChargingFlowResult cancelled() {
            return new ChargingFlowResult(false, true, "Cancelled.", null, null);
        }
        public boolean isSuccess() { return success; }
        public boolean isCancelled() { return cancelled; }
        public String getMessage() { return message; }
        public ChargingSession getSession() { return session; }
        public smartparking.model.Payment getPayment() { return payment; }
    }
}
