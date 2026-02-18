package smartparking.model;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Domain model: ChargingStation (Iteration 2 - Charge Vehicle).
 * Physical location with charging infrastructure. Contains ChargingSlots.
 */
public class ChargingStation {
    public static final String STATION_STATUS_ACTIVE = "active";
    public static final String STATION_STATUS_OUT_OF_SERVICE = "out_of_service";

    private String stationId;
    private String stationName;
    private String location;
    private String stationStatus;
    private List<ChargingSlot> slots;

    public ChargingStation() {
        this.stationStatus = STATION_STATUS_ACTIVE;
        this.slots = new ArrayList<>();
    }

    public ChargingStation(String stationId, String stationName, String location) {
        this();
        this.stationId = stationId;
        this.stationName = stationName;
        this.location = location;
    }

    public String getStationId() { return stationId; }
    public void setStationId(String stationId) { this.stationId = stationId; }
    public String getStationName() { return stationName; }
    public void setStationName(String stationName) { this.stationName = stationName; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public String getStationStatus() { return stationStatus; }
    public void setStationStatus(String stationStatus) { this.stationStatus = stationStatus; }
    public List<ChargingSlot> getSlots() { return slots; }
    public void setSlots(List<ChargingSlot> slots) { this.slots = slots != null ? slots : new ArrayList<>(); }

    /** Get available slots at this station (matches location filter when location is provided). */
    public List<ChargingSlot> getAvailableSlots(String locationFilter) {
        if (!STATION_STATUS_ACTIVE.equalsIgnoreCase(stationStatus)) return List.of();
        String filter = (locationFilter != null) ? locationFilter.trim() : "";
        if (!filter.isEmpty()) {
            String stationLoc = (location != null) ? location.trim().toLowerCase() : "";
            if (stationLoc.isEmpty() || !stationLoc.contains(filter.toLowerCase())) {
                return List.of();
            }
        }
        return slots.stream()
                .filter(ChargingSlot::isAvailable)
                .collect(Collectors.toList());
    }

    /** Get slot details for a charging mode (delegates to first matching slot with that mode). */
    public ChargingSlot.ChargingDetails getSlotDetails(String modeType) {
        for (ChargingSlot slot : slots) {
            ChargingSlot.ChargingDetails details = slot.getChargingDetails(modeType);
            if (details != null) return details;
        }
        return null;
    }

    public ChargingSlot getSlotById(String slotId) {
        return slots.stream()
                .filter(s -> slotId != null && slotId.equals(s.getSlotId()))
                .findFirst()
                .orElse(null);
    }

    @Override
    public String toString() {
        return String.format("ChargingStation{stationId='%s', name='%s', location='%s', status='%s', slots=%d}",
                stationId, stationName, location, stationStatus, slots.size());
    }
}
