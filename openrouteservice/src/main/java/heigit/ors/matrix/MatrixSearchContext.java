package heigit.ors.matrix;

import com.graphhopper.storage.Graph;

public class MatrixSearchContext {
	private Graph _graph;
	private MatrixLocations _sources;
	private MatrixLocations _destinations;

	public MatrixSearchContext(Graph graph, MatrixLocations sources, MatrixLocations destinations)
	{
		_graph = graph;
		_sources = sources;
		_destinations = destinations;
	}
	
	public Graph getGraph()
	{
		return _graph;
	}
	
	public MatrixLocations getSources()
	{
		return _sources;
	}
	
	public MatrixLocations getDestinations()
	{
		return _destinations;
	}
}
