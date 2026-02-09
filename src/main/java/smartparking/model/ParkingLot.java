package smartparking.model;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Domain model: ParkingLot (composition owner of ParkingSlot).
 * Contains one or more parking slots.
 */
public class ParkingLot {
    private String lotId;
    private String name;
    private String address;
    private List<ParkingSlot> slots;

    public ParkingLot() {
        this.slots = new ArrayList<>();
    }

    public ParkingLot(String lotId, String name, String address) {
        this();
        this.lotId = lotId;
        this.name = name;
        this.address = address;
    }

    public String getLotId() { return lotId; }
    public void setLotId(String lotId) { this.lotId = lotId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public List<ParkingSlot> getSlots() { return slots; }
    public void setSlots(List<ParkingSlot> slots) { this.slots = slots != null ? slots : new ArrayList<>(); }

    /** Get slots that are currently available. */
    public List<ParkingSlot> getAvailableSlots() {
        return slots.stream()
                .filter(ParkingSlot::isAvailable)
                .collect(Collectors.toList());
    }

    /** Find slots by type (e.g. Standard, EV, Handicap). */
    public List<ParkingSlot> findSlotByType(String type) {
        if (type == null) return List.of();
        return slots.stream()
                .filter(s -> type.equalsIgnoreCase(s.getSlotType()))
                .collect(Collectors.toList());
    }

    public ParkingSlot getSlotById(String slotId) {
        return slots.stream()
                .filter(s -> slotId != null && slotId.equals(s.getSlotId()))
                .findFirst()
                .orElse(null);
    }

    @Override
    public String toString() {
        return String.format("ParkingLot{lotId='%s', name='%s', address='%s', slots=%d}",
                lotId, name, address, slots.size());
    }
}
