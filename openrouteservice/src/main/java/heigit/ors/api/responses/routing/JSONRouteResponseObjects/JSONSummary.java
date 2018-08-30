package heigit.ors.api.responses.routing.JSONRouteResponseObjects;

public class JSONSummary {
    private Double distance;
    private Double duration;

    public JSONSummary(Double distance, Double duration) {
        this.distance = distance;
        this.duration = duration;
    }

    public Double getDistance() {
        return distance;
    }

    public Double getDuration() {
        return duration;
    }
}