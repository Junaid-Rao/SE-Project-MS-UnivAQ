public class ParkingSlot { 

  

    private String location; 

    private double pricePerHour; 

    private String slotType; 

  

    public ParkingSlot(String location, double pricePerHour, String slotType) { 

        this.location = location; 

        this.pricePerHour = pricePerHour; 

        this.slotType = slotType; 

    } 

  

    public String getLocation() { 

        return location; 

    } 

  

    public void setLocation(String location) { 

        this.location = location; 

    } 

  

    public double getPricePerHour() { 

        return pricePerHour; 

    } 

  

    public void setPricePerHour(double pricePerHour) { 

        this.pricePerHour = pricePerHour; 

    } 

  

    public String getSlotType() { 

        return slotType; 

    } 

  

    public void setSlotType(String slotType) { 

        this.slotType = slotType; 

    } 

} 
