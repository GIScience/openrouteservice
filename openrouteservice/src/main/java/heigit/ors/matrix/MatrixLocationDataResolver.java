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
import com.graphhopper.util.DistanceCalc;
import com.graphhopper.util.EdgeIteratorState;
import com.graphhopper.util.Helper;
import com.graphhopper.util.PointList;
import com.graphhopper.util.shapes.GHPoint3D;
import com.vividsolutions.jts.geom.Coordinate;

public class MatrixLocationDataResolver {
	private Map<Coordinate, LocationEntry> _locationCache;
	private boolean _resolveNames;
	private LocationIndex _locIndex;
	private EdgeFilter _edgeFilter;
	private ByteArrayBuffer _buffer;

	class LocationEntry
	{
		public int nodeId;
		public MatrixLocation location;
		public ClosestEdgeData edge;
	}

	public MatrixLocationDataResolver(LocationIndex index, EdgeFilter edgeFilter, ByteArrayBuffer buffer, boolean resolveNames)
	{
		_locIndex = index;
		_edgeFilter = edgeFilter;
		_buffer = buffer;
		_resolveNames = resolveNames;
	}

	public MatrixSearchData resolve(Coordinate[] coords)
	{
		if (_locationCache == null)
			_locationCache = new HashMap<Coordinate, LocationEntry>();

		MatrixSearchData mld = new MatrixSearchData(coords.length, _resolveNames);

		DistanceCalc distCalc = Helper.DIST_EARTH;
		Coordinate p = null;
		
		for (int i = 0; i < coords.length; i++)
		{
			p = coords[i];

			LocationEntry ld = _locationCache.get(p);
			if (ld != null)
				mld.setData(i, ld.nodeId, ld.location, ld.edge);
			else
			{  
				ld = new LocationEntry();

				QueryResult qr = _locIndex.findClosest(p.y, p.x, _edgeFilter, _buffer);
				
				if (qr.isValid())
				{
					EdgeIteratorState closestEdge = qr.getClosestEdge();
					if (closestEdge == null)
					{
						ld.nodeId = -1;
						continue;
					}

					double distToNode = 0.0;
					double totalDist = 0.0;

					int base = closestEdge.getBaseNode();
					// Force the identical direction for all closest edges.
					// It is important to sort multiple results for the same edge by its wayIndex
					boolean doReverse = base > closestEdge.getAdjNode();
					if (base == closestEdge.getAdjNode()) {
						// check for special case #162 where adj == base and force direction via latitude comparison
						PointList pl = closestEdge.fetchWayGeometry(0, _buffer);
						if (pl.size() > 1)
							doReverse = pl.getLatitude(0) > pl.getLatitude(pl.size() - 1);
					}

					if (doReverse) 
						closestEdge = closestEdge.detach(true);

					PointList pointList = closestEdge.fetchWayGeometry(3, _buffer);
					
					if (qr.getSnappedPosition() != QueryResult.Position.PILLAR)
					{
						int len = pointList.getSize();

						double lat1, lon1, lat0 = pointList.getLat(0), lon0 = pointList.getLon(0);
						double qLon = qr.getSnappedPoint().getLon();
						double qLat = qr.getSnappedPoint().getLat();

						double distToSnappedPoint = distCalc.calcDist(lat0, lon0, qLat, qLon);
						distToNode = distToSnappedPoint;

						for (int pointIndex = 1; pointIndex < len; pointIndex++) {
							lat1 = pointList.getLatitude(pointIndex);
							lon1 = pointList.getLongitude(pointIndex);
							if (distCalc.isCrossBoundary(lon1, lat1)) 
								continue;

							distToSnappedPoint = distCalc.calcDist(lat1, lon1, qLat, qLon);
							if (distToSnappedPoint < distToNode)
								distToNode = totalDist + distToSnappedPoint;

							totalDist += distCalc.calcDist(lat0, lon0, lat1, lon1);

							lon0 = lon1;
							lat0 = lat1;
						}
					}
					else
					{
						totalDist = pointList.calcDistance(distCalc);
					}
					
					ClosestEdgeData ned = new ClosestEdgeData();
					ned.edgeState = closestEdge;
					ned.distanceFromNode = distToNode;
					ned.distanceToNode = totalDist - distToNode;
					ned.nodeId = qr.getClosestNode();

					GHPoint3D pt = qr.getSnappedPoint();
					ld.nodeId = qr.getClosestNode();
					ld.location = new MatrixLocation(new Coordinate(pt.getLon(), pt.getLat()), _resolveNames? qr.getClosestEdge().getName(): null, qr.getQueryDistance());
					ld.edge = ned;
				}
				else
				{
					ld.nodeId = -1;
				}

				_locationCache.put(p, ld);
				
				mld.setData(i, ld.nodeId, ld.location, ld.edge);
			}
		}

		return mld;
	}
}
