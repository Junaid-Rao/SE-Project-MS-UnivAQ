package smartparking.reporting;

import smartparking.model.*;
import smartparking.persistence.PersistentManager;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Reporting perspective: generate text reports from persisted data.
 * Outputs to data/reports/ for iteration documentation and exam presentation.
 */
public class ReportGenerator {

    private static final String REPORTS_DIR = "data/reports";
    private static final DateTimeFormatter DT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final PersistentManager persistence;

    public ReportGenerator(PersistentManager persistence) {
        this.persistence = persistence;
    }

    /** Generate a summary report (users, lots, reservations, payments) to a file. */
    public Path generateSummaryReport() throws IOException {
        Path dir = Paths.get(REPORTS_DIR);
        Files.createDirectories(dir);
        Path file = dir.resolve("summary_report_" + System.currentTimeMillis() + ".txt");

        StringBuilder sb = new StringBuilder();
        sb.append("=== Smart Parking System - Summary Report (Iteration 1 & 2) ===\n\n");

        sb.append("--- Users ---\n");
        for (User u : persistence.findAllUsers()) {
            sb.append("  ").append(u).append("\n");
        }
        sb.append("\n--- Parking Lots ---\n");
        for (ParkingLot lot : persistence.findAllParkingLots()) {
            sb.append("  ").append(lot).append("\n");
            for (ParkingSlot s : lot.getSlots()) {
                sb.append("    ").append(s).append("\n");
            }
        }
        sb.append("\n--- Reservations ---\n");
        for (Reservation r : persistence.findAllReservations()) {
            sb.append("  ").append(r).append("\n");
        }
        sb.append("\n--- Payments ---\n");
        for (Payment p : persistence.findAllPayments()) {
            sb.append("  ").append(p).append("\n");
            sb.append("    ").append(p.generateReceipt()).append("\n");
        }
        sb.append("\n--- Charging Stations & Sessions (Iteration 2) ---\n");
        for (ChargingStation st : persistence.findAllChargingStations()) {
            sb.append("  ").append(st).append("\n");
            for (ChargingSlot s : st.getSlots()) {
                sb.append("    ").append(s).append("\n");
            }
        }
        for (ChargingSession cs : persistence.findAllChargingSessions()) {
            sb.append("  ").append(cs).append("\n");
        }

        Files.writeString(file, sb.toString());
        return file;
    }

    /** Generate reservations report with formatted details. */
    public Path generateReservationsReport() throws IOException {
        Path dir = Paths.get(REPORTS_DIR);
        Files.createDirectories(dir);
        Path file = dir.resolve("reservations_report_" + System.currentTimeMillis() + ".txt");

        List<Reservation> list = persistence.findAllReservations();
        StringBuilder sb = new StringBuilder();
        sb.append("=== Reservations Report ===\n");
        sb.append("Total: ").append(list.size()).append("\n\n");
        for (Reservation r : list) {
            sb.append("ID: ").append(r.getReservationId()).append("\n");
            sb.append("  User: ").append(r.getUserId()).append(" | Slot: ").append(r.getSlotId()).append("\n");
            sb.append("  Start: ").append(r.getStartTime() != null ? r.getStartTime().format(DT) : "-").append("\n");
            sb.append("  End: ").append(r.getEndTime() != null ? r.getEndTime().format(DT) : "-").append("\n");
            sb.append("  Status: ").append(r.getReservationStatus()).append(" | Total Cost: ").append(r.getTotalCost()).append("\n\n");
        }
        Files.writeString(file, sb.toString());
        return file;
    }
}
