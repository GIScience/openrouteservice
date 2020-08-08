package org.heigit.ors.weightaugmentation;

import static org.heigit.ors.util.HelperFunctions.convertCoordinateArray;

import com.graphhopper.routing.util.AllEdgesIterator;
import com.graphhopper.routing.util.CarFlagEncoder;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.routing.util.HintsMap;
import com.graphhopper.routing.weighting.FastestWeighting;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.GraphBuilder;
import com.graphhopper.storage.GraphHopperStorage;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.heigit.ors.exceptions.ParameterValueException;
import org.heigit.ors.routing.graphhopper.extensions.ORSGraphHopper;
import org.heigit.ors.routing.graphhopper.extensions.util.ORSPMap;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class AugmentedStorageWeightingTest {
  private final GeometryFactory geometryFactory = new GeometryFactory();
  private AugmentedStorageWeighting augmentedStorageWeighting;
  private final CarFlagEncoder carEncoder = new CarFlagEncoder();
  private final EncodingManager encodingManager = EncodingManager.create(carEncoder);
  private ORSGraphHopper graphHopper;
  private final Map<Integer, Double> expectedAugmentations = new HashMap<Integer, Double>(){
    {put(0,  1.0 * 0.75);};
    {put(1,  1.0 * 0.75);};
    {put(2,  1.0 * 0.75);};
    {put(3,  1.0 * 0.75 * 1.2);};
    {put(4,  1.0 * 0.75 * 1.4);};
    {put(5,  1.0 * 1.1 * 1.2);};
    {put(6,  1.0 * 0.75);};
    {put(7,  1.0 * 0.75);};
    {put(8,  1.0 * 1.2 * 1.3);};
    {put(9,  1.0 * 1.3);};
    {put(10, 1.0 * 1.2);};
    {put(11, 1.0 * 1.4);};
    {put(12, 1.0 * 1.5 * 1.2);};
  };
  private final Weighting superWeighting = new FastestWeighting(carEncoder, new HintsMap());

  private GraphHopperStorage createGHStorage() {
    return new GraphBuilder(encodingManager).create();
  }

  public GraphHopperStorage createMediumGraph() {
    //    3---4--5
    //   /\   |  |
    //  2--0  6--7
    //  | / \   /
    //  |/   \ /
    //  1-----8
    GraphHopperStorage g = createGHStorage();
    g.edge(0, 1, 1, true);
    g.edge(0, 2, 1, true);
    g.edge(0, 3, 5, true);
    g.edge(0, 8, 1, true);
    g.edge(1, 2, 1, true);
    g.edge(1, 8, 2, true);
    g.edge(2, 3, 2, true);
    g.edge(3, 4, 2, true);
    g.edge(4, 5, 1, true);
    g.edge(4, 6, 1, true);
    g.edge(5, 7, 1, true);
    g.edge(6, 7, 2, true);
    g.edge(7, 8, 3, true);
    //Set test lat lon
    g.getBaseGraph().getNodeAccess().setNode(0, 3, 3);
    g.getBaseGraph().getNodeAccess().setNode(1, 1, 1);
    g.getBaseGraph().getNodeAccess().setNode(2, 3, 1);
    g.getBaseGraph().getNodeAccess().setNode(3, 4, 2);
    g.getBaseGraph().getNodeAccess().setNode(4, 4, 4);
    g.getBaseGraph().getNodeAccess().setNode(5, 4, 5);
    g.getBaseGraph().getNodeAccess().setNode(6, 3, 4);
    g.getBaseGraph().getNodeAccess().setNode(7, 3, 5);
    g.getBaseGraph().getNodeAccess().setNode(8, 1, 4);
    return g;
  }

  private List<AugmentedWeight> createAugmentedWeightList() throws ParameterValueException {
    // polygon
    // expected edges: 0-4, 6-7
    Coordinate[] polygonCoordinates = convertCoordinateArray(new double[][]{{1,2},{1,5},{3,5},{3,2},{1,2}});
    double polygonWeight = 0.75;
    Polygon polygon = geometryFactory.createPolygon(polygonCoordinates);

    // point
    // expected edges: 12
    double pointWeight = 1.5;
    Point point = geometryFactory.createPoint(new Coordinate(4.5, 2.0));

    // lineString
    // expected edges: 5
    double lineStringWeight = 1.1;
    Coordinate[] lineStringCoordinates = convertCoordinateArray(new double[][]{{1,1},{4,1}});
    LineString lineString = geometryFactory.createLineString(lineStringCoordinates);

    // multiPolygon
    // expected edges: 3, 5, 8, 10, 12
    double multiPolygonWeight = 1.2;
    Polygon[] polygons = new Polygon[]{
        geometryFactory.createPolygon(convertCoordinateArray(new double[][]{{3.5,0.5},{4.0,1.5},{4.5,0.5},{3.5,0.5}})),
        geometryFactory.createPolygon(convertCoordinateArray(new double[][]{{4.5,3.5},{5.0,4.5},{5.5,3.5},{4.5,3.5}}))
    };
    MultiPolygon multiPolygon = geometryFactory.createMultiPolygon(polygons);

    // multiPoint
    // expected edges: 8, 9
    double multiPointWeight = 1.3;
    Point[] points = new Point[]{
        geometryFactory.createPoint(new Coordinate(4.5, 4.5)),
        geometryFactory.createPoint(new Coordinate(3.99, 3.5))
    };
    MultiPoint multiPoint = geometryFactory.createMultiPoint(points);

    // multiLineString
    // expected edges: 4, 11
    double multiLineStringWeight = 1.4;
    LineString[] lineStrings = new LineString[]{
        geometryFactory.createLineString(convertCoordinateArray(new double[][]{{1,1},{1,3}})),
        geometryFactory.createLineString(convertCoordinateArray(new double[][]{{4,3},{5,3}}))
    };
    MultiLineString multiLineString = geometryFactory.createMultiLineString(lineStrings);

    // assemble geometries and parse
    Geometry[] geometries = new Geometry[]{polygon, point, lineString, multiPolygon, multiPoint, multiLineString};
    double[] weights = new double[]{polygonWeight, pointWeight, lineStringWeight, multiPolygonWeight, multiPointWeight, multiLineStringWeight};
    return new UserWeightParser().parse(geometries, weights);
  }

  @Before
  public void setUp() throws ParameterValueException {
    graphHopper = new ORSGraphHopper();
    graphHopper.setCHEnabled(false);
    graphHopper.setCoreEnabled(false);
    graphHopper.setCoreLMEnabled(false);
    graphHopper.setEncodingManager(encodingManager);
    graphHopper.setGraphHopperStorage(createMediumGraph());
    graphHopper.postProcessing();

    ORSPMap additionalHints = new ORSPMap();
    additionalHints.putObj("user_weights", createAugmentedWeightList());

    augmentedStorageWeighting = new AugmentedStorageWeighting(additionalHints, superWeighting, graphHopper, .1, 1000);
  }

  @Test
  public void getAugmentations() {
    AllEdgesIterator allEdges = graphHopper.getGraphHopperStorage().getAllEdges();
    Map<Integer, Double> actualAugmentations = new HashMap<>();
    while (allEdges.next()) {
      double weight = augmentedStorageWeighting.getAugmentations(allEdges);
      actualAugmentations.put(allEdges.getEdge(), weight);
    }

    Assert.assertEquals(expectedAugmentations, actualAugmentations);
  }

  @Test
  public void calcWeight() {
    AllEdgesIterator allEdges = graphHopper.getGraphHopperStorage().getAllEdges();
    Map<Integer, Double> actualWeights = new HashMap<>();
    Map<Integer, Double> expectedWeights = new HashMap<>();
    while (allEdges.next()) {
      double weight = augmentedStorageWeighting.calcWeight(allEdges, true, 0);
      actualWeights.put(allEdges.getEdge(), weight);
      expectedWeights.put(allEdges.getEdge(), expectedAugmentations.get(allEdges.getEdge()) * superWeighting.calcWeight(allEdges, true, 0));
    }
    Assert.assertEquals(expectedWeights, actualWeights);
  }

  @Test
  public void calcMillis() {
    AllEdgesIterator allEdges = graphHopper.getGraphHopperStorage().getAllEdges();
    Map<Integer, Long> actualMillis = new HashMap<>();
    Map<Integer, Long> expectedMillis = new HashMap<>();
    while (allEdges.next()) {
      long millis = augmentedStorageWeighting.calcMillis(allEdges, true, 0);
      actualMillis.put(allEdges.getEdge(), millis);
      expectedMillis.put(allEdges.getEdge(), (long) (expectedAugmentations.get(allEdges.getEdge()) * (double) superWeighting.calcMillis(allEdges, true, 0)));
    }
    Assert.assertEquals(expectedMillis, actualMillis);
  }

  @Test
  public void getMinWeight() {
    double distance = 3.5;
    double expectedMinWeight = superWeighting.getMinWeight(distance) * 0.75;
    double actualMinWeight = augmentedStorageWeighting.getMinWeight(distance);
    Assert.assertEquals(expectedMinWeight, actualMinWeight, 0.0);
  }

  @Test
  public void getFlagEncoder() {
    Assert.assertEquals(superWeighting.getFlagEncoder(), augmentedStorageWeighting.getFlagEncoder());
  }

  @Test
  public void matches() {
    HintsMap hintsMap = new HintsMap();
    Assert.assertEquals(superWeighting.matches(hintsMap), augmentedStorageWeighting.matches(hintsMap));
  }

  @Test
  public void testToString() {
    Assert.assertEquals("augmented|" + superWeighting.toString(), augmentedStorageWeighting.toString());
  }

  @Test
  public void getName() {
    Assert.assertEquals("augmented|" + superWeighting.getName(), augmentedStorageWeighting.getName());
  }
}