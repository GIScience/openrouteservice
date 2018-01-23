package heigit.ors.services.routing.requestprocessors.geojson;

import com.graphhopper.util.shapes.BBox;
import com.vividsolutions.jts.geom.LineString;
import heigit.ors.config.AppConfig;
import heigit.ors.routing.RouteResult;
import heigit.ors.routing.RoutingRequest;
import heigit.ors.services.routing.RoutingServiceSettings;
import heigit.ors.services.routing.requestprocessors.json.JsonRoutingResponseWriter;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.json.JSONArray;
import org.json.JSONObject;
import org.opengis.feature.simple.SimpleFeatureType;

import javax.sound.sampled.Line;

class FeatureParser {
    // TODO implement all the different kind of SimpleFeatureTypes here!
    public static SimpleFeatureType createRouteFeatureType() throws Exception {
        // make routeResults and request accessible class wide
        // create SimpleFeatureType template
        SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
        // set the name --> The result looks weird but a name is required!! result -->  https://go.openrouteservice.org/:openrouteservice routing
        builder.setName(RoutingServiceSettings.getParameter("routing_name"));
        builder.add("geometry", LineString.class);
        return builder.buildFeatureType();
    }

    public static SimpleFeatureCollection creatureRouteFeatureCollection(SimpleFeatureType feature) {
        //featureBuilder.add("info", JSONObject.class);
        //featureBuilder.add("extras", JSONObject.class);

        return null;
    }
}
