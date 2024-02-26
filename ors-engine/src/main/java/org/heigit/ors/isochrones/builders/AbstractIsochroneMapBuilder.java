package org.heigit.ors.isochrones.builders;

import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.routing.util.HikeFlagEncoder;
import com.graphhopper.util.*;
import com.graphhopper.util.shapes.GHPoint3D;
import org.heigit.ors.routing.RouteSearchContext;
import org.heigit.ors.routing.graphhopper.extensions.flagencoders.FootFlagEncoder;
import org.heigit.ors.routing.graphhopper.extensions.flagencoders.ORSAbstractFlagEncoder;
import org.heigit.ors.routing.graphhopper.extensions.flagencoders.WheelchairFlagEncoder;
import org.heigit.ors.routing.graphhopper.extensions.flagencoders.bike.CommonBikeFlagEncoder;
import org.heigit.ors.util.GeomUtility;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;

import java.util.List;

public abstract class AbstractIsochroneMapBuilder implements IsochroneMapBuilder {
    protected static final boolean BUFFERED_OUTPUT = true;
    private static final double MAX_SPLIT_LENGTH = 20000.0;
    protected static final DistanceCalc dcFast = new DistancePlaneProjection();
    protected GeometryFactory geometryFactory;
    protected RouteSearchContext searchContext;
    protected double defaultSmoothingDistance = 0.012;// Use a default length of ~1333m

    public void initialize(RouteSearchContext searchContext) {
        this.searchContext = searchContext;
        this.geometryFactory = new GeometryFactory();
    }

    /**
     * Converts the smoothing factor into a distance (which can be used in algorithms for generating isochrone polygons).
     * The distance value returned is dependent on the radius and smoothing factor.
     *
     * @param smoothingFactor A factor that should be used in the smoothing process. Lower numbers produce a smaller
     *                        distance (and so likely a more detailed polygon)
     * @param maxRadius       The maximum radius of the isochrone (in metres)
     * @return
     */
    protected double convertSmoothingFactorToDistance(float smoothingFactor, double maxRadius) {
        final double MINIMUM_DISTANCE = 0.006;

        if (smoothingFactor == -1) {
            // No user defined smoothing factor, so use defaults

            // For shorter isochrones, we want to use a smaller minimum distance else we get inaccurate results
            if (maxRadius < 5000)
                return MINIMUM_DISTANCE;

            return defaultSmoothingDistance;
        }

        double intervalDegrees = GeomUtility.metresToDegrees(maxRadius);
        double maxLength = (intervalDegrees / 100f) * smoothingFactor;

        if (maxLength < MINIMUM_DISTANCE)
            maxLength = MINIMUM_DISTANCE;
        return maxLength;
    }

    protected void addPoint(List<Coordinate> points, double lon, double lat) {
        Coordinate p = new Coordinate(lon, lat);
        points.add(p);
    }

    protected void addBufferPoints(List<Coordinate> points, double lon0, double lat0, double lon1,
                                 double lat1, boolean addLast, double bufferSize) {
        double dx = (lon0 - lon1);
        double dy = (lat0 - lat1);
        double normLength = Math.sqrt((dx * dx) + (dy * dy));
        double scale = bufferSize / normLength;

        double dx2 = -dy * scale;
        double dy2 = dx * scale;

        addPoint(points, lon0 + dx2, lat0 + dy2);
        addPoint(points, lon0 - dx2, lat0 - dy2);

        // add a middle point if two points are too far from each other
        if (normLength > 2 * bufferSize) {
            addPoint(points, (lon0 + lon1) / 2.0 + dx2, (lat0 + lat1) / 2.0 + dy2);
            addPoint(points, (lon0 + lon1) / 2.0 - dx2, (lat0 + lat1) / 2.0 - dy2);
        }

        if (addLast) {
            addPoint(points, lon1 + dx2, lat1 + dy2);
            addPoint(points, lon1 - dx2, lat1 - dy2);
        }
    }

    protected void addPointsFromEdge(List<Coordinate> points, double isolineCost, float maxCost, float minCost, double bufferSize, double edgeDist, PointList pl) {
        double edgeCost = maxCost - minCost;
        double costPerMeter = edgeCost / edgeDist;
        double distPolyline = 0.0;

        double lat0 = pl.getLat(0);
        double lon0 = pl.getLon(0);
        double lat1;
        double lon1;

        for (int i = 1; i < pl.size(); ++i) {
            lat1 = pl.getLat(i);
            lon1 = pl.getLon(i);

            distPolyline += dcFast.calcDist(lat0, lon0, lat1, lon1);

            if (BUFFERED_OUTPUT) {
                double distCost = minCost + distPolyline * costPerMeter;
                if (distCost >= isolineCost) {
                    double segLength = (1 - (distCost - isolineCost) / edgeCost);
                    double lon2 = lon0 + segLength * (lon1 - lon0);
                    double lat2 = lat0 + segLength * (lat1 - lat0);

                    addBufferPoints(points, lon0, lat0, lon2, lat2, true, bufferSize);

                    break;
                } else {
                    addBufferPoints(points, lon0, lat0, lon1, lat1, false, bufferSize);
                }
            } else {
                addPoint(points, lon0, lat0);
            }

            lat0 = lat1;
            lon0 = lon1;
        }
    }

    /**
     * Splits a line between two points of the distance is longer than a limit
     *
     * @param point0 point0 of line
     * @param point1 point1 of line
     * @param minlim limit above which the edge will be split (in meters)
     * @param maxlim limit above which the edge will NOT be split anymore (in meters)
     */
    private void splitEdge(GHPoint3D point0, GHPoint3D point1, PointList pointList, double minlim, double maxlim) {
        //No need to consider elevation
        double lat0 = point0.getLat();
        double lon0 = point0.getLon();
        double lat1 = point1.getLat();
        double lon1 = point1.getLon();
        double dist = dcFast.calcDist(lat0, lon0, lat1, lon1);
        boolean is3D = pointList.is3D();

        if (dist > minlim && dist < maxlim) {
            int n = (int) Math.ceil(dist / minlim);
            for (int i = 1; i < n; i++) {
                if (is3D)
                    pointList.add(lat0 + i * (lat1 - lat0) / n, lon0 + i * (lon1 - lon0) / n, 0);
                else
                    pointList.add(lat0 + i * (lat1 - lat0) / n, lon0 + i * (lon1 - lon0) / n);
            }
        }
    }

    protected PointList edgeToPoints(EdgeIteratorState iter, double minSplitLength) {
        // always use mode=3, since other ones do not provide correct results
        PointList pl = iter.fetchWayGeometry(FetchMode.ALL);

        double edgeDist = iter.getDistance();
        if (edgeDist > minSplitLength) {
            PointList expandedPoints = new PointList(pl.size(), pl.is3D());
            for (int i = 0; i < pl.size() - 1; i++)
                splitEdge(pl.get(i), pl.get(i + 1), expandedPoints, minSplitLength, MAX_SPLIT_LENGTH);
            pl.add(expandedPoints);
        }

        return pl;
    }

    protected void addCutEdgeGeometry(List<Coordinate> points, double isolineCost, double minSplitLength, EdgeIteratorState iter, float maxCost, float minCost, double bufferSize) {
        PointList pl = edgeToPoints(iter, minSplitLength);
        if (pl.isEmpty()) {
            return;
        }
        addPointsFromEdge(points, isolineCost, maxCost, minCost, bufferSize, iter.getDistance(), pl);
    }

    protected double determineMaxSpeed() {
        FlagEncoder encoder = searchContext.getEncoder();
        double maxSpeed = encoder.getMaxSpeed();

        if (encoder instanceof FootFlagEncoder || encoder instanceof HikeFlagEncoder) {
            // in the GH FootFlagEncoder, the maximum speed is set to 15km/h which is way too high
            maxSpeed = 4;
        }

        if (encoder instanceof WheelchairFlagEncoder) {
            maxSpeed = WheelchairFlagEncoder.MEAN_SPEED;
        }

        return maxSpeed;
    }

    protected double determineMeanSpeed(double maxSpeed) {
        double meanSpeed = maxSpeed;
        FlagEncoder encoder = searchContext.getEncoder();

        if (encoder instanceof ORSAbstractFlagEncoder flagEncoder) {
            meanSpeed = flagEncoder.getMeanSpeed();
        }

        if (encoder instanceof CommonBikeFlagEncoder flagEncoder) {
            meanSpeed =  flagEncoder.getMeanSpeed();
        }

        return meanSpeed;
    }
}
