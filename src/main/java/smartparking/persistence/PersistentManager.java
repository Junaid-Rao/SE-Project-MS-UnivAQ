package smartparking.persistence;

import smartparking.model.*;

import java.util.List;
import java.util.Optional;

/**
 * Facade for all persistence operations (Larman: "only access point for operations to the database").
 * For Iteration 1: file-based storage only. Service is declared here; implementation in FilePersistentManager.
 */
public interface PersistentManager {

    // --- Users ---
    Optional<User> findUserById(String userId);
    Optional<User> findUserByEmail(String email);
    List<User> findAllUsers();
    void saveUser(User user);

    // --- Parking lots and slots ---
    Optional<ParkingLot> findParkingLotById(String lotId);
    List<ParkingLot> findAllParkingLots();
    void saveParkingLot(ParkingLot lot);

    // --- Reservations ---
    Optional<Reservation> findReservationById(String reservationId);
    List<Reservation> findAllReservations();
    List<Reservation> findReservationsByUserId(String userId);
    void saveReservation(Reservation reservation);

    // --- Payments ---
    Optional<Payment> findPaymentById(String paymentId);
    List<Payment> findAllPayments();
    void savePayment(Payment payment);

    // --- Gateway (in-memory or single config for demo) ---
    Optional<PaymentGateway> getDefaultPaymentGateway();
}
