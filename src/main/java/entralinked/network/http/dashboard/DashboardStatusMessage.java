package entralinked.network.http.dashboard;

public record DashboardStatusMessage(String message, boolean error) {
    
    public DashboardStatusMessage(String message) {
        this(message, false);
    }
}
