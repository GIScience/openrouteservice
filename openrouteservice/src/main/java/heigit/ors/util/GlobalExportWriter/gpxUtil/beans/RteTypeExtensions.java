package heigit.ors.util.GlobalExportWriter.gpxUtil.beans;


import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

/**
 * {@link RteTypeExtensions} represents a class that extends {@link RteType}
 * Can be manually extended
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = {
        "distance",
        "duration",
        "distanceActual",
        "ascent",
        "descent",
        "avgSpeed"
        // always add new variables here! and below
})

public class RteTypeExtensions extends ExtensionsType {

    private double distance;
    private double duration;
    private double distanceActual;
    private double ascent;
    private double descent;
    private double avgSpeed;


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
     * Gets the value of the DistanceActual property
     *
     * @return distanceActual as double
     */
    public double getDistanceActual() {
        return distanceActual;
    }

    /**
     * Sets the value of the DistanceActual property
     *
     * @param distanceActual needs a double as input
     */
    public void setDistanceActual(double distanceActual) {
        this.distanceActual = distanceActual;
    }


    /**
     * Gets the value of the ascent property
     *
     * @return ascent as double
     */
    public double getAscent() {
        return ascent;
    }

    /**
     * Sets the value of the ascent property
     *
     * @param ascent needs a double as input
     */
    public void setAscent(double ascent) {
        this.ascent = ascent;
    }

    /**
     * Gets the value of the descent property
     *
     * @return descent as double
     */
    public double getDescent() {
        return descent;
    }

    /**
     * Sets the value of the descent property
     *
     * @param descent needs a double as input
     */
    public void setDescent(double descent) {
        this.descent = descent;
    }

    /**
     * Gets the value of the avgspeed property
     *
     * @return avgspeed as double
     */
    public double getAvgSpeed() {
        return avgSpeed;
    }

    /**
     * Sets the value of the avgspeed property
     *
     * @param avgSpeed needs a double as input
     */
    public void setAvgSpeed(double avgSpeed) {
        this.avgSpeed = avgSpeed;
    }
}
