package heigit.ors.controllersOld.DataTransferObjects2;

import com.fasterxml.jackson.annotation.JsonProperty;

public class RouteRequestOptionsDTO {

    public enum AvoidFeatures {
        @JsonProperty("tollways")
        TOLLWAYS,
        @JsonProperty("ferries")
        FERRIES
    }

    @JsonProperty("maximum_speed")
    private short maximumSpeed;
    @JsonProperty("avoid_features")
    private AvoidFeatures[] avoidFeatures;

    public short getMaximumSpeed() {
        return this.maximumSpeed;
    }

    public void setMaximumSpeed(short maximumSpeed) {
        this.maximumSpeed = maximumSpeed;
    }

    public AvoidFeatures[] getAvoidFeatures() {
        return this.avoidFeatures;
    }

    public void setAvoidFeatures(AvoidFeatures[] avoidFeatures) {
        this.avoidFeatures = avoidFeatures;
    }
}
