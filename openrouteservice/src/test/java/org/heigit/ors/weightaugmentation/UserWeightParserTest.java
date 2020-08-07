package org.heigit.ors.weightaugmentation;

import static org.heigit.ors.util.HelperFunctions.convertCoordinateArray;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.heigit.ors.exceptions.ParameterValueException;
import org.heigit.ors.geojson.GeometryJSON;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class UserWeightParserTest {
  private final GeometryFactory geometryFactory = new GeometryFactory();
  private UserWeightParser userWeightParser;
  private List<AugmentedWeight> weightAugmentations;
  private List<Geometry> geometries1;
  private List<Double> weights1;
  private List<Geometry> geometries2;
  private List<Double> weights2;
  String normalInputJson;

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Before
  public void setUp() throws Exception {
    userWeightParser = new UserWeightParser();
    normalInputJson = "{\"type\": \"FeatureCollection\", \"features\": [{\"type\": \"Feature\", \"properties\": {\"weight\": \"2.0\"}, \"geometry\": {\"type\": \"Polygon\", \"coordinates\": [[[8.691, 49.415], [8.691, 49.413], [8.699, 49.413], [8.691, 49.415]]] } }, { \"type\": \"Feature\", \"properties\": { \"weight\": 0.1 }, \"geometry\": { \"type\": \"Polygon\", \"coordinates\": [[[8.682, 49.413], [8.689, 49.413], [8.689, 49.419], [8.682, 49.419], [8.682, 49.413]], [[8.684, 49.418], [8.684, 49.414], [8.687, 49.414], [8.687, 49.418], [8.684, 49.418]]]}}]}";

    geometries1 = new ArrayList<>();
    geometries1.add(GeometryJSON.parse(new JSONObject("{\"type\": \"Polygon\", \"coordinates\": [[[8.691, 49.415], [8.691, 49.413], [8.699, 49.413], [8.691, 49.415]]]}")));
    geometries1.add(GeometryJSON.parse(new JSONObject("{\"type\": \"Polygon\", \"coordinates\": [[[8.682, 49.413], [8.689, 49.413], [8.689, 49.419], [8.682, 49.419], [8.682, 49.413]], [[8.684, 49.418], [8.684, 49.414], [8.687, 49.414], [8.687, 49.418], [8.684, 49.418]]]}")));
    weights1 = new ArrayList<>(Arrays.asList(2.0, 0.1));
    geometries2 = new ArrayList<>();
    geometries2.add(GeometryJSON.parse(new JSONObject("{\"type\": \"Polygon\", \"coordinates\": [[[8.680, 49.416], [8.664, 49.399], [8.692, 49.401], [8.680, 49.416]]]}")));
    weights2 = new ArrayList<>(Arrays.asList(1.9));
  }

  @Test
  public void testParse() throws ParameterValueException {
    weightAugmentations = userWeightParser.parse(normalInputJson);
    List<AugmentedWeight> expectedWeightAugmentations = new ArrayList<>();
    expectedWeightAugmentations.add(new AugmentedWeight(geometries1.get(0), weights1.get(0)));
    expectedWeightAugmentations.add(new AugmentedWeight(geometries1.get(1), weights1.get(1)));
    Assert.assertEquals(expectedWeightAugmentations, weightAugmentations);
  }

  @Test
  public void testAlternativeParse() throws ParameterValueException {
    Geometry[] geometries = new Geometry[]{geometries1.get(0), geometries1.get(1)};
    double[] weights = new double[]{weights1.get(0), weights1.get(1)};
    weightAugmentations = userWeightParser.parse(geometries, weights);
    for (int i = 0; i < weightAugmentations.size(); i++) {
      Assert.assertEquals(weightAugmentations.get(i).getGeometry(), geometries[i]);
      Assert.assertEquals(weightAugmentations.get(i).getWeight(), weights[i], 0.0);
    }
  }

  @Test
  public void testAlternativeParseWrongParameterSize() throws ParameterValueException {
    Geometry[] geometries = new Geometry[]{geometries1.get(0), geometries1.get(1)};
    double[] weights = new double[]{weights1.get(0)};
    thrown.expect(ParameterValueException.class);
    thrown.expectMessage("Parameter 'user_weights' has incorrect value of '2 != 1'. Given weights and geometries length not equal.");
    weightAugmentations = userWeightParser.parse(geometries, weights);
  }

  @Test
  public void testAddWeightAugmentations() throws Exception {
    weightAugmentations = userWeightParser.parse(normalInputJson);
    int sizeBefore = weightAugmentations.size();
    String geomJson = "{\"type\": \"Polygon\",\"coordinates\": [[[8.681,49.420],[8.685,49.420],[8.684,49.423],[8.681,49.420]]]}";
    Geometry geom = GeometryJSON.parse(new JSONObject(geomJson));
    userWeightParser.addWeightAugmentations(weightAugmentations, geom, 1.3);
    AugmentedWeight expectedResult = new AugmentedWeight(geom, 1.3);
    AugmentedWeight actualResult = weightAugmentations.get(weightAugmentations.size() - 1);
    Assert.assertEquals(expectedResult, actualResult);
    Assert.assertEquals(sizeBefore + 1, weightAugmentations.size());
  }

  @Test
  public void testAddWeightAugmentationsWrongGeometry() throws ParameterValueException {
    weightAugmentations = new ArrayList<>();
    Geometry geom = geometryFactory.createLinearRing(convertCoordinateArray(new double[][]{{8.680,49.416}, {8.664,49.399}, {8.692,49.401}, {8.680,49.416}}));
    thrown.expect(ParameterValueException.class);
    thrown.expectMessage("Parameter 'user_weights' has incorrect value of 'LinearRing'. Only these geometry types are currently implemented: Polygon");
    userWeightParser.addWeightAugmentations(weightAugmentations, geom, 1.2);
  }

  @Test
  public void testParseGeometry() throws ParameterValueException {
    String inputJson = "{\"type\": \"Polygon\", \"coordinates\": [[[8.680, 49.416], [8.664, 49.399], [8.692, 49.401], [8.680, 49.416]]]}";
    thrown.expect(ParameterValueException.class);
    thrown.expectMessage("Parameter 'user_weights' has incorrect value of 'Polygon'. Invalid GeoJSON type. Only 'FeatureCollection' or 'Feature' is allowed.");
    userWeightParser.parse(inputJson);
  }

  @Test
  public void testParseFeature() throws ParameterValueException {
    String inputJson = "{\"type\": \"Feature\", \"properties\": {\"weight\": 1.9}, \"geometry\": {\"type\": \"Polygon\", \"coordinates\": [[[8.680, 49.416], [8.664, 49.399], [8.692, 49.401], [8.680, 49.416]]]}}";
    weightAugmentations = userWeightParser.parse(inputJson);
    List<AugmentedWeight> expectedAugmentations = new ArrayList<>();
    expectedAugmentations.add(new AugmentedWeight(geometries2.get(0), weights2.get(0)));
    Assert.assertEquals(expectedAugmentations, weightAugmentations);
  }

  @Test
  public void testParseFeatureWithoutWeight() throws ParameterValueException {
    String inputJson = "{\"type\": \"Feature\", \"properties\": {}, \"geometry\": {\"type\": \"Polygon\", \"coordinates\": [[[8.680, 49.416], [8.664, 49.399], [8.692, 49.401], [8.680, 49.416]]]}}";
    thrown.expect(JSONException.class);
    thrown.expectMessage("JSONObject[\"weight\"] not found.");
    userWeightParser.parse(inputJson);
  }

  @Test
  public void testParseFeatureWithWrongWeightType0() throws ParameterValueException {
    testParseNotANumber("\"2..3\"");
  }

  @Test
  public void testParseFeatureWithWrongWeightType1() throws ParameterValueException {
    testParseNotANumber("foo");
  }

  @Test
  public void testParseFeatureWithWrongWeightType2() throws ParameterValueException {
    testParseNotANumber("null");
  }

  @Test
  public void testParseFeatureWithWrongWeightType3() throws ParameterValueException {
    testParseNotANumber( "\"foo\"");
  }

  @Test
  public void testParseFeatureWithWrongWeightType4() throws ParameterValueException {
    testParseNotANumber("\"\"");
  }

  private void testParseNotANumber(String weightString) throws ParameterValueException {
    String inputJson = "{\"type\": \"Feature\", \"properties\": {\"weight\": " + weightString + "}, \"geometry\": {\"type\": \"Polygon\", \"coordinates\": [[[8.680, 49.416], [8.664, 49.399], [8.692, 49.401], [8.680, 49.416]]]}}";
    thrown.expect(JSONException.class);
    thrown.expectMessage("JSONObject[\"weight\"] is not a number.");
    userWeightParser.parse(inputJson);
  }

  @Test
  public void testParseWrongGeometryType() throws ParameterValueException {
    String inputJson = "{\"type\": \"Feature\", \"properties\": {\"weight\": 1.0}, \"geometry\": {\"type\": \"LineString\", \"coordinates\": [[8.680, 49.416], [8.664, 49.399]]}}";
    thrown.expect(ParameterValueException.class);
    thrown.expectMessage("Parameter 'user_weights' has incorrect value of 'LineString'. Only these geometry types are currently implemented: Polygon");
    userWeightParser.parse(inputJson);
  }

  @Test
  public void testParseBrokenGeometry() throws ParameterValueException {
    String inputJson = "{\"type\": \"Feature\", \"properties\": {\"weight\": 1.0}, \"geometry\": {\"type\": \"LineString\", \"coordinates\": [[8.680, \"name\"], [8.664, 49.399]]}}";
    thrown.expect(ParameterValueException.class);
    thrown.expectMessage("Parameter 'user_weights' has incorrect value of");
    thrown.expectMessage("Geometry could not be parsed.");
    userWeightParser.parse(inputJson);
  }

  @Test
  public void testParseMissingGeometry() throws ParameterValueException {
    String inputJson = "{\"type\": \"Feature\", \"properties\": {\"weight\": 1.0}}";
    thrown.expect(JSONException.class);
    thrown.expectMessage("JSONObject[\"geometry\"] not found.");
    userWeightParser.parse(inputJson);
  }

  @Test
  public void testParseMissingProperties() throws ParameterValueException {
    String inputJson = "{\"type\": \"Feature\", \"geometry\": {\"type\": \"LineString\", \"coordinates\": [[8.680, 49.416], [8.664, 49.399]]}}";
    thrown.expect(JSONException.class);
    thrown.expectMessage("JSONObject[\"properties\"] not found.");
    userWeightParser.parse(inputJson);
  }

  @Test
  public void testParseMissingWeight() throws ParameterValueException {
    String inputJson = "{\"type\": \"Feature\", \"properties\": {}, \"geometry\": {\"type\": \"LineString\", \"coordinates\": [[8.680, 49.416], [8.664, 49.399]]}}";
    thrown.expect(JSONException.class);
    thrown.expectMessage("JSONObject[\"weight\"] not found.");
    userWeightParser.parse(inputJson);
  }

  @Test
  public void testParseFeatureCollection() throws ParameterValueException {
    String inputJson = "{\"type\": \"FeatureCollection\", \"features\": [{\"type\": \"Feature\", \"properties\": {\"weight\": 1.9}, \"geometry\": {\"type\": \"Polygon\", \"coordinates\": [[[8.680, 49.416], [8.664, 49.399], [8.692, 49.401], [8.680, 49.416]]]}}]}";
    weightAugmentations = userWeightParser.parse(inputJson);
    List<AugmentedWeight> expectedAugmentations = new ArrayList<>();
    expectedAugmentations.add(new AugmentedWeight(geometries2.get(0), weights2.get(0)));
    Assert.assertEquals(expectedAugmentations, weightAugmentations);
  }

  @Test
  public void testParseFeatureCollectionWithoutWeight() throws ParameterValueException {
    String inputJson = "{\"type\": \"FeatureCollection\", \"features\": [{\"type\": \"Feature\", \"properties\": {}, \"geometry\": {\"type\": \"Polygon\", \"coordinates\": [[[8.680, 49.416], [8.664, 49.399], [8.692, 49.401], [8.680, 49.416]]]}}]}";
    thrown.expect(JSONException.class);
    thrown.expectMessage("JSONObject[\"weight\"] not found.");
    userWeightParser.parse(inputJson);
  }
}