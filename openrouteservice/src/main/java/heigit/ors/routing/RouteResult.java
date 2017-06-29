/*|----------------------------------------------------------------------------------------------
 *|														Heidelberg University
 *|	  _____ _____  _____      _                     	Department of Geography		
 *|	 / ____|_   _|/ ____|    (_)                    	Chair of GIScience
 *|	| |  __  | | | (___   ___ _  ___ _ __   ___ ___ 	(C) 2014-2016
 *|	| | |_ | | |  \___ \ / __| |/ _ \ '_ \ / __/ _ \	
 *|	| |__| |_| |_ ____) | (__| |  __/ | | | (_|  __/	Berliner Strasse 48								
 *|	 \_____|_____|_____/ \___|_|\___|_| |_|\___\___|	D-69120 Heidelberg, Germany	
 *|	        	                                       	http://www.giscience.uni-hd.de
 *|								
 *|----------------------------------------------------------------------------------------------*/
package heigit.ors.routing;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.graphhopper.util.PointList;
import com.vividsolutions.jts.geom.Coordinate;

public class RouteResult 
{
	private RouteSummary _summary;
	private Coordinate[] _geometry;
	private List<RouteSegment> _segments;
	private List<RouteExtraInfo> _extraInfo;
	private int[] _wayPointsIndices = null;
	private int _locationIndex = 0;

	public RouteResult(int routeExtras) throws Exception
	{
		_segments = new ArrayList<RouteSegment>();
		_summary = new RouteSummary();
		
		if (routeExtras != 0)
			_extraInfo = new ArrayList<RouteExtraInfo>();
	}

	public List<RouteSegment> getSegments()
	{
		return _segments;
	}

	public RouteSummary getSummary()
	{
		return _summary;
	}

	public Coordinate[] getGeometry() {
		return _geometry;
	}

	public void addPoints(PointList points, boolean skipFirstPoint, boolean includeElevation)
	{
		int index = skipFirstPoint ? 1 : 0;
		
		if (_geometry == null)
		{
			int newSize = points.size() - index;
			_geometry = new Coordinate[newSize];

			if (includeElevation && points.is3D())
			{
				for (int i = index; i < newSize; ++i)
					_geometry[i] = new Coordinate(points.getLon(i), points.getLat(i), points.getEle(i));
			}
			else
			{
				for (int i= index; i < newSize; ++i)
					_geometry[i] = new Coordinate(points.getLon(i), points.getLat(i));
			}
		}
		else
		{
			int oldSize = _geometry.length;
			int pointsSize = points.size() - index;
			int newSize = oldSize + pointsSize;
			Coordinate[] coords = new Coordinate[newSize];

			for (int i = 0; i < oldSize; i++)
				coords[i] = _geometry[i];

			if (includeElevation && points.is3D())
			{
				for (int i = 0; i < pointsSize; ++i)
				{
					int j = i + index; 
					coords[oldSize + i] = new Coordinate(points.getLon(j), points.getLat(j), points.getEle(j));
				}
			}
			else
			{
				for (int i = 0; i < pointsSize; ++i)
				{
					int j = i + index; 
					coords[oldSize + i] = new Coordinate(points.getLon(j), points.getLat(j));
				}
			}
			
			_geometry = coords;
		}
	}

	
	public List<RouteExtraInfo> getExtraInfo() 
	{
		return _extraInfo;
	}
	
	public void addExtraInfo(RouteExtraInfo info)
	{
		_extraInfo.add(info);
	}
	
	public void addExtraInfo(Collection<RouteExtraInfo> infos)
	{
		_extraInfo.addAll(infos);
	}

	public int[] getWayPointsIndices() {
		return _wayPointsIndices;
	}

	public void setWayPointsIndices(int[] wayPointsIndices) {
		_wayPointsIndices = wayPointsIndices;
	}

	public int getLocationIndex() {
		return _locationIndex;
	}

	public void setLocationIndex(int locationIndex) {
		_locationIndex = locationIndex;
	}
}
