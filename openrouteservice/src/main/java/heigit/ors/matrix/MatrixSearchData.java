/*|----------------------------------------------------------------------------------------------
 *|														Heidelberg University
 *|	  _____ _____  _____      _                     	Department of Geography		
 *|	 / ____|_   _|/ ____|    (_)                    	Chair of GIScience
 *|	| |  __  | | | (___   ___ _  ___ _ __   ___ ___ 	(C) 2014-2017
 *|	| | |_ | | |  \___ \ / __| |/ _ \ '_ \ / __/ _ \	
 *|	| |__| |_| |_ ____) | (__| |  __/ | | | (_|  __/	Berliner Strasse 48								
 *|	 \_____|_____|_____/ \___|_|\___|_| |_|\___\___|	D-69120 Heidelberg, Germany	
 *|	        	                                       	http://www.giscience.uni-hd.de
 *|								
 *|----------------------------------------------------------------------------------------------*/
package heigit.ors.matrix;

public class MatrixSearchData {
	private int[] _nodeIds;
	private MatrixLocation[] _locations;
	private ClosestEdgeData[] _edges;

	public MatrixSearchData(int size, boolean resolveNames)
	{
		_nodeIds = new int[size];
		_locations = new MatrixLocation[size];
		_edges = new ClosestEdgeData[size];
	}

	public MatrixLocation[] getLocations()
	{
		return _locations;
	}
	
	public int size()
	{
		return _nodeIds.length;
	}

	public int[] getNodeIds()
	{
		return _nodeIds;
	}
	
	public int getNodeId(int index)
	{
		return _nodeIds[index];
	}
	
	public ClosestEdgeData getNearestEdge(int index)
	{
		return _edges[index];
	}

	public void setData(int index, int nodeId, MatrixLocation location, ClosestEdgeData edge)
	{
		_nodeIds[index] = nodeId;
		_locations[index] = location;
		_edges[index] = edge;
	}
}
