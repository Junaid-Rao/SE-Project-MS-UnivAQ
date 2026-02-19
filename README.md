# Smart Parking System

Software Engineering project — **Iteration 1 & 2**. Implements **Use Case 1: Reserve Parking Slot** and **Use Case 2: Charge Vehicle** with interactive flows, file-based persistence, and reporting.

## Requirements (from course)

- **Process**: UP (Unified Process), iterative and incremental; at least 3 iterations (Inception + 2 Elaboration).
- **Persistence**: No database required for now; **filing** (file-based storage) is used.
- **Domain layer**: Design limited to Domain layer; persistence accessed via a **PersistentManager** facade (declared, file implementation).
- **Reporting**: Data and reports can be produced for documentation and exam presentation.

## Design patterns (testament of design)

| Pattern | Where | Purpose |
|--------|--------|--------|
| **Facade** | `BookingFacade` | Single entry point for booking (slots, users, make/cancel reservation). Hides persistence, service, and commands. |
| **Controller** | `ChargingController` | Entry point for Use Case 2: startCharging, requestChargingSlot, selectChargingMode, processPayment, stopCharging. |
| **Strategy** | `PaymentStrategy`, `CreditCardPaymentStrategy`, `PayPalPaymentStrategy`, `PaymentContext`, `PaymentStrategyRegistry` | Interchangeable payment methods; add new methods without changing client code (Open/Closed). |
| **Command** | `BookingCommand`, `MakeReservationCommand`, `CancelReservationCommand` | Encapsulate make/cancel reservation as objects; invoker calls `execute()`. Supports queuing and undo semantics. |
| **Template Method** | `AbstractBookingFlow`, `InteractiveBookingFlow`, `ChargingFlow` | Fixed booking/charging steps; primitive ops (I/O) implemented by console. |
| **Builder** | `BookingRequestBuilder`, `BookingRequest` | Construct a valid booking request step by step with validation. |
| **Persistence Facade** | `PersistentManager`, `FilePersistentManager` | Single access point for all persistence (Larman). |

## Interactive flow (main menu)

On run, the user sees a **main menu**:

1. **Book a parking slot** — Template Method flow: select user → list available slots → select slot → enter start/duration → confirm summary → select payment method → execute (Command) → show receipt.
2. **View available slots** — List all available parking slots.
3. **View my reservations** — Select user, then list their reservations.
4. **Cancel a reservation** — Select an active reservation and cancel (Command).
5. **Charge Vehicle (Use Case 2)** — Select driver → start session → enter location → select charging slot → select mode (fast/normal) → pay → optional stop charging.
6. **Stop Charging** — Select an active charging session and end it (returns end time and energy used).
7. **View my charging sessions** — Select user, then list their charging sessions.
8. **Generate report (file)** — Write summary and reservations reports to `data/reports/` (includes charging stations and sessions).
9. **Exit** — Quit.

**Auto-deallocation:** When returning to the main menu, the system automatically releases resources whose time has passed: reservations past their end time are marked **Expired** and parking slots freed; charging sessions past their scheduled end time (default 2 hours from start) are completed and charging slots freed.

## Project structure

```
src/main/java/smartparking/
├── Application.java                 # Entry point; interactive main menu (Iteration 1 & 2)
├── model/                            # Domain model (from class diagram)
│   ├── User.java, Reservation.java, ParkingSlot.java, ParkingLot.java
│   ├── Payment.java, PaymentGateway.java, Navigation.java
│   ├── ChargingMode.java, ChargingStation.java, ChargingSlot.java, ChargingSession.java  # Iteration 2
├── persistence/
│   ├── PersistentManager.java       # Facade (interface)
│   └── FilePersistentManager.java   # File-based implementation (JSON)
├── service/
│   ├── MakeReservationService.java  # Use Case 1: Reserve Parking Slot (uses Strategy for payment)
│   └── ChargingService.java          # Use Case 2: Charge Vehicle (sessions, slots, payment)
├── controller/
│   └── ChargingController.java      # System operations for Charge Vehicle (SSD)
├── strategy/                         # Strategy pattern (payment methods)
│   ├── PaymentStrategy.java, PaymentContext.java
│   ├── CreditCardPaymentStrategy.java, PayPalPaymentStrategy.java
│   ├── PaymentStrategyRegistry.java, DefaultPaymentStrategyRegistry.java
├── command/                          # Command pattern
│   ├── BookingCommand.java, CommandResult.java
│   ├── MakeReservationCommand.java, CancelReservationCommand.java
├── flow/                             # Template Method (booking flow) + Charging flow
│   ├── AbstractBookingFlow.java, InteractiveBookingFlow.java
│   └── ChargingFlow.java             # Use Case 2 interactive flow
├── facade/
│   └── BookingFacade.java            # Facade for booking subsystem (+ releaseExpiredReservations)
├── builder/
│   ├── BookingRequest.java, BookingRequestBuilder.java
├── ui/
│   ├── ConsoleInput.java, SystemConsoleInput.java
└── reporting/
    └── ReportGenerator.java          # Summary + reservations + charging stations/sessions
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

Reservations past their **end time** are auto-marked **Expired** and slots released when the user returns to the main menu.

## Use Case 2: Charge Vehicle

**System operations (from SSD):**

1. **startCharging()** — Driver starts; system creates a charging session and returns session started.
2. **requestChargingSlot(location)** — Driver requests slots at a location; system returns available charging slots (location filter trimmed; blank = any).
3. **selectChargingMode(fast, normal)** — Driver selects mode; system returns charging details and price per unit.
4. **processPayment(paymentMethod)** — Driver pays; system authorizes via PaymentGateway, creates payment record, occupies slot; returns start time, receipt, reward (or payment failure).
5. **stopCharging()** — Driver stops; system ends session and returns end time and energy used.

**Domain model (Iteration 2):** Customer (User), ChargingMode, ChargingStation, ChargingSlot, ChargingSession, Payment (extended with `chargingSessionId`). Sessions have an optional **scheduledEndTime** (default 2 hours from start); when that time is passed, the session is auto-completed and the charging slot released on the next menu return.

**Payment and rewards:**
- **Unsuccessful payment (demo):** **PayPal** is configured to simulate a failed payment (SSD alt [payment failed]). Use **Credit Card** for a successful payment in both Book a parking slot and Charge Vehicle.
- **Loyalty rewards:** After a successful charge, the driver receives reward points. **Loyal customers** (at least 2 completed charging sessions or 2 confirmed reservations) get a bonus (e.g. 10 + 5 points). Others get the base points (10). Policy constants: `ChargingService.REWARD_BASE_POINTS`, `REWARD_LOYAL_BONUS_POINTS`, `LOYAL_CUSTOMER_THRESHOLD`.

## Data persistence (filing)

- **Location**: `data/` (created on first run).
- **Format**: JSON files:
  - **Iteration 1:** `users.json`, `parkinglots.json`, `reservations.json`, `payments.json`
  - **Iteration 2:** `chargingmodes.json`, `chargingstations.json`, `chargingsessions.json`
- **Reports**: `data/reports/` — summary report (users, lots, reservations, payments, charging stations/sessions) and reservations report (text files).

No database is used; everything is file-based.

## Build and run

**Prerequisites:** Java 17+, Maven 3.6+

```bash
# Build
mvn clean compile

# Run (interactive menu: book slot, charge vehicle, view/cancel/stop, report)
mvn exec:java -Dexec.mainClass="smartparking.Application"
```

Or run the main class from your IDE: `smartparking.Application`. Use the menu to **Book a parking slot** or **Charge Vehicle** (select user → location → slot → mode → pay → optionally stop).

## Seed data

On first run (when no users exist), the application seeds:

- One user (U001) and one parking lot with three slots (Standard, EV, Handicap).
- Two charging modes (normal, fast) and one charging station at "123 Main St" with two charging slots.

For **Charge Vehicle**, enter a location such as `123 Main St` or `Main`, or press Enter for any location.

## Iteration 1 & 2 deliverables

- **Domain model**: User, Reservation, ParkingSlot, ParkingLot, Payment, PaymentGateway, Navigation; plus ChargingMode, ChargingStation, ChargingSlot, ChargingSession (Iteration 2).
- **Use Case 1**: Reserve Parking Slot implemented end-to-end with filing and auto-expiry of reservations.
- **Use Case 2**: Charge Vehicle implemented with the five system operations, filing, and auto-release of charging sessions after scheduled end time.
- **PersistentManager**: Single access point for persistence; file implementation for all entities.
- **Reporting**: Console output and file reports in `data/reports/` including charging data.
