package org.heigit.ors.weightaugmentation;

import com.vividsolutions.jts.geom.Geometry;
import java.util.ArrayList;
import java.util.List;
import org.heigit.ors.api.requests.routing.RouteRequest;
import org.heigit.ors.exceptions.ParameterValueException;
import org.heigit.ors.geojson.GeometryJSON;
import org.heigit.ors.routing.RoutingErrorCodes;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * <p>Parser to parse the given GeoJSON input to a list of {@link AugmentedWeight AugmentedWeights}.</p>
 *
 * <ul>
 *   <li>GeoJSON input has to follow <a href="https://tools.ietf.org/html/rfc7946">RFC7946</a>.</li>
 *   <li>{@code "FeatureCollection"} and {@code "Feature"} are supported.</li>
 *   <li>{@code "Geometry"} is not possible because of the missing {@code "properties"}.</li>
 *   <li>The augmentation (or weight factor) has to be stored as {@code "weight"} in {@code "properties"}.</li>
 *   <li>Currently, the only supported geometry type is {@code "Polygon"}.</li>
 * </ul>
 * <p>Example GeoJSON:<br>
 * <pre>
 * {
 *     "type": "Feature",
 *     "properties": {
 *         "weight": 1.5
 *     },
 *     "geometry": {
 *         "type": "Polygon",
 *         "coordinates": [
 *             [
 *                 [
 *                     8.674982786178589,
 *                     49.37891531029554
 *                 ],
 *                 [
 *                     8.674918413162231,
 *                     49.378621937439284
 *                 ],
 *                 [
 *                     8.674982786178589,
 *                     49.37891531029554
 *                 ]
 *             ]
 *         ]
 *     }
 * }
 * </pre>
 * </p>
 */
public class UserWeightParser {
  public static final String ALLOWED_GEOMETRY_TYPES = "Point, LineString, Polygon, MultiPoint, MultiLineString, MultiPolygon, GeometryCollection";

  /**
   * Overloaded function for {@link #parse(JSONObject)} that receives a {@link String} instead of a {@link JSONObject org.json.JSONObject}.
   */
  public List<AugmentedWeight> parse(String input) throws ParameterValueException {
    return parse(new JSONObject(input));
  }

  /**
   * Overloaded function for {@link #parse(JSONObject)} that receives a {@link org.json.simple.JSONObject org.json.simple.JSONObject} instead of a {@link JSONObject org.json.JSONObject}.
   */
  public List<AugmentedWeight> parse(org.json.simple.JSONObject input) throws ParameterValueException {
    return parse(input.toJSONString());
  }

  /**
   * Alternative parsing method that parses arrays of geometries and weights instead of JSONs.
   * @param geometries array of geometries with the same length as {@code weights}
   * @param weights array of weights with the same length as {@code geometries}
   * @return {@link ArrayList} of {@link AugmentedWeight}
   * @throws ParameterValueException for wrong parameters
   */
  public List<AugmentedWeight> parse(Geometry[] geometries, double[] weights) throws ParameterValueException {
    List<AugmentedWeight> weightAugmentations = new ArrayList<>();
    if (geometries.length == weights.length) {
      for (int i = 0; i < geometries.length; i++) {
        addWeightAugmentations(weightAugmentations, geometries[i], weights[i]);
      }
    } else {
      throw new ParameterValueException(RoutingErrorCodes.INCOMPATIBLE_PARAMETERS, RouteRequest.PARAM_USER_WEIGHTS, geometries.length + " != " + weights.length, "Given weights and geometries length not equal.");
    }
    return weightAugmentations;
  }

  /**
   * Adds the given weight augmentations an existing list and checks first if the geometry is supported.
   * @param weightAugmentations list to add the augmentation to
   * @param geom geometry to be added
   * @param weight weight factor to be added
   * @throws ParameterValueException is thrown if the geometry is not supported
   */
  public void addWeightAugmentations(List<AugmentedWeight> weightAugmentations, Geometry geom, double weight) throws ParameterValueException {
    if (ALLOWED_GEOMETRY_TYPES.contains(geom.getGeometryType())) {
      weightAugmentations.add(new AugmentedWeight(geom, weight));
    } else {
      throw new ParameterValueException(RoutingErrorCodes.INVALID_PARAMETER_VALUE, RouteRequest.PARAM_USER_WEIGHTS, geom.getGeometryType(),
          "Only these geometry types are currently implemented: " + ALLOWED_GEOMETRY_TYPES);
    }
  }

  /**
   * Regular parser for GeoJSON input.
   *
   * It uses {@link GeometryJSON} to parse the {@code "Geometry"} objects.
   * @param input {@link org.json.JSONObject} to be parsed
   * @return {@link ArrayList} of {@link AugmentedWeight}
   * @throws ParameterValueException for wrong parameters
   */
  public List<AugmentedWeight> parse(org.json.JSONObject input) throws ParameterValueException {
    List<AugmentedWeight> weightAugmentations = new ArrayList<>();
    JSONArray features;
    String geoJSONtype = input.getString("type");
    if (geoJSONtype.equals("FeatureCollection")) {
      features = input.getJSONArray("features");
    } else if (geoJSONtype.equals("Feature")) {
      features = new JSONArray();
      features.put(input);
    } else {
      throw new ParameterValueException(RoutingErrorCodes.INVALID_JSON_FORMAT, RouteRequest.PARAM_USER_WEIGHTS, geoJSONtype, "Invalid GeoJSON type. Only 'FeatureCollection' or 'Feature' is allowed.");
    }
    for (int i = 0; i < features.length(); i++) {
      JSONObject feature = features.getJSONObject(i);
      JSONObject geometry = feature.getJSONObject("geometry");
      Geometry geom;
      try {
        geom = GeometryJSON.parse(geometry);
      } catch (Exception e) {
        throw new ParameterValueException(RoutingErrorCodes.INVALID_JSON_FORMAT, RouteRequest.PARAM_USER_WEIGHTS, feature.toString(), "Geometry could not be parsed.");
      }
      JSONObject properties = feature.getJSONObject("properties");
      double weight = properties.getDouble("weight");
      addWeightAugmentations(weightAugmentations, geom, weight);
    }
    return weightAugmentations;
  }
}
