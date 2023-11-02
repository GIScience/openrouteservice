package org.heigit.ors.isochrones.builders;

import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.routing.util.HikeFlagEncoder;
import com.graphhopper.util.DistanceCalc;
import com.graphhopper.util.DistancePlaneProjection;
import org.heigit.ors.isochrones.IsochroneMap;
import org.heigit.ors.isochrones.IsochroneSearchParameters;
import org.heigit.ors.isochrones.builders.concaveballs.PointItemVisitor;
import org.heigit.ors.routing.RouteSearchContext;
import org.heigit.ors.routing.graphhopper.extensions.flagencoders.FootFlagEncoder;
import org.heigit.ors.routing.graphhopper.extensions.flagencoders.ORSAbstractFlagEncoder;
import org.heigit.ors.routing.graphhopper.extensions.flagencoders.WheelchairFlagEncoder;
import org.heigit.ors.routing.graphhopper.extensions.flagencoders.bike.CommonBikeFlagEncoder;
import org.heigit.ors.util.GeomUtility;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.index.quadtree.Quadtree;

import java.util.List;
import java.util.TreeSet;

public class AbstractIsochroneMapBuilder implements IsochroneMapBuilder {
    protected static final boolean BUFFERED_OUTPUT = true;
    protected static final DistanceCalc dcFast = new DistancePlaneProjection();
    protected final Envelope searchEnv = new Envelope();
    protected final GeometryFactory geometryFactory = new GeometryFactory();
    protected RouteSearchContext searchContext;
    protected PointItemVisitor visitor = null;
    protected TreeSet<Coordinate> treeSet;
    protected double searchWidth = 0.0007;
    protected double pointWidth = 0.0005;
    protected double visitorThreshold = 0.0013;
    protected double defaultSmoothingDistance = 0.012;// Use a default length of ~1333m

    public void initialize(RouteSearchContext searchContext) {
        this.searchContext = searchContext;
    }

    @Override
    public IsochroneMap compute(IsochroneSearchParameters parameters) throws Exception {
        throw new UnsupportedOperationException("Not implemented");
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
        double minimumDistance = 0.006;

        if (smoothingFactor == -1) {
            // No user defined smoothing factor, so use defaults

            // For shorter isochrones, we want to use a smaller minimum distance else we get inaccurate results
            if (maxRadius < 5000)
                return minimumDistance;

            return defaultSmoothingDistance;
        }

        double intervalDegrees = GeomUtility.metresToDegrees(maxRadius);
        double maxLength = (intervalDegrees / 100f) * smoothingFactor;

        if (maxLength < minimumDistance)
            maxLength = minimumDistance;
        return maxLength;
    }

    protected Boolean addPoint(List<Coordinate> points, Quadtree tree, double lon, double lat, boolean checkNeighbours) {
        if (checkNeighbours) {
            visitor.setPoint(lon, lat);
            searchEnv.init(lon - searchWidth, lon + searchWidth, lat - searchWidth, lat + searchWidth);
            tree.query(searchEnv, visitor);
            if (!visitor.isNeighbourFound()) {
                Coordinate p = new Coordinate(lon, lat);

                if (!treeSet.contains(p)) {
                    Envelope env = new Envelope(lon - pointWidth, lon + pointWidth, lat - pointWidth, lat + pointWidth);
                    tree.insert(env, p);
                    points.add(p);
                    treeSet.add(p);

                    return true;
                }
            }
        } else {
            Coordinate p = new Coordinate(lon, lat);
            if (!treeSet.contains(p)) {
                Envelope env = new Envelope(lon - pointWidth, lon + pointWidth, lat - pointWidth, lat + pointWidth);
                tree.insert(env, p);
                points.add(p);
                treeSet.add(p);

                return true;
            }
        }

        return false;
    }

    protected void addBufferPoints(List<Coordinate> points, Quadtree tree, double lon0, double lat0, double lon1,
                                   double lat1, boolean addLast, boolean checkNeighbours, double bufferSize) {
        double dx = (lon0 - lon1);
        double dy = (lat0 - lat1);
        double normLength = Math.sqrt((dx * dx) + (dy * dy));
        double scale = bufferSize / normLength;

        double dx2 = -dy * scale;
        double dy2 = dx * scale;

        addPoint(points, tree, lon0 + dx2, lat0 + dy2, checkNeighbours);
        addPoint(points, tree, lon0 - dx2, lat0 - dy2, checkNeighbours);

        // add a middle point if two points are too far from each other
        if (normLength > 2 * bufferSize) {
            addPoint(points, tree, (lon0 + lon1) / 2.0 + dx2, (lat0 + lat1) / 2.0 + dy2, checkNeighbours);
            addPoint(points, tree, (lon0 + lon1) / 2.0 - dx2, (lat0 + lat1) / 2.0 - dy2, checkNeighbours);
        }

        if (addLast) {
            addPoint(points, tree, lon1 + dx2, lat1 + dy2, checkNeighbours);
            addPoint(points, tree, lon1 - dx2, lat1 - dy2, checkNeighbours);
        }
    }

    protected double determineMeanSpeed(double maxSpeed) {
        FlagEncoder encoder = searchContext.getEncoder();
        if (encoder instanceof ORSAbstractFlagEncoder flagEncoder) {
            return flagEncoder.getMeanSpeed();
        }
        if (encoder instanceof CommonBikeFlagEncoder flagEncoder) {
           return flagEncoder.getMeanSpeed();
        }
        return maxSpeed;
    }

    protected double determineMaxSpeed() {
        FlagEncoder encoder = searchContext.getEncoder();
        if (encoder instanceof FootFlagEncoder || encoder instanceof HikeFlagEncoder) {
            // in the GH FootFlagEncoder, the maximum speed is set to 15km/h which is way too high
            return 4;
        }
        if (encoder instanceof WheelchairFlagEncoder) {
            return WheelchairFlagEncoder.MEAN_SPEED;
        }
        return encoder.getMaxSpeed();
    }
}
