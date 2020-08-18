package org.heigit.ors.weightaugmentation;

import static org.heigit.ors.util.HelperFunctions.convertCoordinateArray;

import com.graphhopper.routing.util.CarFlagEncoder;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.routing.util.HintsMap;
import com.graphhopper.routing.weighting.FastestWeighting;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.GraphBuilder;
import com.graphhopper.storage.GraphHopperStorage;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import java.util.ArrayList;
import java.util.List;
import org.heigit.ors.exceptions.AugmentationStorageException;
import org.heigit.ors.exceptions.ParameterValueException;
import org.heigit.ors.routing.graphhopper.extensions.ORSGraphHopper;
import org.heigit.ors.routing.graphhopper.extensions.util.ORSPMap;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class UserWeightFactoryTest {
  private final GeometryFactory geometryFactory = new GeometryFactory();
  private final CarFlagEncoder carEncoder = new CarFlagEncoder();
  private final EncodingManager encodingManager = EncodingManager.create(carEncoder);
  private ORSGraphHopper graphHopper;
  private ORSPMap additionalHints;
  private UserWeightFactory userWeightFactory;

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

  @Before
  public void setUp() throws ParameterValueException, AugmentationStorageException {
    graphHopper = new ORSGraphHopper();
    graphHopper.setCHEnabled(false);
    graphHopper.setCoreEnabled(false);
    graphHopper.setCoreLMEnabled(false);
    graphHopper.setEncodingManager(encodingManager);
    graphHopper.setGraphHopperStorage(createMediumGraph());
    graphHopper.postProcessing();

    additionalHints = new ORSPMap();
    additionalHints.putObj("user_weights", new ArrayList<AugmentedWeight>());

    userWeightFactory = new UserWeightFactory(additionalHints, graphHopper, .1, 1000);
  }

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Test
  public void getStorage() {
    Assert.assertEquals(new AugmentationStorage(), userWeightFactory.getStorage());
  }

  @Test
  public void getWeighting() {
    Weighting superWeighting = new FastestWeighting(carEncoder, new HintsMap());
    Assert.assertEquals(new AugmentedWeighting(superWeighting, userWeightFactory.getStorage()), userWeightFactory.getWeighting(superWeighting));
  }

  @Test
  public void unsupportedGeometry() throws ParameterValueException, AugmentationStorageException {
    LinearRing linearRing = geometryFactory.createLinearRing(convertCoordinateArray(new double[][]{{4.5,2.5},{4.5,4.5},{5.5,4.5},{5.5,2.5},{4.5,2.5}}));
    List<AugmentedWeight> augmentedWeights = new ArrayList<>();
    augmentedWeights.add(new AugmentedWeight(linearRing, 0.85));
    additionalHints.putObj("user_weights", augmentedWeights);

    thrown.expect(UnsupportedOperationException.class);
    thrown.expectMessage("AugmentationStorage is not implemented for LinearRing");
    new UserWeightFactory(additionalHints, graphHopper);
  }
}