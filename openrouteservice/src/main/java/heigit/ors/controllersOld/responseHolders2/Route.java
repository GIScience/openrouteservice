package heigit.ors.controllersOld.responseHolders2;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;

import java.util.ArrayList;
import java.util.Arrays;

public class Route {
    private Coordinate startPoint;
    private Coordinate destinationPoint;
    private Coordinate[] viaPoints;

    private LineString routeGeom;

    public Route(Coordinate startPoint, Coordinate endPoint) {
        this(startPoint, endPoint, new Coordinate[] {});
    }

    public Route(Coordinate startPoint, Coordinate endPoint, Coordinate[] viaPoints) {
        this.startPoint = startPoint;
        this.destinationPoint = endPoint;
        this.viaPoints = viaPoints;
    }

    public Route(ArrayList<Coordinate> coordinates) {
        if(coordinates.size() >= 2) {
            this.startPoint = coordinates.get(0);
            this.destinationPoint = coordinates.get(coordinates.size() - 1);
            this.viaPoints = coordinates.subList(1, coordinates.size() - 2).toArray(new Coordinate[coordinates.size()-2]);
        }
    }

    public void generateRoute() {
        ArrayList<Coordinate> lsCoords = new ArrayList<>();
        lsCoords.add(startPoint);

        lsCoords.addAll(Arrays.asList(viaPoints));

        lsCoords.add(destinationPoint);

        GeometryFactory gf = new GeometryFactory();
        routeGeom = gf.createLineString(lsCoords.toArray(new Coordinate[lsCoords.size()]));
    }

    public Coordinate getStartPoint() {
        return startPoint;
    }

    public Coordinate getDestinationPoint() {
        return destinationPoint;
    }

    public Coordinate[] getViaPoints() {
        return viaPoints;
    }

    public LineString getRouteGeom() {
        return routeGeom;
    }
}
