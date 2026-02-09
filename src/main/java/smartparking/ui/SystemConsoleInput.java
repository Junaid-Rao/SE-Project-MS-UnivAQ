package smartparking.ui;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Scanner;

/**
 * Console input implementation using System.in and Scanner.
 */
public class SystemConsoleInput implements ConsoleInput {

    private final Scanner scanner = new Scanner(System.in);

    @Override
    public void print(String message) {
        System.out.print(message);
    }

    @Override
    public void println(String message) {
        System.out.println(message);
    }

    @Override
    public String readLine(String prompt, boolean allowBlank) {
        while (true) {
            print(prompt);
            String line = scanner.nextLine();
            if (line != null) line = line.trim();
            if (allowBlank || (line != null && !line.isBlank())) return line != null ? line : "";
        }
    }

    @Override
    public int readIntInRange(String prompt, int min, int max) {
        while (true) {
            print(prompt);
            String line = scanner.nextLine();
            try {
                int n = Integer.parseInt(line.trim());
                if (n >= min && n <= max) return n;
            } catch (NumberFormatException ignored) { }
            println("Please enter a number between " + min + " and " + max + ".");
        }
    }

    @Override
    public int selectOption(String title, List<String> options, boolean allowCancel) {
        println(title);
        for (int i = 0; i < options.size(); i++) {
            println("  " + (i + 1) + ". " + options.get(i));
        }
        if (allowCancel) {
            println("  0. Cancel");
        }
        int max = allowCancel ? options.size() : options.size();
        int min = allowCancel ? 0 : 1;
        return readIntInRange("Enter choice: ", min, max);
    }

    @Override
    public LocalDateTime readDateTime(String prompt, LocalDateTime defaultValue) {
        print(prompt + " (e.g. now, now+1, now+2 for hours; or press Enter for " + defaultValue + "): ");
        String line = scanner.nextLine();
        if (line == null || line.isBlank()) return defaultValue;
        line = line.trim().toLowerCase();
        if (line.equals("now")) return LocalDateTime.now();
        if (line.startsWith("now+")) {
            try {
                int hours = Integer.parseInt(line.substring(4).trim());
                return LocalDateTime.now().plusHours(hours);
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        }
        return defaultValue;
    }
}
