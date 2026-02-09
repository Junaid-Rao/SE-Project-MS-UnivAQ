package smartparking.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Domain model: User. Can make and cancel reservations.
 */
public class User {
    private String userId;
    private String name;
    private String email;
    private String phoneNumber;
    private String password;
    private List<Reservation> reservations;

    public User() {
        this.reservations = new ArrayList<>();
    }

    public User(String userId, String name, String email, String phoneNumber, String password) {
        this();
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.password = password;
    }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public List<Reservation> getReservations() { return reservations; }
    public void setReservations(List<Reservation> reservations) { this.reservations = reservations != null ? reservations : new ArrayList<>(); }

    /** Register this user (persistence is handled by PersistentManager). */
    public boolean register() {
        return userId != null && !userId.isBlank() && email != null && !email.isBlank();
    }

    /** Check credentials (no side effect; actual login/session is outside domain). */
    public boolean login(String email, String password) {
        return this.email != null && this.email.equals(email)
                && this.password != null && this.password.equals(password);
    }

    /**
     * Initiate a reservation. Creation and linking are done by domain service;
     * this method represents the user action. Returns the created reservation or null.
     */
    public Reservation makeReservation(String slotId, LocalDateTime startTime, LocalDateTime endTime) {
        // Responsibility delegated to Reservation / service layer for full flow
        return null;
    }

    /** Cancel a reservation by id (returns true if found and cancelled). */
    public boolean cancelReservation(String reservationId) {
        return reservations.removeIf(r -> reservationId != null && reservationId.equals(r.getReservationId()));
    }

    @Override
    public String toString() {
        return String.format("User{userId='%s', name='%s', email='%s'}",
                userId, name, email);
    }
}
