package org.heigit.ors.weightaugmentation;

import com.graphhopper.storage.GraphHopperStorage;
import com.graphhopper.util.EdgeIteratorState;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Polygon;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.heigit.ors.api.requests.routing.RouteRequest;
import org.heigit.ors.exceptions.ParameterValueException;
import org.heigit.ors.geojson.GeometryJSON;
import org.heigit.ors.routing.RoutingErrorCodes;
import org.json.JSONArray;
import org.json.JSONObject;

public class UserWeightParser {
  private final List<AugmentedWeight> weightAugmentations = new ArrayList<>();

  public UserWeightParser(org.json.JSONObject input) throws ParameterValueException {
    parse(input);
  }

  public UserWeightParser(String input) throws ParameterValueException {
    this(new JSONObject(input));
  }

  public UserWeightParser(org.json.simple.JSONObject input) throws ParameterValueException {
    this(input.toJSONString());
  }

  public UserWeightParser(Geometry[] geometries, double[] weights) throws ParameterValueException {
    if (geometries.length == weights.length) {
      for (int i = 0; i < geometries.length; i++) {
        addWeightAugmentations(geometries[i], weights[i]);
      }
    } else {
      throw new ParameterValueException(RoutingErrorCodes.INCOMPATIBLE_PARAMETERS, RouteRequest.PARAM_USER_WEIGHTS);
    }
  }

  public List<AugmentedWeight> getWeightAugmentations() {
    return weightAugmentations;
  }

  public void addWeightAugmentations(Geometry geom, double weight) throws ParameterValueException {
    if (geom instanceof Polygon) {
      weightAugmentations.add(new AugmentedWeight(geom, weight));
    } else {
      throw new ParameterValueException(RoutingErrorCodes.INVALID_PARAMETER_VALUE, RouteRequest.PARAM_USER_WEIGHTS);
    }
  }

  public void parse(org.json.JSONObject input) throws ParameterValueException {
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
      addWeightAugmentations(geom, weight);
    }
  }

  public void applyAugmentationsToAll(GraphHopperStorage ghs) {
    for (AugmentedWeight augmentedWeight: weightAugmentations) {
      augmentedWeight.applyAugmentationToAll(ghs);
    }
  }

  public double getAugmentations(EdgeIteratorState edge) {
    double factor = 1.0;
    for (AugmentedWeight augmentedWeight: weightAugmentations) {
      factor *= augmentedWeight.getAugmentation(edge);
    }
    return factor;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    UserWeightParser that = (UserWeightParser) o;
    return Objects.equals(weightAugmentations, that.weightAugmentations);
  }

  @Override
  public int hashCode() {
    return Objects.hash(weightAugmentations);
  }
}
