package smartparking.command;

/**
 * Command pattern: encapsulates a request (make reservation, cancel reservation) as an object.
 * Allows parameterization of clients, queuing, and undo support. Invoker calls execute().
 */
public interface BookingCommand {

    /** Execute the command. Returns result describing success/failure. */
    CommandResult execute();

    /** Optional: human-readable description for logging/UI. */
    default String getDescription() {
        return getClass().getSimpleName();
    }
}
