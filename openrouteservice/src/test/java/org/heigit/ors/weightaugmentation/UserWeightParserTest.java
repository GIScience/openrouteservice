package org.heigit.ors.weightaugmentation;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import junit.framework.TestCase;
import org.heigit.ors.geojson.GeometryJSON;
import org.json.JSONObject;

public class UserWeightParserTest extends TestCase {

  private static final GeometryFactory factory = new GeometryFactory();
  private UserWeightParser userWeightParser;
  private List<Geometry> geometries;
  private List<Double> weights;

  public void setUp() throws Exception {
    super.setUp();
    String inputJson = "{\"type\": \"FeatureCollection\", \"features\": [{\"type\": \"Feature\", \"properties\": {\"weight\": \"5.0\"}, \"geometry\": {\"type\": \"Polygon\", \"coordinates\": [[[8.691, 49.415], [8.691, 49.413], [8.699, 49.413], [8.691, 49.415]]] } }, { \"type\": \"Feature\", \"properties\": { \"weight\": 0.1 }, \"geometry\": { \"type\": \"Polygon\", \"coordinates\": [[[8.682, 49.413], [8.689, 49.413], [8.689, 49.419], [8.682, 49.419], [8.682, 49.413]], [[8.684, 49.418], [8.684, 49.414], [8.687, 49.414], [8.687, 49.418], [8.684, 49.418]]]}}]}";
    userWeightParser = new UserWeightParser(inputJson);
    geometries = new ArrayList<>();
    geometries.add(GeometryJSON.parse(new JSONObject("{\"type\": \"Polygon\", \"coordinates\": [[[8.691, 49.415], [8.691, 49.413], [8.699, 49.413], [8.691, 49.415]]]}")));
    geometries.add(GeometryJSON.parse(new JSONObject("{\"type\": \"Polygon\", \"coordinates\": [[[8.682, 49.413], [8.689, 49.413], [8.689, 49.419], [8.682, 49.419], [8.682, 49.413]], [[8.684, 49.418], [8.684, 49.414], [8.687, 49.414], [8.687, 49.418], [8.684, 49.418]]]}")));
    weights = new ArrayList<>(Arrays.asList(5.0, 0.1));
  }

  public void testGetWeightAugmentations() {
    List<AugmentedWeight> weightAugmentations = userWeightParser.getWeightAugmentations();
    ArrayList<AugmentedWeight> expectedWeightAugmentations = new ArrayList<>();
    expectedWeightAugmentations.add(new AugmentedWeight(geometries.get(0), weights.get(0)));
    expectedWeightAugmentations.add(new AugmentedWeight(geometries.get(1), weights.get(1)));
    assertEquals(expectedWeightAugmentations, weightAugmentations);
  }

  public void testAddWeightAugmentations() throws Exception {
    String geomJson = "{\"type\": \"Polygon\",\"coordinates\": [[[8.681,49.420],[8.685,49.420],[8.684,49.423],[8.681,49.420]]]}";
    Geometry geom = GeometryJSON.parse(new JSONObject(geomJson));
    userWeightParser.addWeightAugmentations(geom, 1.3);
    AugmentedWeight expectedResult = new AugmentedWeight(geom, 1.3);
    AugmentedWeight actualResult = userWeightParser
        .getWeightAugmentations().get(userWeightParser.getWeightAugmentations().size() - 1);
    assertEquals(expectedResult, actualResult);
  }
}