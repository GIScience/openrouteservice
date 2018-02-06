package heigit.ors.util.GlobalResponseProcessor.gpxUtil.beans;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

/**
 * {@link WptTypeExtensions} represents a class to process the Extensions for {@link WptType}
 * Can be manually extended if needed
 */

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = {
        "distance",
        "duration",
        "type",
        "step"
        // always add new variables here! and below
})

public class WptTypeExtensions extends ExtensionsType {
    protected double distance;
    protected double duration;
    protected int type;
    protected int step;

    /**
     * Gets the value of the distance property
     *
     * @return distance as double
     */
    public double getDistance() {
        return distance;
    }

    /**
     * Sets the value of the distance property
     *
     * @param distance needs a double as input
     */
    public void setDistance(double distance) {
        this.distance = distance;
    }

    /**
     * Gets the value of the duration property
     *
     * @return duration as double
     */
    public double getDuration() {
        return duration;
    }

    /**
     * Sets the value of the duration property
     *
     * @param duration needs a double as input
     */
    public void setDuration(double duration) {
        this.duration = duration;
    }

    /**
     * Gets the value of the type property
     *
     * @return type as int
     */
    public int getType() {
        return type;
    }

    /**
     * Sets the value of the type property
     *
     * @param type needs an int as input
     */
    public void setType(int type) {
        this.type = type;
    }

    /**
     * Gets the value of the step property
     *
     * @return step as int
     */
    public int getStep() {
        return step;
    }

    /**
     * Sets the value of the step property
     *
     * @param step needs an int as input
     */
    public void setStep(int step) {
        this.step = step;
    }
}
