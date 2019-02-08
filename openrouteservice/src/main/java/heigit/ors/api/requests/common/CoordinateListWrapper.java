/*
 * This file is part of Openrouteservice.
 *
 * Openrouteservice is free software; you can redistribute it and/or modify it under the terms of the
 * GNU Lesser General Public License as published by the Free Software Foundation; either version 2.1
 * of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this library;
 * if not, see <https://www.gnu.org/licenses/>.
 */

package heigit.ors.api.requests.common;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.vividsolutions.jts.geom.Coordinate;
import heigit.ors.api.converters.CoordinateListDeserializer;
import heigit.ors.exceptions.ParameterValueException;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.ArrayList;
import java.util.List;

@ApiModel(value = "Coordinates", description = "An array of waypoints in the longitude/latitude pairs.")
@JsonDeserialize(using = CoordinateListDeserializer.class)
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

    public CoordinateListWrapper(List<Coordinate> coordinates) throws ParameterValueException {
        if (coordinates.size() < 2)
            throw new ParameterValueException("Invalid coordinates length");

        start = coordinates.get(0);

        via = new ArrayList<>();
        for (int i = 1; i < coordinates.size() - 1; i++) {
            via.add(coordinates.get(i));
        }

        end = coordinates.get(coordinates.size()-1);
    }

    public CoordinateListWrapper(double[][] coordinates) throws ParameterValueException {
        if (coordinates.length < 2)
            throw new ParameterValueException("Invalid coordinates length");

        for (double[] coordPair : coordinates) {
            if (coordPair.length != 2)
                throw new ParameterValueException("Coordinates must be in pairs");
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

    public List<List<Double>> getCoordinatesList() {
        List<List<Double>> coordinates = new ArrayList<>();
        List<Double> startCoords = new ArrayList<>();
        startCoords.add(start.x);
        startCoords.add(start.y);
        coordinates.add(startCoords);
        for(Coordinate c : via) {
            List<Double> viaCoords = new ArrayList<>();
            viaCoords.add(c.x);
            viaCoords.add(c.y);
            coordinates.add(viaCoords);
        }
        List<Double> endCoords = new ArrayList<>();
        endCoords.add(end.x);
        endCoords.add(end.y);
        coordinates.add(endCoords);

        return coordinates;
    }
}
