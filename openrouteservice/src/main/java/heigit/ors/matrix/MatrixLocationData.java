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

import com.graphhopper.storage.index.QueryResult;
import com.vividsolutions.jts.geom.Coordinate;

public class MatrixLocationData {
	private int[] _nodeIds;
	private String[] _names;
	private Coordinate[] _coords;
	private double[] _distanceToNodes;
	private QueryResult[] _queryResults;

	public MatrixLocationData(int size, boolean resolveNames)
	{
		_nodeIds = new int[size];
		_distanceToNodes = new double[size];
		_coords = new Coordinate[size];
		if (resolveNames)
			_names = new String[size];
	}
	
	public double getDistanceToNode(int index)
	{
		return _distanceToNodes[index];
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

	public String[] getNames()
	{
		return _names;
	}

	public Coordinate[] getCoordinates()
	{
		return _coords;
	}
	
	public void setData(int index, Coordinate coord, int nodeId, double distance, String name)
	{
		_nodeIds[index] = nodeId;
		_coords[index] = coord;
		_distanceToNodes[index] = distance;
		
		if (_names != null)
			_names[index] = name; 
	}
}
