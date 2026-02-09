package smartparking.command;

import smartparking.model.Payment;
import smartparking.model.Reservation;
import smartparking.service.MakeReservationService;
import smartparking.service.MakeReservationService.MakeReservationResult;

import java.time.LocalDateTime;

/**
 * Command pattern: encapsulates "make reservation" request. Invoker can execute without
 * knowing the details of the operation.
 */
public class MakeReservationCommand implements BookingCommand {

    private final MakeReservationService service;
    private final String userId;
    private final String slotId;
    private final LocalDateTime startTime;
    private final LocalDateTime endTime;
    private final String paymentMethod;

    public MakeReservationCommand(MakeReservationService service,
                                  String userId, String slotId,
                                  LocalDateTime startTime, LocalDateTime endTime,
                                  String paymentMethod) {
        this.service = service;
        this.userId = userId;
        this.slotId = slotId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.paymentMethod = paymentMethod != null ? paymentMethod : "Credit Card";
    }

    @Override
    public CommandResult execute() {
        MakeReservationResult result = service.makeReservation(userId, slotId, startTime, endTime, paymentMethod);
        if (result.isSuccess()) {
            return CommandResult.success(result.getMessage(), result.getReservation(), result.getPayment());
        }
        return CommandResult.failure(result.getMessage());
    }

    @Override
    public String getDescription() {
        return "MakeReservation(user=" + userId + ", slot=" + slotId + ")";
    }
}
