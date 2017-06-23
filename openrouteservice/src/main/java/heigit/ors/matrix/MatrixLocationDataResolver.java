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

import java.util.HashMap;
import java.util.Map;

import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.storage.index.LocationIndex;
import com.graphhopper.storage.index.QueryResult;
import com.graphhopper.util.ByteArrayBuffer;
import com.graphhopper.util.shapes.GHPoint3D;
import com.vividsolutions.jts.geom.Coordinate;

public class MatrixLocationDataResolver {
   private Map<Coordinate, LocationData> _locationCache;
   private boolean _resolveNames;
   private LocationIndex _locIndex;
   private EdgeFilter _edgeFilter;
   private ByteArrayBuffer _buffer;
   
   class LocationData
   {
	   public Coordinate coordinate;
	   public int nodeId;
	   public String name;
   }
   
   public MatrixLocationDataResolver(LocationIndex index, EdgeFilter edgeFilter, ByteArrayBuffer buffer, boolean resolveNames)
   {
	   _locIndex = index;
	   _edgeFilter = edgeFilter;
	   _buffer = buffer;
	   _resolveNames = resolveNames;
   }
   
   public MatrixLocationData resolve(Coordinate[] coords)
   {
	   if (_locationCache == null)
		   _locationCache = new HashMap<Coordinate, LocationData>();
	   
	   MatrixLocationData mld = new MatrixLocationData(coords.length, _resolveNames);

		Coordinate p = null;
		for (int i = 0; i < coords.length; i++)
		{
			p = coords[i];

			LocationData ld = _locationCache.get(p);
			if (ld != null)
				mld.setData(i, ld.coordinate, ld.nodeId, ld.name);
			else
			{  
				ld = new LocationData();
				
				QueryResult qr = _locIndex.findClosest(p.y, p.x, _edgeFilter, _buffer);
				if (qr.isValid())
				{
					ld.nodeId = qr.getClosestNode();
					GHPoint3D pt = qr.getSnappedPoint();
					ld.coordinate = new Coordinate(pt.getLon(), pt.getLat());
					if (_resolveNames)
						ld.name = qr.getClosestEdge().getName();
				}
				else
				{
					ld.nodeId = -1;
					ld.coordinate = null;
				}
				
				_locationCache.put(p, ld);
				mld.setData(i, ld.coordinate, ld.nodeId, ld.name);
			}
		}
	   return mld;
   }
}
