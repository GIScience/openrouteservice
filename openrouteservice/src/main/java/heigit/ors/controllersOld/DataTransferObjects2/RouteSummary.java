package heigit.ors.controllersOld.DataTransferObjects2;

public class RouteSummary {
    private double distance;
    private double duration;

    public RouteSummary(double distance, double duration) {
        this.distance = distance;
        this.duration = duration;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public double getDistance() {
        return this.distance;
    }

    public void setDuration(double duration) {
        this.duration = duration;
    }

    public double getDuration() {
        return this.duration;
    }
}
