package smartparking.model;

/**
 * Domain model: Navigation (optional association with User).
 */
public class Navigation {
    private String navigationId;
    private String routeDetails;
    private int estimatedTime;  // minutes
    private String mapLink;

    public String getNavigationId() { return navigationId; }
    public void setNavigationId(String navigationId) { this.navigationId = navigationId; }
    public String getRouteDetails() { return routeDetails; }
    public void setRouteDetails(String routeDetails) { this.routeDetails = routeDetails; }
    public int getEstimatedTime() { return estimatedTime; }
    public void setEstimatedTime(int estimatedTime) { this.estimatedTime = estimatedTime; }
    public String getMapLink() { return mapLink; }
    public void setMapLink(String mapLink) { this.mapLink = mapLink; }

    public void generateRoute(String address) {
        this.routeDetails = "Route to " + (address != null ? address : "");
        this.mapLink = "https://maps.example.com?q=" + (address != null ? address.replace(" ", "+") : "");
    }

    public void updateRoute() {
        // Placeholder for route refresh
    }

    public void displayRoute() {
        // Placeholder for UI display
    }

    @Override
    public String toString() {
        return String.format("Navigation{id='%s', estimatedTime=%d min, mapLink='%s'}",
                navigationId, estimatedTime, mapLink);
    }
}
