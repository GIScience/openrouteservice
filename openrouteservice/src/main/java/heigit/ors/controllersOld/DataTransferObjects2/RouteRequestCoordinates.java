package heigit.ors.controllersOld.DataTransferObjects2;

import com.vividsolutions.jts.geom.Coordinate;

import java.util.ArrayList;

public class RouteRequestCoordinates {
    private double[] start;
    private double[] end;
    private double[][] via;

    public double[] getStart() {
        return start;
    }

    public void setStart(double[] start) {
        this.start = start;
    }

    public double[] getEnd() {
        return end;
    }

    public void setEnd(double[] end) {
        this.end = end;
    }

    public double[][] getVia() {
        return via;
    }

    public void setVia(double[][] via) {
        this.via = via;
    }

    public Coordinate[] getAsCoordinateArray() {
        ArrayList<Coordinate> coords = new ArrayList<>();
        coords.add(new Coordinate(start[0],start[1]));
        for(double[] coord : via) {
            coords.add(new Coordinate(coord[0], coord[1]));
        }
        coords.add(new Coordinate(end[0], end[1]));

        return coords.toArray(new Coordinate[coords.size()]);
    }
}
