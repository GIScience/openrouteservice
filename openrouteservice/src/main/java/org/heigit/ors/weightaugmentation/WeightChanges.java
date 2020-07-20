package org.heigit.ors.weightaugmentation;

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

public class WeightChanges {
  private final List<WeightChange> changes = new ArrayList<>();

  public WeightChanges(org.json.JSONObject input) throws ParameterValueException {
    parse(input);
  }

  public WeightChanges(String input) throws ParameterValueException {
    this(new JSONObject(input));
  }

  public WeightChanges(org.json.simple.JSONObject input) throws ParameterValueException {
    this(input.toJSONString());
  }

  public List<WeightChange> getChanges() {
    return changes;
  }

  public void addChanges(Geometry geom, double weight) throws ParameterValueException {
    if (geom instanceof Polygon) {
      changes.add(new WeightChange(geom, weight));
    } else {
      throw new ParameterValueException(RoutingErrorCodes.INVALID_PARAMETER_VALUE, RouteRequest.PARAM_WEIGHT_CHANGES);
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
        throw new ParameterValueException(RoutingErrorCodes.INVALID_JSON_FORMAT, RouteRequest.PARAM_WEIGHT_CHANGES);
      }
      JSONObject properties = feature.getJSONObject("properties");
      double weight = properties.getDouble("weight");
      addChanges(geom, weight);
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    WeightChanges that = (WeightChanges) o;
    return Objects.equals(changes, that.changes);
  }

  @Override
  public int hashCode() {
    return Objects.hash(changes);
  }
}
