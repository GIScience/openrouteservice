package heigit.ors.matrix.algorithms;

import com.graphhopper.GraphHopper;
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.Graph;

import heigit.ors.matrix.MatrixRequest;

public abstract class AbstractMatrixAlgorithm implements MatrixAlgorithm {
  protected GraphHopper _graphHopper;
  protected Graph _graph;
  protected FlagEncoder _encoder;
  protected Weighting _weighting;
  
  public void init(MatrixRequest req, GraphHopper gh, Graph graph, FlagEncoder encoder, Weighting weighting)
  {
	  _graphHopper = gh;
	  _graph = graph;
	  _encoder = encoder;
	  _weighting = weighting;
  }
}
