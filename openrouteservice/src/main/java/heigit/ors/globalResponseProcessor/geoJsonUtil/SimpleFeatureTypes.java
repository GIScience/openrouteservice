package heigit.ors.globalResponseProcessor.geoJsonUtil;


import com.vividsolutions.jts.geom.LineString;
import heigit.ors.globalResponseProcessor.gpxUtil.GpxResponseWriter;
import heigit.ors.exceptions.MissingConfigParameterException;
import heigit.ors.services.routing.RoutingServiceSettings;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.opengis.feature.simple.SimpleFeatureType;

/**
 * {@link SimpleFeatureTypes} defines {@link SimpleFeatureType} for each Request that will be exported as GeoJSON.
 * The class is only accessible through classes in the same package.
 *
 * @author Julian Psotta, julian@openrouteservice.org
 */
class SimpleFeatureTypes {
    // TODO implement all the different kind of SimpleFeatureTypes here!
    public static SimpleFeatureType createRouteFeatureType() {
        // make routeResults and request accessible class wide
        // create SimpleFeatureType template
        SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
        // set the name --> The result looks weird but a name is required!! result -->  https://go.openrouteservice.org/:openrouteservice routing
        if (!(RoutingServiceSettings.getParameter("routing_name") == null)) {
            builder.setName(RoutingServiceSettings.getParameter("routing_name"));

        } else {
            builder.setName("ORSRoutingFile");
            new MissingConfigParameterException(GpxResponseWriter.class, "routing_name");
        }


        builder.add("geometry", LineString.class);
        return builder.buildFeatureType();
    }

    public static SimpleFeatureType createIsoChronesFeatureType() {
        return null;
    }
}
