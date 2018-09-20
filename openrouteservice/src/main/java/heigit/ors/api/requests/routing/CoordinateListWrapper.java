package heigit.ors.api.requests.routing;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.vividsolutions.jts.geom.Coordinate;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.ArrayList;
import java.util.List;

@ApiModel(value = "Coordinates", description = "An array of waypoints in the longitude/latitude pairs.")
public class CoordinateListWrapper {
    @JsonIgnore
    private Coordinate start;
    @JsonIgnore
    private Coordinate end;
    @JsonIgnore
    private List<Coordinate> via;

    public CoordinateListWrapper(Coordinate start, Coordinate end) {
        this(start, new ArrayList<>(), end);
    }

    public CoordinateListWrapper(Coordinate start, List<Coordinate> via, Coordinate end) {
        this.start = start;
        this.via = via;
        this.end = end;
    }

    public CoordinateListWrapper(List<Coordinate> coordinates) throws Exception {
        if (coordinates.size() < 2)
            throw new Exception("Invalid coordinates length");

        start = coordinates.get(0);

        via = new ArrayList<>();
        for (int i = 1; i < coordinates.size() - 1; i++) {
            via.add(coordinates.get(i));
        }

        end = coordinates.get(coordinates.size()-1);
    }

    public CoordinateListWrapper(double[][] coordinates) throws Exception {
        if (coordinates.length < 2)
            throw new Exception("Invalid coordinates length");

        for (double[] coordPair : coordinates) {
            if (coordPair.length != 2)
                throw new Exception("Coordinates must be in pairs");
        }

        start = new Coordinate(coordinates[0][0], coordinates[0][1]);

        via = new ArrayList<>();
        for (int i = 1; i < coordinates.length - 1; i++) {
            via.add(new Coordinate(coordinates[i][0], coordinates[i][1]));
        }

        end = new Coordinate(coordinates[coordinates.length - 1][0], coordinates[coordinates.length - 1][1]);
    }

    public Coordinate getStart() {
        return start;
    }

    public Coordinate getEnd() {
        return end;
    }

    public List<Coordinate> getVia() {
        return via;
    }

    @ApiModelProperty
    public Coordinate[] getCoordinates() {
        List<Coordinate> coordinates = new ArrayList<>();
        coordinates.add(start);
        coordinates.addAll(via);
        coordinates.add(end);

        return coordinates.toArray(new Coordinate[coordinates.size()]);
    }
}
