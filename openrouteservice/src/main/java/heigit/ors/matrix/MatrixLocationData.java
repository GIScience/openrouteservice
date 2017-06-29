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

import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.storage.index.LocationIndex;
import com.graphhopper.storage.index.QueryResult;
import com.graphhopper.util.ArrayBuffer;
import com.graphhopper.util.shapes.GHPoint3D;
import com.vividsolutions.jts.geom.Coordinate;

public class MatrixLocationData {
	private int[] _nodeIds;
	private String[] _names;
	private Coordinate[] _coords;

	private MatrixLocationData(int size, boolean resolveNames)
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

	public static MatrixLocationData createData(LocationIndex index, Coordinate[] coords, EdgeFilter edgeFilter, ArrayBuffer buffer, boolean resolveNames)
	{
		MatrixLocationData mld = new MatrixLocationData(coords.length, resolveNames);

		Coordinate p = null;
		for (int i = 0; i < coords.length; i++)
		{
			p = coords[i];

			QueryResult qr = index.findClosest(p.y, p.x, edgeFilter, buffer);
			if (qr.isValid())
			{
				mld._nodeIds[i] = qr.getClosestNode();
				GHPoint3D pt = qr.getSnappedPoint();
				mld._coords[i] = new Coordinate(pt.getLon(), pt.getLat());
				
				if (resolveNames)
				{
					mld._names[i] = qr.getClosestEdge().getName();
				}
			}
			else
			{
				mld._nodeIds[i] = -1;
				mld._coords[i] = null;
			}
		}

		return mld;
	}
}
