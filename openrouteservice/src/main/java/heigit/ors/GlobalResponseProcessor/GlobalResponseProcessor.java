package heigit.ors.GlobalResponseProcessor;

import heigit.ors.exceptions.UnsupportedExportException;
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
import heigit.ors.servlet.util.ServletUtility;
import heigit.ors.GlobalResponseProcessor.geoJsonUtil.GeoJsonResponseWriter;
import heigit.ors.GlobalResponseProcessor.gpxUtil.GpxResponseWriter;
import org.json.JSONObject;

import javax.servlet.http.HttpServletResponse;

/**
 * The {@link GlobalResponseProcessor} works as a global Class to process all export functions in one place.
 * The class will assure, that the exports will mostly look the same or at least will reuse parts of it, so integrating new exports will result in minimal adjusting with already existing processions of ors exports.
 * The benefit is that the user can get an easy overview about existing export options and also easily modify existing and integrate new ones in one place.
 * <p>
 * The {@link GlobalResponseProcessor} doesn't include the {@link heigit.ors.servlet.util.ServletUtility} function to write the output.
 * So {@link heigit.ors.servlet.util.ServletUtility} must be called separately with the returned {@link JSONObject}.
 *
 * @author Julian Psotta
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
    // TODO add try and catch errors to all subclasses
    // TODO finish commenting
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
     *
     * @return The method returns a GeoJson as a {@link JSONObject} that can be directly imported into {@link ServletUtility}'s write function. If a specific {@link ServiceRequest} isn't integrated yet, the {@link JSONObject} will be empty.
     * @throws Exception An error will be raised using {@link UnsupportedExportException} with {@link org.apache.log4j.Logger}.
     */

    public JSONObject toGeoJson() throws Exception {
        // Check for the correct ServiceRequest and chose the right export function
        // TODO Integrate all exports here by time
        if (!(this.geocodingRequest == null)) {
            new UnsupportedExportException(this.getClass(), geocodingRequest.getClass(), "GeoJSON");
            if (!(geocodingResult == null)) {
                // TODO Do export
            }
        } else if (!(this.isochroneRequest == null)) {
            new UnsupportedExportException(this.getClass(), isochroneRequest.getClass(), "GeoJSON");
            if (this.isochroneMapCollection.size() > 0) {
                // TODO Do export
            }
        } else if (!(this.locationsRequest == null)) {
            new UnsupportedExportException(this.getClass(), locationsRequest.getClass(), "GeoJSON");
            if (locationsResult.getGeometry().getLength() > 0) {
                // TODO Do export
            }
        } else if (!(this.mapMatchingRequest == null)) {
            new UnsupportedExportException(this.getClass(), mapMatchingRequest.getClass(), "GeoJSON");
            if (this.isochroneMapCollection.size() > 0) {
                // TODO Do export
            }
        } else if (!(this.matrixRequest == null)) {
            new UnsupportedExportException(this.getClass(), matrixRequest.getClass(), "GeoJSON");
            if (this.matrixResult.getSources().length > 0 && this.matrixResult.getDestinations().length > 0) {
                // TODO Do export
            }
        } else if (!(this.routingRequest == null)) {
            if (this.routeResult.length > 0)
                return GeoJsonResponseWriter.toGeoJson(routingRequest, routeResult);
        } else {
            return null;
        }
        return null;
    }

    /**
     * The function works as a distribution class that is/will be able to process any kind of request result combination as an input.
     * If the function doesn't provide a specific Export for a specific {@link ServiceRequest} yet, the {@link JSONObject} will be returned empty.
     *
     * @return The method returns a GPX as a {@link String} that can be directly imported into {@link ServletUtility}'s write function. If a specific {@link ServiceRequest} isn't integrated yet, the {@link String} will be empty.
     * @throws Exception An error will be raised using {@link UnsupportedExportException} with {@link org.apache.log4j.Logger}.
     */
    public String toGPX() throws Exception {
        // Check for the correct ServiceRequest and chose the right export function
        // TODO Integrate all exports here by time
        if (!(this.geocodingRequest == null)) {
            new UnsupportedExportException(this.getClass(), geocodingRequest.getClass(), "toGPX");
            if (!(geocodingResult == null)) {
                // TODO Do export
            }
        } else if (!(this.isochroneRequest == null)) {
            new UnsupportedExportException(this.getClass(), isochroneRequest.getClass(), "toGPX");
            if (this.isochroneMapCollection.size() > 0) {
                // TODO Do export
            }
        } else if (!(this.locationsRequest == null)) {
            new UnsupportedExportException(this.getClass(), locationsRequest.getClass(), "toGPX");
            if (locationsResult.getGeometry().getLength() > 0) {
                // TODO Do export
            }
        } else if (!(this.mapMatchingRequest == null)) {
            new UnsupportedExportException(this.getClass(), mapMatchingRequest.getClass(), "toGPX");
            if (this.isochroneMapCollection.size() > 0) {
                // TODO Do export
            }
        } else if (!(this.matrixRequest == null)) {
            new UnsupportedExportException(this.getClass(), matrixRequest.getClass(), "toGPX");
            if (this.matrixResult.getSources().length > 0 && this.matrixResult.getDestinations().length > 0) {
                // TODO Do export
            }
        } else if (!(this.routingRequest == null)) {
            if (this.routeResult.length > 0)
                return GpxResponseWriter.toGPX(routingRequest, routeResult);
        } else {
            return null;
        }
        return null;
    }

}
