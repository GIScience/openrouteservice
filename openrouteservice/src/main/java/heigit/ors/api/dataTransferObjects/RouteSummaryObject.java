package heigit.ors.api.dataTransferObjects;

public class RouteSummaryObject {
    private double distance;
    private double duration;

    public RouteSummaryObject() {

    }

    public RouteSummaryObject(double distance, double duration) {
        this.distance = distance;
        this.duration = duration;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public double getDuration() {
        return duration;
    }

    public void setDuration(double duration) {
        this.duration = duration;
    }
}
