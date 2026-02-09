package smartparking.persistence;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import smartparking.model.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * File-based implementation of PersistentManager. Uses JSON files in a "data" directory.
 * Suitable for Iteration 1 (no database). Supports reporting via readable file contents.
 */
public class FilePersistentManager implements PersistentManager {

    private static final String DATA_DIR = "data";
    private static final String USERS_FILE = "users.json";
    private static final String LOTS_FILE = "parkinglots.json";
    private static final String RESERVATIONS_FILE = "reservations.json";
    private static final String PAYMENTS_FILE = "payments.json";

    private final Path dataPath;
    private final ObjectMapper mapper;
    private final ConcurrentHashMap<String, PaymentGateway> gatewayCache = new ConcurrentHashMap<>();

    public FilePersistentManager() {
        this(DATA_DIR);
    }

    public FilePersistentManager(String dataDir) {
        this.dataPath = Paths.get(dataDir).toAbsolutePath();
        this.mapper = new ObjectMapper();
        this.mapper.registerModule(new JavaTimeModule());
        this.mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        ensureDataDir();
    }

    private void ensureDataDir() {
        try {
            Files.createDirectories(dataPath);
        } catch (IOException e) {
            throw new RuntimeException("Could not create data directory: " + dataPath, e);
        }
    }

    private Path usersPath() { return dataPath.resolve(USERS_FILE); }
    private Path lotsPath() { return dataPath.resolve(LOTS_FILE); }
    private Path reservationsPath() { return dataPath.resolve(RESERVATIONS_FILE); }
    private Path paymentsPath() { return dataPath.resolve(PAYMENTS_FILE); }

    private <T> List<T> readList(Path path, TypeReference<List<T>> typeRef) {
        if (!Files.exists(path)) return new ArrayList<>();
        try {
            String json = Files.readString(path);
            if (json.isBlank()) return new ArrayList<>();
            return mapper.readValue(json, typeRef);
        } catch (IOException e) {
            return new ArrayList<>();
        }
    }

    private <T> void writeList(Path path, List<T> list) throws IOException {
        mapper.writerWithDefaultPrettyPrinter().writeValue(path.toFile(), list);
    }

    @Override
    public Optional<User> findUserById(String userId) {
        List<User> list = readList(usersPath(), new TypeReference<>() {});
        return list.stream().filter(u -> userId != null && userId.equals(u.getUserId())).findFirst();
    }

    @Override
    public Optional<User> findUserByEmail(String email) {
        List<User> list = readList(usersPath(), new TypeReference<>() {});
        return list.stream().filter(u -> email != null && email.equals(u.getEmail())).findFirst();
    }

    @Override
    public List<User> findAllUsers() {
        return readList(usersPath(), new TypeReference<>() {});
    }

    @Override
    public void saveUser(User user) {
        List<User> list = findAllUsers();
        list.removeIf(u -> user.getUserId() != null && user.getUserId().equals(u.getUserId()));
        list.add(user);
        try {
            writeList(usersPath(), list);
        } catch (IOException e) {
            throw new RuntimeException("Failed to save user", e);
        }
    }

    @Override
    public Optional<ParkingLot> findParkingLotById(String lotId) {
        List<ParkingLot> list = readList(lotsPath(), new TypeReference<>() {});
        return list.stream().filter(l -> lotId != null && lotId.equals(l.getLotId())).findFirst();
    }

    @Override
    public List<ParkingLot> findAllParkingLots() {
        return readList(lotsPath(), new TypeReference<>() {});
    }

    @Override
    public void saveParkingLot(ParkingLot lot) {
        List<ParkingLot> list = findAllParkingLots();
        list.removeIf(l -> lot.getLotId() != null && lot.getLotId().equals(l.getLotId()));
        list.add(lot);
        try {
            writeList(lotsPath(), list);
        } catch (IOException e) {
            throw new RuntimeException("Failed to save parking lot", e);
        }
    }

    @Override
    public Optional<Reservation> findReservationById(String reservationId) {
        List<Reservation> list = readList(reservationsPath(), new TypeReference<>() {});
        return list.stream().filter(r -> reservationId != null && reservationId.equals(r.getReservationId())).findFirst();
    }

    @Override
    public List<Reservation> findAllReservations() {
        return readList(reservationsPath(), new TypeReference<>() {});
    }

    @Override
    public List<Reservation> findReservationsByUserId(String userId) {
        List<Reservation> list = findAllReservations();
        return list.stream().filter(r -> userId != null && userId.equals(r.getUserId())).toList();
    }

    @Override
    public void saveReservation(Reservation reservation) {
        List<Reservation> list = findAllReservations();
        list.removeIf(r -> reservation.getReservationId() != null && reservation.getReservationId().equals(r.getReservationId()));
        list.add(reservation);
        try {
            writeList(reservationsPath(), list);
        } catch (IOException e) {
            throw new RuntimeException("Failed to save reservation", e);
        }
    }

    @Override
    public Optional<Payment> findPaymentById(String paymentId) {
        List<Payment> list = readList(paymentsPath(), new TypeReference<>() {});
        return list.stream().filter(p -> paymentId != null && paymentId.equals(p.getPaymentId())).findFirst();
    }

    @Override
    public List<Payment> findAllPayments() {
        return readList(paymentsPath(), new TypeReference<>() {});
    }

    @Override
    public void savePayment(Payment payment) {
        List<Payment> list = findAllPayments();
        list.removeIf(p -> payment.getPaymentId() != null && payment.getPaymentId().equals(p.getPaymentId()));
        list.add(payment);
        try {
            writeList(paymentsPath(), list);
        } catch (IOException e) {
            throw new RuntimeException("Failed to save payment", e);
        }
    }

    @Override
    public Optional<PaymentGateway> getDefaultPaymentGateway() {
        PaymentGateway gw = gatewayCache.computeIfAbsent("default", k -> {
            PaymentGateway g = new PaymentGateway();
            g.setGatewayId("GW-001");
            g.setGatewayName("Default Gateway");
            g.setProvider("Stripe");
            g.setStatus("Active");
            g.connect();
            return g;
        });
        return Optional.of(gw);
    }
}
