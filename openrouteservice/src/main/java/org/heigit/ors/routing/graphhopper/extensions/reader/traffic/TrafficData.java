package org.heigit.ors.routing.graphhopper.extensions.reader.traffic;

import com.graphhopper.util.shapes.Polygon;
import org.apache.log4j.Logger;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;

import java.util.*;

public class TrafficData {

    private double minLat = 180f;
    private double minLon = 180f;
    private double maxLat = -180f;
    private double maxLon = -180f;
    private Geometry bbox;
    private com.vividsolutions.jts.geom.Geometry bboxOld;
    GeometryFactory gf = new GeometryFactory();
    com.vividsolutions.jts.geom.GeometryFactory geometryFactoryOld = new com.vividsolutions.jts.geom.GeometryFactory();


    private HashMap<Integer, TrafficLink> links;
    private HashMap<Integer, TrafficPattern> patterns;

    public TrafficData() {
        this.links = new HashMap<>();
        this.patterns = new HashMap<>();
    }

    /**
     * Add a {@link TrafficLink} to the Traffic data and update the extent.
     *
     * @param link Add a  link to the traffic data.
     */
    public void addLink(TrafficLink link) {
        this.links.put(link.getLinkId(), link);
        // Update bounding box
        double[] bb = link.getBBox();
        if (bb[0] < minLon) minLon = bb[0];
        if (bb[1] > maxLon) maxLon = bb[1];
        if (bb[2] < minLat) minLat = bb[2];
        if (bb[3] > maxLat) maxLat = bb[3];
        bbox = gf.createLineString(new Coordinate[]{new Coordinate(minLon, minLat), new Coordinate(maxLon, maxLat)}).getEnvelope();
        bboxOld = geometryFactoryOld.createLineString(new com.vividsolutions.jts.geom.Coordinate[]{new com.vividsolutions.jts.geom.Coordinate(minLon, minLat), new com.vividsolutions.jts.geom.Coordinate(maxLon, maxLat)}).getEnvelope();
    }

    /**
     * Remove a {@link TrafficLink} by its linkID.
     *
     * @param linkId the ID from the link to be removed.
     */
    public void removeLink(Integer linkId) {
        this.links.remove(linkId);
    }

    /**
     * Remove a collection of {@link Integer} representing link IDs from the link Collection.
     *
     * @param linkIDs the link IDs to be removed.
     */
    public void removeLinkIdCollection(Collection<Integer> linkIDs) {
        for (Integer linkID : linkIDs) {
            this.links.remove(linkID);
        }
    }

    public double[] getBBox() {
        return new double[]{minLon, maxLon, minLat, maxLat};
    }

    public Polygon getBBoxPolygon() {
        return new Polygon(new double[]{minLat, maxLat}, new double[]{minLon, maxLon});
    }

    public Collection<TrafficLink> getLinks() {
        return links.values();
    }

    public boolean hasLink(Integer linkId) {
        return links.get(linkId) != null;
    }

    public TrafficLink getLink(int linkId) {
        return links.get(linkId);
    }

    public void setLink(TrafficLink link) {
        links.put(link.getLinkId(), link);
    }

    public Map<Integer, TrafficPattern> getPatterns() {
        return patterns;
    }

    public TrafficPattern getPattern(Integer patternId) {
        return patterns.get(patternId);
    }

    public void setPattern(TrafficPattern pattern) {
        this.patterns.put(pattern.getPatternId(), pattern);
    }

    public boolean inBoundary(LineString line) {
        return bbox.intersects(line);
    }

    public boolean inBoundary(com.vividsolutions.jts.geom.Coordinate[] coordinates) {
        return bboxOld.contains(geometryFactoryOld.createLineString(coordinates));
    }

}
