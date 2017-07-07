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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.graphhopper.routing.QueryGraph;
import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.storage.Graph;
import com.graphhopper.storage.index.LocationIndex;
import com.graphhopper.storage.index.QueryResult;
import com.graphhopper.util.ByteArrayBuffer;
import com.graphhopper.util.shapes.GHPoint3D;
import com.vividsolutions.jts.geom.Coordinate;

public class MatrixSearchContextBuilder {
	private Map<Coordinate, LocationEntry> _locationCache;
	private boolean _resolveNames;
	private LocationIndex _locIndex;
	private EdgeFilter _edgeFilter;
	private ByteArrayBuffer _buffer;

	class LocationEntry
	{
		public int nodeId;
		public ResolvedLocation location;
	}

	public MatrixSearchContextBuilder(LocationIndex index, EdgeFilter edgeFilter, ByteArrayBuffer buffer, boolean resolveNames)
	{
		_locIndex = index;
		_edgeFilter = edgeFilter;
		_buffer = buffer;
		_resolveNames = resolveNames;
	}

	public MatrixSearchContext create(Graph graph, Coordinate[] sources, Coordinate[] destinations, double maxSearchRadius)
	{
		if (_locationCache == null)
			_locationCache = new HashMap<Coordinate, LocationEntry>();
	
		QueryGraph queryGraph = new QueryGraph(graph);
		List<QueryResult> queryResults = new ArrayList<QueryResult>(sources.length + destinations.length);
		
		MatrixLocations mlSources = createLocations(sources, queryResults, maxSearchRadius);
		MatrixLocations mlDestinations = createLocations(destinations, queryResults, maxSearchRadius);

		queryGraph.lookup(queryResults, _buffer);
		
		return new  MatrixSearchContext(queryGraph, mlSources, mlDestinations);
	}
	
	private MatrixLocations createLocations(Coordinate[] coords, List<QueryResult> queryResults, double maxSearchRadius)
	{
		MatrixLocations mlRes = new MatrixLocations(coords.length, _resolveNames);
		
		Coordinate p = null;
		
		for (int i = 0; i < coords.length; i++)
		{
			p = coords[i];

			LocationEntry ld = _locationCache.get(p);
			if (ld != null)
				mlRes.setData(i, ld.nodeId, ld.location);
			else
			{  
				ld = new LocationEntry();

				QueryResult qr = _locIndex.findClosest(p.y, p.x, _edgeFilter, _buffer);
				
				if (qr.isValid() && qr.getQueryDistance() < maxSearchRadius)
				{
					GHPoint3D pt = qr.getSnappedPoint();
					ld.nodeId = qr.getClosestNode();
					ld.location = new ResolvedLocation(new Coordinate(pt.getLon(), pt.getLat()), _resolveNames ? qr.getClosestEdge().getName(): null, qr.getQueryDistance());
				}
				else
				{
					ld.nodeId = -1;
				}

				_locationCache.put(p, ld);
				
				mlRes.setData(i, ld.nodeId, ld.location);
			}
		}
		
		return mlRes;
	}
}
