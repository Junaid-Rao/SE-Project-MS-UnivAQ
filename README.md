# Smart Parking System

Software Engineering project — **Iteration 1**. Implements **Use Case 1: Reserve Parking Slot** with **interactive** flow (select user → select slot → confirm → pay), file-based persistence, and reporting.

**Exam/presentation notes:** See `docs/EXAM_AND_PRESENTATION_NOTES.md` for OOA/OOD discussion notes, presentation outline, and design pattern logic (for teacher Q&A).

## Design pattern

| Pattern | Where | Purpose |
|--------|--------|--------|
| **Facade** | `BookingFacade` | Single entry point for booking (slots, users, make/cancel reservation). Hides persistence, service, and commands. |
| **Strategy** | `PaymentStrategy`, `CreditCardPaymentStrategy`, `PayPalPaymentStrategy`, `PaymentContext`, `PaymentStrategyRegistry` | Interchangeable payment methods; add new methods without changing client code (Open/Closed). |
| **Command** | `BookingCommand`, `MakeReservationCommand`, `CancelReservationCommand` | Encapsulate make/cancel reservation as objects; invoker calls `execute()`. Supports queuing and undo semantics. |
| **Template Method** | `AbstractBookingFlow`, `InteractiveBookingFlow` | Fixed booking steps (select user → slot → times → confirm → payment); primitive ops (I/O) implemented by subclass (console). |
| **Builder** | `BookingRequestBuilder`, `BookingRequest` | Construct a valid booking request step by step with validation. |
| **Persistence Facade** | `PersistentManager`, `FilePersistentManager` | Single access point for all persistence (Larman). |

## Interactive flow

On run, the user sees a **main menu**:

1. **Book a parking slot** — Template Method flow: select user → list available slots → select slot → enter start/duration → confirm summary → select payment method → execute (Command) → show receipt.
2. **View available slots** — List all available slots.
3. **View my reservations** — Select user, then list their reservations.
4. **Cancel a reservation** — Select an active reservation and cancel (Command).
5. **Generate report (file)** — Write summary and reservations reports to `data/reports/`.
6. **Exit** — Quit.

## Project structure

```
src/main/java/smartparking/
├── Application.java                 # Entry point; interactive main menu
├── model/                            # Domain model (from class diagram)
│   ├── User.java, Reservation.java, ParkingSlot.java, ParkingLot.java
│   ├── Payment.java, PaymentGateway.java, Navigation.java
├── persistence/
│   ├── PersistentManager.java       # Facade (interface)
│   └── FilePersistentManager.java   # File-based implementation (JSON)
├── service/
│   └── MakeReservationService.java  # Use Case 1: Reserve Parking Slot (uses Strategy for payment)
├── strategy/                         # Strategy pattern (payment methods)
│   ├── PaymentStrategy.java, PaymentContext.java
│   ├── CreditCardPaymentStrategy.java, PayPalPaymentStrategy.java
│   ├── PaymentStrategyRegistry.java, DefaultPaymentStrategyRegistry.java
├── command/                          # Command pattern
│   ├── BookingCommand.java, CommandResult.java
│   ├── MakeReservationCommand.java, CancelReservationCommand.java
├── flow/                             # Template Method (booking flow)
│   ├── AbstractBookingFlow.java, InteractiveBookingFlow.java
├── facade/
│   └── BookingFacade.java            # Facade for booking subsystem
├── builder/
│   ├── BookingRequest.java, BookingRequestBuilder.java
├── ui/
│   ├── ConsoleInput.java, SystemConsoleInput.java
└── reporting/
    └── ReportGenerator.java
```

## Use Case 1: Reserve Parking Slot

**Main success scenario:**

1. User is identified (by userId).
2. System finds the requested parking slot and checks availability (`ParkingSlot.checkAvailability()`).
3. System creates a reservation (`Reservation.createReservation()`), links User and ParkingSlot.
4. System calculates cost (`Reservation.calculateCost()` using `ParkingSlot.calculatePrice()`).
5. User pays; system processes payment via `PaymentGateway` (`Payment.processPayment()`).
6. Slot is reserved (`ParkingSlot.reserve()`), reservation status set to Confirmed.
7. All data is persisted via **PersistentManager** (users, lots, reservations, payments).

## Data persistence (filing)

- **Location**: `data/` (created on first run).
- **Format**: JSON files — `users.json`, `parkinglots.json`, `reservations.json`, `payments.json`.
- **Reports**: `data/reports/` — summary and reservations reports (text files).

No database is used; everything is file-based for Iteration 1.

## Build and run

**Prerequisites:** Java 17+, Maven 3.6+

```bash
# Build
mvn clean compile

# Run (interactive menu: book slot, view slots, cancel, report)
mvn exec:java -Dexec.mainClass="smartparking.Application"
```

Or run the main class from your IDE: `smartparking.Application`. Use the menu to **Book a parking slot** (select user → select slot → confirm → choose payment method).

## Iteration 1 deliverables

- **Domain model**: Classes and operations from the class diagram (User, Reservation, ParkingSlot, ParkingLot, Payment, PaymentGateway, Navigation).
- **Use Case 1**: Reserve Parking Slot implemented end-to-end with filing.
- **PersistentManager**: Declared as the single access point for persistence; implemented as `FilePersistentManager`.
- **Reporting**: Console output and file reports in `data/reports/` for documentation and exam use.
