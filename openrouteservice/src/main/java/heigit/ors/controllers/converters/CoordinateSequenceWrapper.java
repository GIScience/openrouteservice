package heigit.ors.controllers.converters;

import com.vividsolutions.jts.geom.Coordinate;

import java.util.List;

public class CoordinateSequenceWrapper {
    private List<Coordinate> coordinates;

    public CoordinateSequenceWrapper(List<Coordinate> coordinates) {
        this.coordinates = coordinates;
    }

    public List<Coordinate> getCoordinates() {
        return this.coordinates;
    }
}
