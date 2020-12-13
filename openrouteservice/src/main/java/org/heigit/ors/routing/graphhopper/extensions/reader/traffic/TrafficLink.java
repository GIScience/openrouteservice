package org.heigit.ors.routing.graphhopper.extensions.reader.traffic;

import com.graphhopper.util.DistanceCalc;
import com.graphhopper.util.DistanceCalcEarth;
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
    private double coordinateFirstY;
    private double coordinateFirstX;
    private double coordinateLastY;
    private double coordinateLastX;

    private int linkId;
    private double linkLength;

    private LineString linkGeometry;
    private TrafficLinkMetadata trafficLinkMetadata;
    private EnumMap<TrafficEnums.WeekDay, Integer> trafficPatternIdsFrom;
    private EnumMap<TrafficEnums.WeekDay, Integer> trafficPatternIdsTo;

    /**
     * Construct a TrafficLink object used for processing the link traffic data.
     *
     * @param linkId       Link ID of the link
     * @param linkGeometry Geometry representing the link
     * @param properties   Properties of the link
     * @param distanceCalcEarth   Initialized {@link DistanceCalcEarth} object that can easily be reused to calculate the lengths.
     * @throws InvalidObjectException Provides detailed information when a geometry was invalid.
     */
    public TrafficLink(int linkId, Geometry linkGeometry, Collection<Property> properties, DistanceCalcEarth distanceCalcEarth) throws InvalidObjectException {
        this.linkId = linkId;

        this.trafficPatternIdsFrom = new EnumMap<>(TrafficEnums.WeekDay.class);
        this.trafficPatternIdsTo = new EnumMap<>(TrafficEnums.WeekDay.class);

        this.setLinkGeometry(linkGeometry);
        this.setLinkLength(linkGeometry, distanceCalcEarth);

        trafficLinkMetadata = new TrafficLinkMetadata(properties);

    }

    private void setLinkLength(Geometry linkGeometry, DistanceCalcEarth dc) {
        double temporaryLength = 0;

        if (this.getLinkGeometry() != null) {
            LineString ls = this.getLinkGeometry();
            int nPoints = ls.getNumPoints();

            if (nPoints > 1) {
                Coordinate c = ls.getCoordinateN(0);
                double x0 = c.x;
                double y0 = c.y;
                for (int i = 1; i < ls.getNumPoints(); i++) {
                    c = ls.getCoordinateN(i);

                    temporaryLength += dc.calcDist(y0, x0, c.y, c.x);
                    x0 = c.x;
                    y0 = c.y;
                }
            }
        }
        this.linkLength = temporaryLength;
    }

    public double getLinkLength() {
        return this.linkLength;
    }

    public int getLinkId() {
        return this.linkId;
    }

    private void setLinkGeometry(Geometry linkGeometry) throws InvalidObjectException {
        GeometryFactory gf = new GeometryFactory();
        if (linkGeometry.getGeometryType().equals("LineString")) {
            this.linkGeometry = gf.createLineString(linkGeometry.getCoordinates());
            this.coordinateFirstX = this.linkGeometry.getCoordinateN(0).x;
            this.coordinateFirstY = this.linkGeometry.getCoordinateN(0).y;
            this.coordinateLastX = this.linkGeometry.getCoordinateN(this.linkGeometry.getCoordinates().length - 1).x;
            this.coordinateLastY = this.linkGeometry.getCoordinateN(this.linkGeometry.getCoordinates().length - 1).y;
        } else {
            LOGGER.error("Invalid geometry - " + linkGeometry.getGeometryType());
            throw new InvalidObjectException("Invalid geometry for linkId " + linkId);
        }
    }

    public LineString getLinkGeometry() {
        return this.linkGeometry;
    }

    public void setTrafficPatternId(TrafficEnums.TravelDirection travelDirection, TrafficEnums.WeekDay weekDay, Integer trafficPatternId) {
        if (travelDirection == TrafficEnums.TravelDirection.TO) {
            this.trafficPatternIdsTo.put(weekDay, trafficPatternId);
        } else {
            this.trafficPatternIdsFrom.put(weekDay, trafficPatternId);
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
        return trafficLinkMetadata.functionalClass() != TrafficEnums.FunctionalClass.CLASS5;
    }

    public boolean isBothDirections() {
        return trafficLinkMetadata.getTravelDirection() == TrafficEnums.LinkTravelDirection.BOTH;
    }

    public int getFunctionalClass() {
        return this.trafficLinkMetadata.getFunctionalClassWithRamp();
    }

    public Geometry getToGeometry() {
        if (this.coordinateFirstY < this.coordinateLastY) {
            // First is Reference if its latitude is lower. Most common case!
            return this.linkGeometry.reverse();
        } else if (this.coordinateFirstY > this.coordinateLastY) {
            // Last is Reference if its latitude is lower
            return this.linkGeometry;
        } else if (this.coordinateFirstX < this.coordinateLastX) {
            // First is Reference if latitudes are equal but its longitude is lower
            return this.linkGeometry.reverse();
        } else if (this.coordinateFirstX > this.coordinateLastX) {
            // Last is Reference if latitudes are equal but its longitude is lower
            return this.linkGeometry;
        } else {
            // Teardrop nodes with same Coords. This shouldn't happen with roads from Here!
            // TODO deside if return null or return the original coordinate order should be returned
            return this.linkGeometry;
        }
    }

    public Geometry getFromGeometry() {
        if (this.coordinateFirstY < this.coordinateLastY) {
            // First coordinate is the reference if its latitude is lower. Most common case!
            return this.linkGeometry;
        } else if (this.coordinateFirstY > this.coordinateLastY) {
            // Last  coordinate is Reference if its latitude is lower.
            return this.linkGeometry.reverse();
        } else if (this.coordinateFirstX < this.coordinateLastX) {
            // First coordinate is the reference if latitudes are equal but its longitude is lower.
            // This represents horizontal lines >------>
            return this.linkGeometry;
        } else if (this.coordinateFirstX > this.coordinateLastX) {
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
}
