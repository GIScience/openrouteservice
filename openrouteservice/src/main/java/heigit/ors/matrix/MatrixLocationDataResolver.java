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
		public double distanceToNode;
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

		DistanceCalc distCalc = Helper.DIST_EARTH;
		Coordinate p = null;
		for (int i = 0; i < coords.length; i++)
		{
			p = coords[i];

			LocationData ld = _locationCache.get(p);
			if (ld != null)
				mld.setData(i, ld.coordinate, ld.nodeId, ld.distanceToNode, ld.name);
			else
			{  
				ld = new LocationData();

				QueryResult qr = _locIndex.findClosest(p.y, p.x, _edgeFilter, _buffer);
				if (qr.isValid())
				{
					EdgeIteratorState closestEdge = qr.getClosestEdge();
					if (closestEdge == null)
					{
						ld.nodeId = -1;
						ld.coordinate = null;
						continue;
					}

					double distToNode = 0.0;

					// compute distance from snapped point to nearest node
					if (qr.getSnappedPosition() != QueryResult.Position.PILLAR)
					{
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
						int len = pointList.getSize();

						double lat1, lon1, lat0 = pointList.getLat(0), lon0 = pointList.getLon(0);
						double qLon = qr.getSnappedPoint().getLon();
						double qLat = qr.getSnappedPoint().getLat();

						double totalDist = 0.0;
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

					ld.nodeId = qr.getClosestNode();
					GHPoint3D pt = qr.getSnappedPoint();
					ld.coordinate = new Coordinate(pt.getLon(), pt.getLat());
					if (_resolveNames)
						ld.name = qr.getClosestEdge().getName();
					ld.distanceToNode = distToNode;
				}
				else
				{
					ld.nodeId = -1;
					ld.coordinate = null;
				}

				_locationCache.put(p, ld);
				mld.setData(i, ld.coordinate, ld.nodeId, ld.distanceToNode, ld.name);
			}
		}

		return mld;
	}
}
