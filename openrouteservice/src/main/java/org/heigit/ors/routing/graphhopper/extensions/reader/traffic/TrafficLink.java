package org.heigit.ors.routing.graphhopper.extensions.reader.traffic;

import com.graphhopper.util.DistanceCalc;
import org.apache.log4j.Logger;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.opengis.feature.Property;

import java.io.InvalidObjectException;
import java.util.Collection;
import java.util.EnumMap;
import java.util.Map;

public class TrafficLink {
    private static final Logger LOGGER = Logger.getLogger(TrafficLink.class);

    private int linkId;
    private double minLat = 180f;
    private double minLon = 180f;
    private double maxLat = -180f;
    private double maxLon = -180f;

    private LineString linkGeometry;
    private TrafficLinkMetadata trafficLinkMetadata;
    private Map<TrafficEnums.WeekDay, Integer> trafficPatternIdsFrom;
    private Map<TrafficEnums.WeekDay, Integer> trafficPatternIdsTo;

    /**
     * Construct a TrafficLink object used for processing the link traffic data.
     *
     * @param linkId       Link ID of the link
     * @param linkGeometry Geometry representing the link
     * @param properties   Properties of the link
     * @throws InvalidObjectException Provides detailed information when a geometry was invalid.
     */
    public TrafficLink(int linkId, Geometry linkGeometry, Collection<Property> properties) throws InvalidObjectException {
        this.linkId = linkId;
        GeometryFactory gf = new GeometryFactory();

        this.trafficPatternIdsFrom = new EnumMap<>(TrafficEnums.WeekDay.class);
        this.trafficPatternIdsTo = new EnumMap<>(TrafficEnums.WeekDay.class);

        if (linkGeometry.getGeometryType().equals("LineString"))
            this.linkGeometry = gf.createLineString(linkGeometry.getCoordinates());
        else {
            LOGGER.error("Invalid geometry - " + linkGeometry.getGeometryType());
            throw new InvalidObjectException("Invalid geometry for linkId " + linkId);
        }

        Geometry bbox = linkGeometry.getEnvelope();
        for (Coordinate c : bbox.getCoordinates()) {
            if (c.x < minLon) minLon = c.x;
            if (c.x > maxLon) maxLon = c.x;
            if (c.y < minLat) minLat = c.y;
            if (c.y > maxLat) maxLat = c.y;
        }
        trafficLinkMetadata = new TrafficLinkMetadata(properties);

    }

    public double[] getBBox() {
        return new double[]{minLon, maxLon, minLat, maxLat};
    }

    public int getLinkId() {
        return linkId;
    }

    public LineString getLinkGeometry() {
        return linkGeometry;
    }

    public void setLinkGeometry(LineString linkGeometry) {
        this.linkGeometry = linkGeometry;
    }

    public void setTrafficPatternId(TrafficEnums.TravelDirection travelDirection, TrafficEnums.WeekDay weekDay, Integer trafficPatternId) {
        if (travelDirection == TrafficEnums.TravelDirection.TO) {
            this.trafficPatternIdsTo.put(weekDay, trafficPatternId);
        } else {
            this.trafficPatternIdsFrom.put(weekDay, trafficPatternId);
        }
    }

    public Integer getTrafficPatternId(TrafficEnums.TravelDirection travelDirection, TrafficEnums.WeekDay weekDay) {
        if (travelDirection == TrafficEnums.TravelDirection.TO) {
            return this.trafficPatternIdsTo.get(weekDay);
        } else {
            return this.trafficPatternIdsFrom.get(weekDay);
        }
    }

    public Map<TrafficEnums.WeekDay, Integer> getTrafficPatternIds(TrafficEnums.TravelDirection travelDirection) {
        if (travelDirection == TrafficEnums.TravelDirection.TO) {
            return this.trafficPatternIdsTo;
        } else {
            return this.trafficPatternIdsFrom;
        }
    }

    public boolean isPotentialTrafficSegment() {
        if (trafficPatternIdsTo.isEmpty() && trafficPatternIdsFrom.isEmpty()) return false;
        if (trafficLinkMetadata.isFerry()) return false;
        if (trafficLinkMetadata.isRoundAbout()) return false;
        if (trafficLinkMetadata.functionalClass() == TrafficEnums.FunctionalClass.CLASS5) return false;
        return true;
    }

    public TrafficEnums.FunctionalClass getWayType() {
        return trafficLinkMetadata.functionalClass();
    }

    public boolean isBothDirections() {
        return trafficLinkMetadata.getTravelDirection() == TrafficEnums.LinkTravelDirection.BOTH;
    }

    public double getLength(DistanceCalc dc) {
        double res = 0;

        if (this.getLinkGeometry() != null) {
            LineString ls = this.getLinkGeometry();
            int nPoints = ls.getNumPoints();

            if (nPoints > 1) {
                Coordinate c = ls.getCoordinateN(0);
                double x0 = c.x;
                double y0 = c.y;
                for (int i = 1; i < ls.getNumPoints(); i++) {
                    c = ls.getCoordinateN(i);

                    res += dc.calcDist(y0, x0, c.y, c.x);
                    x0 = c.x;
                    y0 = c.y;
                }
            }
        }
        return res;
    }

    public int getFunctionalClass() {
        return this.trafficLinkMetadata.getFunctionalClassWithRamp();
    }

    public Geometry getToGeometry() {
        Coordinate coordinateFirst = this.linkGeometry.getCoordinateN(0);
        Coordinate coordinateLast = this.linkGeometry.getCoordinateN(this.linkGeometry.getCoordinates().length - 1);
        if (coordinateFirst.y < coordinateLast.y) {
            // First is Reference if its latitude is lower. Most common case!
            return this.linkGeometry.reverse();
        } else if (coordinateFirst.y > coordinateLast.y) {
            // Last is Reference if its latitude is lower
            return this.linkGeometry;
        } else if (coordinateFirst.x < coordinateLast.x) {
            // First is Reference if latitudes are equal but its longitude is lower
            return this.linkGeometry.reverse();
        } else if (coordinateFirst.x > coordinateLast.x) {
            // Last is Reference if latitudes are equal but its longitude is lower
            return this.linkGeometry;
        } else {
            // Teardrop nodes with same Coords. This shouldn't happen with roads from Here!
            // TODO deside if return null or return the original coordinate order should be returned
            return this.linkGeometry;
        }
    }

    public Geometry getFromGeometry() {
        Coordinate coordinateFirst = this.linkGeometry.getCoordinateN(0);
        Coordinate coordinateLast = this.linkGeometry.getCoordinateN(this.linkGeometry.getCoordinates().length - 1);
        if (coordinateFirst.y < coordinateLast.y) {
            // First coordinate is the reference if its latitude is lower. Most common case!
            return this.linkGeometry;
        } else if (coordinateFirst.y > coordinateLast.y) {
            // Last  coordinate is Reference if its latitude is lower.
            return this.linkGeometry.reverse();
        } else if (coordinateFirst.x < coordinateLast.x) {
            // First coordinate is the reference if latitudes are equal but its longitude is lower.
            // This represents horizontal lines >------>
            return this.linkGeometry;
        } else if (coordinateFirst.x > coordinateLast.x) {
            // First coordinate is the reference if latitudes are equal but its longitude is lower.
            return this.linkGeometry.reverse();
        } else {
            // Teardrop nodes with same Coords. This shouldn't happen with roads from Here!
            return null;
        }
    }

    public boolean isOnlyFromDirection() {
        return trafficLinkMetadata.getTravelDirection() == TrafficEnums.LinkTravelDirection.FROM;
    }

    public boolean isOnlyToDirection() {
        return trafficLinkMetadata.getTravelDirection() == TrafficEnums.LinkTravelDirection.TO;
    }
}
