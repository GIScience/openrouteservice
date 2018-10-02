package heigit.ors.api.responses.routing.JSONRouteResponseObjects;

import com.fasterxml.jackson.annotation.JsonProperty;

public class JSONExtraSummary {
    private double value;
    private double distance;
    private double amount;

    public JSONExtraSummary(double value, double distance, double amount) {
        this.value = value;
        this.distance = distance;
        this.amount = amount;
    }

    @JsonProperty("value")
    public double getValue() {
        return value;
    }

    @JsonProperty("distance")
    public double getDistance() {
        return distance;
    }

    @JsonProperty("amount")
    public double getAmount() {
        return amount;
    }
}
