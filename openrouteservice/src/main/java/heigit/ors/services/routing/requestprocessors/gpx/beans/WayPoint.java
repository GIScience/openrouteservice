package heigit.ors.services.routing.requestprocessors.gpx.beans;

import java.math.BigDecimal;

public class WayPoint{
    private BigDecimal lon;
    private BigDecimal lat;
    private Double ele;
    private double distance;
    private double duration;
    private String instruction;
    private String name;
    private int type;
    private String wayPoints;
    private String comment;

    public void setLon(BigDecimal lon) {
        this.lon = lon;
    }

    public BigDecimal getLon() {
        return lon;
    }

    public void setLat(BigDecimal lat) {
        this.lat = lat;
    }

    public BigDecimal getLat() {
        return lat;
    }

    public void setEle(Double ele) {
        this.ele = ele;
    }

    public Double getEle() {
        return ele;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public double getDistance() {
        return distance;
    }

    public void setDuration(double duration) {
        this.duration = duration;
    }

    public double getDuration() {
        return duration;
    }

    public void setInstruction(String instruction) {
        this.instruction = instruction;
    }

    public String getInstruction() {
        return instruction;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }

    public void setWayPointIdentifier(String wayPoints) {
        this.wayPoints = wayPoints;
    }

    public String getWayPointIdentifier() {
        return wayPoints;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getComment() {
        return comment;
    }
}
