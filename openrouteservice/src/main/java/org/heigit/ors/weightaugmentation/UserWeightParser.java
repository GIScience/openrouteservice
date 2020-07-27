package org.heigit.ors.weightaugmentation;

import com.graphhopper.storage.GraphHopperStorage;
import com.graphhopper.util.EdgeIteratorState;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Polygon;
import java.util.ArrayList;
import java.util.List;
import org.heigit.ors.api.requests.routing.RouteRequest;
import org.heigit.ors.exceptions.ParameterValueException;
import org.heigit.ors.geojson.GeometryJSON;
import org.heigit.ors.routing.RoutingErrorCodes;
import org.json.JSONArray;
import org.json.JSONObject;

public class UserWeightParser {
  public List<AugmentedWeight> parse(String input) throws ParameterValueException {
    return parse(new JSONObject(input));
  }

  public List<AugmentedWeight> parse(org.json.simple.JSONObject input) throws ParameterValueException {
    return parse(input.toJSONString());
  }

  public List<AugmentedWeight> parse(Geometry[] geometries, double[] weights) throws ParameterValueException {
    List<AugmentedWeight> weightAugmentations = new ArrayList<>();
    if (geometries.length == weights.length) {
      for (int i = 0; i < geometries.length; i++) {
        addWeightAugmentations(weightAugmentations, geometries[i], weights[i]);
      }
    } else {
      throw new ParameterValueException(RoutingErrorCodes.INCOMPATIBLE_PARAMETERS, RouteRequest.PARAM_USER_WEIGHTS);
    }
    return weightAugmentations;
  }

  public void addWeightAugmentations(List<AugmentedWeight> weightAugmentations, Geometry geom, double weight) throws ParameterValueException {
    if (geom instanceof Polygon) {
      weightAugmentations.add(new AugmentedWeight(geom, weight));
    } else {
      throw new ParameterValueException(RoutingErrorCodes.INVALID_PARAMETER_VALUE, RouteRequest.PARAM_USER_WEIGHTS);
    }
  }

  public List<AugmentedWeight> parse(org.json.JSONObject input) throws ParameterValueException {
    List<AugmentedWeight> weightAugmentations = new ArrayList<>();
    JSONArray features = input.getJSONArray("features");
    for (int i = 0; i < features.length(); i++) {
      JSONObject feature = features.getJSONObject(i);
      Geometry geom;
      try {
        geom = GeometryJSON.parse(feature.getJSONObject("geometry"));
      } catch (Exception e) {
        throw new ParameterValueException(RoutingErrorCodes.INVALID_JSON_FORMAT, RouteRequest.PARAM_USER_WEIGHTS);
      }
      JSONObject properties = feature.getJSONObject("properties");
      double weight = properties.getDouble("weight");
      addWeightAugmentations(weightAugmentations, geom, weight);
    }
    return weightAugmentations;
  }
}
