package heigit.ors.util.GlobalResponseProcessor;

import heigit.ors.geocoding.geocoders.GeocodingResult;
import heigit.ors.isochrones.IsochroneMapCollection;
import heigit.ors.isochrones.IsochroneRequest;
import heigit.ors.locations.LocationsRequest;
import heigit.ors.locations.LocationsResult;
import heigit.ors.mapmatching.MapMatchingRequest;
import heigit.ors.matrix.MatrixRequest;
import heigit.ors.matrix.MatrixResult;
import heigit.ors.routing.RouteResult;
import heigit.ors.routing.RoutingRequest;
import heigit.ors.services.ServiceRequest;
import heigit.ors.services.geocoding.requestprocessors.GeocodingRequest;
import heigit.ors.util.GlobalResponseProcessor.geoJsonUtil.GeoJsonResponseWriter;
import org.json.JSONObject;

import javax.servlet.http.HttpServletResponse;

/**
 * The {@link GlobalResponseProcessor} works as a global Class to process all export functions in one place.
 * The class will assure, that the exports will mostly look the same or at least will reuse parts of it, so integrating new exports will result in minimal adjusting with already existing processions of ors exports.
 * The benefit is that the user can get an easy overview about existing export options and also easily modify existing and integrate new ones in one place.
 * <p>
 * The {@link GlobalResponseProcessor} doesn't include the {@link heigit.ors.servlet.util.ServletUtility} function to write the output.
 * So {@link heigit.ors.servlet.util.ServletUtility} must be called separately with the returned {@link JSONObject}.
 */
public class GlobalResponseProcessor {
    private HttpServletResponse response;
    private GeocodingRequest geocodingRequest;
    private GeocodingResult geocodingResult;
    private IsochroneRequest isochroneRequest;
    private IsochroneMapCollection isochroneMapCollection; // The result type for Isochrones!!!
    private LocationsRequest locationsRequest;
    private LocationsResult locationsResult;
    private MapMatchingRequest mapMatchingRequest;
    private MatrixRequest matrixRequest;
    private MatrixResult matrixResult;
    private RoutingRequest routingRequest;
    private RouteResult[] routeResult;

    // TODO The constructors still need refinement with their inputs
    public GlobalResponseProcessor(HttpServletResponse response, GeocodingRequest geocodingRequest, GeocodingResult geocodingResult) {
        this.response = response;
        this.geocodingRequest = geocodingRequest;
        this.geocodingResult = geocodingResult;
    }

    public GlobalResponseProcessor(HttpServletResponse response, IsochroneRequest isochroneRequest, IsochroneMapCollection isochroneMapCollection) {
        this.response = response;
        this.isochroneRequest = isochroneRequest;
        this.isochroneMapCollection = isochroneMapCollection;
    }

    public GlobalResponseProcessor(HttpServletResponse response, LocationsRequest locationsRequest, LocationsResult locationsResult) {
        this.response = response;
        this.locationsRequest = locationsRequest;
        this.locationsResult = locationsResult;
    }

    public GlobalResponseProcessor(HttpServletResponse response, MapMatchingRequest mapMatchingRequest, RouteResult[] routeResult) {
        this.response = response;
        this.mapMatchingRequest = mapMatchingRequest;
        this.routeResult = routeResult;
    }

    public GlobalResponseProcessor(HttpServletResponse response, MatrixRequest matrixRequest, MatrixResult matrixResult) {
        this.response = response;
        this.matrixRequest = matrixRequest;
        this.matrixResult = matrixResult;
    }

    public GlobalResponseProcessor(HttpServletResponse response, RoutingRequest request, RouteResult[] result) {
        this.response = response;
        this.routingRequest = request;
        this.routeResult = result;
    }

    /**
     * The function works as a distribution class that is/will be able to process any kind of request result combination as an input.
     * If the function doesn't provide a specific Export for a specific {@link ServiceRequest} yet, the {@link JSONObject} will be returned empty.
     * <p>
     * TODO remove these @param comments if not used in the end
     * //* @param response The input should be {@link HttpServletResponse}
     * //* @param request  The input should be of class {@link ServiceRequest} e.g. {@link RoutingRequest}
     * //* @param result   It should be one of the following MapMatchingRes {@link heigit.ors.geocoding.geocoders.GeocodingResult}, {@link heigit.ors.isochrones.IsochroneMapCollection}, {@link heigit.ors.locations.LocationsResult}, {@link heigit.ors.matrix.MatrixResult}, {@link RouteResult}
     *
     * @return The method returns a GeoJson as a {@link JSONObject} that can be directly imported into {@link heigit.ors.servlet.util.ServletUtility}'s write function. If a specific {@link ServiceRequest} isn't integrated yet, the {@link JSONObject} will be empty.
     * @throws Exception There will be no specific Exceptions here. The reason is, that this class shouldn't process any variable parameters from the {@link javax.servlet.http.HttpServletRequest}. So the content will always
     */

    public JSONObject toGeoJson() throws Exception {
        // Create an empty JSONObject to store the export result later or return it as it is.
        JSONObject geojson = new JSONObject();
        // Check for the correct ServiceRequest and chose the right export function
        // TODO Integrate all exports here by time
        if (!(this.geocodingRequest == null)) {
            if (geocodingResult != null) {
                // TODO Do export
            }
        } else if (!(this.isochroneRequest == null)) {
            if (this.isochroneMapCollection.size() > 0) {
                // TODO Do export
            }
        } else if (!(this.locationsRequest == null)) {
            if (locationsResult.getGeometry().getLength() > 0){
                // TODO Do export
            }
        } else if (!(this.mapMatchingRequest == null)) {
            if (this.isochroneMapCollection.size() > 0){
                // TODO Do export
            }
        } else if (!(this.matrixRequest == null)) {
            if (this.matrixResult.getSources().length > 0 && this.matrixResult.getDestinations().length > 0){
                // TODO Do export
            }
        } else if (!(this.routingRequest == null)) {
            if (this.routeResult.length > 0)
                return GeoJsonResponseWriter.toGeoJson(routingRequest, routeResult);
        } else {
            return geojson;
        }
        return geojson;
    }


}
