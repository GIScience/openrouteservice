package heigit.ors.util.GlobalExportWriter.geoJsonUtil;

import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import org.geotools.geojson.GeoJSON;
import org.json.JSONObject;

import java.io.StringWriter;
import java.io.Writer;

public class GeoJsonExportWriter {
    /**
     * *Not implemented yet*
     * This function creates a Point-GeoJsonExportWriter
     *
     * @param point Input must be a {@link Point}
     * @return Returns a GeometryJSON as a well formatted {@link String}
     */
    public static String toGeoJSON(Point point) {
        return null;
    }

    /**
     * *Not implemented yet*
     * This function creates a Line-GeoJsonExportWriter
     *
     * @param lineString Input must be a {@link LineString}
     * @return Returns a GeometryJSON as a well formatted {@link String}
     */
    public static JSONObject toGeoJSON(LineString lineString) throws Exception {
        // Create StringWriter to catch output of GeoJsonExportWriter
        Writer output = new StringWriter();
        GeoJSON.write(lineString, output);
        GeoJSON.write(lineString, new StringWriter());
        return new JSONObject(output.toString());
    }
    // TODO: Integrate all geometry features into the class
}
