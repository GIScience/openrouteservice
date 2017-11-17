package heigit.ors.services.routing.requestprocessors.gpx.beans;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;

public final class Route implements Iterable<WayPoint>, Serializable {
    private  String name;
    private  String comment;
    private  String description;
    private  String source;
    private  String type;
    private  List<WayPoint> wayPointList;


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<WayPoint> getWayPointList() {
        return null;
    }

    public void setWayPointList(List<WayPoint> wayPointList) {
        this.wayPointList = wayPointList;
    }

    @Override
    public Iterator<WayPoint> iterator() {
        return null;
    }
}
