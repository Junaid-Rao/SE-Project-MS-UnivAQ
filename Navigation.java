public class Navigation { 

  

    private String navigationId; 

    private String routeDetails; 

  

    public Navigation(String navigationId, String routeDetails) { 

        this.navigationId = navigationId; 

        this.routeDetails = routeDetails; 

    } 

  

    public String getNavigationId() { 

        return navigationId; 

    } 

  

    public String getRouteDetails() { 

        return routeDetails; 

    } 

  

    public void displayRoute() { 

        System.out.println("Route: " + routeDetails); 

    } 

} 
