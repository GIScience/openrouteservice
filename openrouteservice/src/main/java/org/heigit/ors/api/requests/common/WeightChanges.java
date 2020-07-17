package org.heigit.ors.api.requests.common;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Polygon;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import org.heigit.ors.api.requests.routing.RouteRequest;
import org.heigit.ors.exceptions.ParameterValueException;
import org.heigit.ors.geojson.GeometryJSON;
import org.heigit.ors.routing.RoutingErrorCodes;
import org.json.JSONArray;
import org.json.JSONObject;

public class WeightChanges {
  private List<WeightChange> changes = new ArrayList<>();

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

  public void setChanges(List<WeightChange> changes) {
    this.changes = changes;
  }

  public void addChanges(Geometry geom, Double weight) throws ParameterValueException {
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
      Double weight = properties.getDouble("weight");
      addChanges(geom, weight);
    }
  }

  // if used: TODO add proper types, don't use the generic ones (e.g. LinkedHashMap)
  // otherwise: TODO remove method
  public void parseAlternative(org.json.simple.JSONObject input) throws ParameterValueException {
    ArrayList<LinkedHashMap> features = (ArrayList<LinkedHashMap>) input.get("features");
    for (LinkedHashMap feature : features) {
      JSONObject geomJson = new JSONObject();
      LinkedHashMap geometry = (LinkedHashMap) feature.get("geometry");
      geomJson.put("type", geometry.get("type"));
      List<List<Double[]>> coordinates = (List<List<Double[]>>) geomJson.get("coordinates");
      geomJson.put("coordinates", coordinates);
      Geometry geom;
      try {
        geom = GeometryJSON.parse(geomJson);
      } catch (Exception e) {
        throw new ParameterValueException(RoutingErrorCodes.INVALID_JSON_FORMAT, RouteRequest.PARAM_WEIGHT_CHANGES);
      }
      LinkedHashMap properties = (LinkedHashMap) feature.get("properties");
      Double weight = (Double) properties.get("weight");
      addChanges(geom, weight);
    }
  }
}
