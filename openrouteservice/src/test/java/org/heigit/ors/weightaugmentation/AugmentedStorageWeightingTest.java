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
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Polygon;
import java.util.ArrayList;
import java.util.Collections;
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
  private AugmentedStorageWeighting augmentedStorageWeighting;
  private final CarFlagEncoder carEncoder = new CarFlagEncoder();
  private final EncodingManager encodingManager = EncodingManager.create(carEncoder);
  private ORSGraphHopper graphHopper;
  private final Map<Integer, Double> expectedAugmentations = new HashMap<Integer, Double>(){
    {put(0, 0.75);};
    {put(1, 0.75);};
    {put(2, 0.75);};
    {put(3, 0.75);};
    {put(4, 0.75);};
    {put(5, 1.0);};
    {put(6, 0.75);};
    {put(7, 0.75);};
    {put(8, 1.0);};
    {put(9, 1.0);};
    {put(10, 1.0);};
    {put(11, 1.0);};
    {put(12, 1.0);};
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
    Coordinate[] coordinates = convertCoordinateArray(new double[][]{{1,2},{1,5},{3,5},{3,2},{1,2}});
    double weight = 0.75;
    GeometryFactory geometryFactory = new GeometryFactory();
    Polygon polygon = geometryFactory.createPolygon(coordinates);
    return new ArrayList<>(Collections.singletonList(new AugmentedWeight(polygon, weight)));
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

    augmentedStorageWeighting = new AugmentedStorageWeighting(additionalHints, superWeighting, graphHopper, 1, 1000);
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