package heigit.ors.services.routing;

import com.vividsolutions.jts.geom.Coordinate;
import org.json.JSONArray;
import org.json.JSONObject;

public class RouteObject {
    private Coordinate start;
    //private Coordinate destination;
    //private Coordinate[] via;

    public RouteObject() {

    }

    public void setStart(String coords) throws Exception{
        start = parseCoordinateFromString(coords);
    }

    private Coordinate parseCoordinateFromString(String coordinatesAsString) throws Exception {
        String coords[] = coordinatesAsString.split(",");
        if(coords.length == 2) {
            Coordinate c = new Coordinate();
            c.x = Double.parseDouble(coords[0]);
            c.y = Double.parseDouble(coords[1]);

            return c;
        } else {
            throw new Exception("Invalid coordinates");
        }
    }

    public Coordinate getStart() {
        return start;
    }

    public JSONObject toGeoJSON() {
        JSONObject json = new JSONObject();

        JSONObject geom = new JSONObject();
        geom.put("type", "Point");
        JSONArray coords = new JSONArray();
        coords.put(start.x);
        coords.put(start.y);
        geom.put("coords", coords);

        json.put("geometry", geom);

        return json;
    }
}
