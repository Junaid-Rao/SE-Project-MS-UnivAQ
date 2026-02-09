import java.time.LocalDateTime; 

  

public class Reservation { 

  

    private String reservationId; 

    private LocalDateTime startTime; 

    private LocalDateTime endTime; 

    private String reservationStatus; 

  

    // Relationships 

    private User user;                // 1 user 

    private ParkingSlot parkingSlot;  // 1 slot 

    private Payment payment;          // 1 payment 

    private Navigation navigation;    // 0..1 navigation 

  

    public Reservation(String reservationId, 

                       LocalDateTime startTime, 

                       LocalDateTime endTime, 

                       User user, 

                       ParkingSlot parkingSlot) { 

  

        this.reservationId = reservationId; 

        this.startTime = startTime; 

        this.endTime = endTime; 

        this.user = user; 

        this.parkingSlot = parkingSlot; 

        this.reservationStatus = "Created"; 

    } 

  

    public void attachPayment(Payment payment) { 

        this.payment = payment; 

    } 

  

    public void attachNavigation(Navigation navigation) { 

        this.navigation = navigation; 

    } 

  

    public void confirmReservation() { 

        reservationStatus = "Confirmed"; 

    } 

  

    public String getReservationId() { 

        return reservationId; 

    } 

  

    public String getReservationStatus() { 

        return reservationStatus; 

    } 

  

    public User getUser() { 

        return user; 

    } 

  

    public ParkingSlot getParkingSlot() { 

        return parkingSlot; 

    } 

  

    public Payment getPayment() { 

        return payment; 

    } 

  

    public Navigation getNavigation() { 

        return navigation; 

    } 

} 
