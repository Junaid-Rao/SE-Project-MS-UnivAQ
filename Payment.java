public class Payment { 

  

    private String paymentId; 

    private String paymentStatus;     // Paid, Pending, Failed 

    private String paymentMethod;     // Card, Cash, Wallet 

    private double amount; 

  

    public Payment(String paymentId, String paymentStatus, 

                   String paymentMethod, double amount) { 

        this.paymentId = paymentId; 

        this.paymentStatus = paymentStatus; 

        this.paymentMethod = paymentMethod; 

        this.amount = amount; 

    } 

  

    public void processPayment() { 

        System.out.println("Processing payment of " + amount); 

        this.paymentStatus = "Paid"; 

    } 

  

    public String getPaymentId() { 

        return paymentId; 

    } 

  

    public String getPaymentStatus() { 

        return paymentStatus; 

    } 

  

    public String getPaymentMethod() { 

        return paymentMethod; 

    } 

  

    public double getAmount() { 

        return amount; 

    } 

} 
