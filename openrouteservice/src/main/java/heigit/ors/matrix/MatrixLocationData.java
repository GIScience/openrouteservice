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

import com.vividsolutions.jts.geom.Coordinate;

public class MatrixLocationData {
	private int[] _nodeIds;
	private String[] _names;
	private Coordinate[] _coords;

	public MatrixLocationData(int size, boolean resolveNames)
	{
		_nodeIds = new int[size];
		_coords = new Coordinate[size];
		if (resolveNames)
			_names = new String[size];
	}
	
	public int getSize()
	{
		return _nodeIds.length;
	}

	public int[] getNodeIds()
	{
		return _nodeIds;
	}

	public String[] getNames()
	{
		return _names;
	}

	public Coordinate[] getCoordinates()
	{
		return _coords;
	}
	
	public void setData(int index, Coordinate coord, int nodeId, String name)
	{
		_nodeIds[index] = nodeId;
		_coords[index] = coord;
		
		if (_names != null)
			_names[index] = name; 
	}
}
