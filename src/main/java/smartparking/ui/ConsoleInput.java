package smartparking.ui;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Abstraction for console input (Adapter for I/O). Enables testing and swapping implementations.
 * Template Method flow uses this for primitive operations (read user choice, read line, etc.).
 */
public interface ConsoleInput {

    /** Print a message (no newline unless included in message). */
    void print(String message);

    /** Print line. */
    void println(String message);

    /** Read a non-empty line; null or empty retries until non-empty (or return blank if allowBlank). */
    String readLine(String prompt, boolean allowBlank);

    /** Read integer in range [min, max]; invalid input retries. */
    int readIntInRange(String prompt, int min, int max);

    /** Display numbered options and return 1-based selection (or 0 for cancel). */
    int selectOption(String title, List<String> options, boolean allowCancel);

    /** Read date-time (e.g. "now", "now+2h" or full format). Simplified for demo. */
    LocalDateTime readDateTime(String prompt, LocalDateTime defaultValue);
}
