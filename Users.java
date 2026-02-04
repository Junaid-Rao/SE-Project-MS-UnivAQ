import java.util.ArrayList; 

import java.util.List; 

  

public class User { 

  

    private String userId; 

    private String name; 

    private String email; 

    private String phone; 

  

    // A user can make multiple reservations 

    private List<Reservation> reservations; 

  

    public User(String userId, String name, String email, String phone) { 

        this.userId = userId; 

        this.name = name; 

        this.email = email; 

        this.phone = phone; 

        this.reservations = new ArrayList<>(); 

    } 

  

    // User makes a reservation 

    public void makeReservation(Reservation reservation) { 

        reservations.add(reservation); 

    } 

  

    // Getters & Setters 

    public String getUserId() { 

        return userId; 

    } 

  

    public void setUserId(String userId) { 

        this.userId = userId; 

    } 

  

    public String getName() { 

        return name; 

    } 

  

    public void setName(String name) { 

        this.name = name; 

    } 

  

    public String getEmail() { 

        return email; 

    } 

  

    public void setEmail(String email) { 

        this.email = email; 

    } 

  

    public String getPhone() { 

        return phone; 

    } 

  

    public void setPhone(String phone) { 

        this.phone = phone; 

    } 

  

    public List<Reservation> getReservations() { 

        return reservations; 

    } 

} 
