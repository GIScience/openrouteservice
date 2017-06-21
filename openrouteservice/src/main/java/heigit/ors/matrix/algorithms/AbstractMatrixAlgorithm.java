package heigit.ors.matrix.algorithms;

import com.graphhopper.GraphHopper;
import com.graphhopper.routing.util.FlagEncoder;

public abstract class AbstractMatrixAlgorithm implements MatrixAlgorithm {
  protected GraphHopper _graphHopper;
  protected FlagEncoder _encoder;
  
  public void init(GraphHopper gh, FlagEncoder encoder)
  {
	  _graphHopper = gh;
	  _encoder = encoder;
  }
}
