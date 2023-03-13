/*  This file is part of Openrouteservice.
 *
 *  Openrouteservice is free software; you can redistribute it and/or modify it under the terms of the 
 *  GNU Lesser General Public License as published by the Free Software Foundation; either version 2.1 
 *  of the License, or (at your option) any later version.

 *  This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details.

 *  You should have received a copy of the GNU Lesser General Public License along with this library; 
 *  if not, see <https://www.gnu.org/licenses/>.  
 */
package org.heigit.ors.routing;

import com.graphhopper.ResponsePath;
import com.graphhopper.util.PointList;
import org.locationtech.jts.geom.Coordinate;
import org.heigit.ors.common.DistanceUnit;
import org.heigit.ors.util.FormatUtility;
import org.heigit.ors.util.GeomUtility;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

public class RouteResult {

	private Coordinate[] geometry;
	private List<RouteSegment> segments;
	private List<RouteLeg> legs;
	private List<RouteExtraInfo> extraInfo;
	private PointList pointlist;
	private String graphDate = "";
	private final List<Integer> wayPointsIndices;
	private final List<RouteWarning> routeWarnings;
	private final RouteSummary summary;

	private ZonedDateTime departure;
	private ZonedDateTime arrival;
	public static final String KEY_TIMEZONE_DEPARTURE = "timezone.departure";
	public static final String KEY_TIMEZONE_ARRIVAL = "timezone.arrival";
	public static final String DEFAULT_TIMEZONE = "Europe/Berlin";

	public RouteResult(int routeExtras) {
		segments = new ArrayList<>();
		summary = new RouteSummary();
		if (routeExtras != 0)
			extraInfo = new ArrayList<>();
		routeWarnings = new ArrayList<>();
		wayPointsIndices = new ArrayList<>();
		legs = new ArrayList<>();
	}
	
	public void addSegment(RouteSegment seg)
	{
		segments.add(seg);
	}

	public List<RouteSegment> getSegments()
	{
		return segments;
	}

	public void resetSegments() {
		segments = new ArrayList<>();
	}

	public RouteSummary getSummary()
	{
		return summary;
	}

	public Coordinate[] getGeometry() {
		return geometry;
	}

	public void addPointsToGeometry(PointList points, boolean skipFirstPoint, boolean includeElevation)
	{
		int index = skipFirstPoint ? 1 : 0;
		
		if (geometry == null)
		{
			int newSize = points.size() - index;
			geometry = new Coordinate[newSize];

			if (includeElevation && points.is3D())
			{
				for (int i = index; i < newSize; ++i)
					geometry[i] = new Coordinate(points.getLon(i), points.getLat(i), points.getEle(i));
			}
			else
			{
				for (int i= index; i < newSize; ++i)
					geometry[i] = new Coordinate(points.getLon(i), points.getLat(i));
			}
		}
		else
		{
			int oldSize = geometry.length;
			int pointsSize = points.size() - index;
			int newSize = oldSize + pointsSize;
			Coordinate[] coords = new Coordinate[newSize];
			System.arraycopy(geometry, 0, coords, 0, oldSize);
			for (int i = 0; i < pointsSize; ++i) {
				int j = i + index;
				if (includeElevation && points.is3D())
					coords[oldSize + i] = new Coordinate(points.getLon(j), points.getLat(j), points.getEle(j));
				else
					coords[oldSize + i] = new Coordinate(points.getLon(j), points.getLat(j));
			}

			geometry = coords;
		}
	}

	
	public List<RouteExtraInfo> getExtraInfo() 
	{
		return extraInfo;
	}
	
	private void addExtraInfo(RouteExtraInfo info)
	{
		if(extraInfo == null)
			extraInfo = new ArrayList<>();
		extraInfo.add(info);
	}
	
	public void addWarning(RouteWarning warning) {
		routeWarnings.add(warning);
	}

	public List<RouteWarning> getWarnings() {
		return routeWarnings;
	}

	public void addPointlist(PointList pointlistToAdd) {
		if (pointlist == null) {
			pointlist = new PointList(pointlistToAdd.size(), pointlistToAdd.is3D());
		}
		pointlist.add(pointlistToAdd);
	}

	public List<Integer> getWayPointsIndices() {
		return wayPointsIndices;
	}

	public void addWayPointIndex(int index) {
		wayPointsIndices.add(index);
	}

	void addExtras(RoutingRequest request, List<RouteExtraInfo> extras) {
		if (extras == null)
			return;
		// add the extras if they generate a "warning" or they were requested
		for (RouteExtraInfo extra : extras) {
			if (extra.isUsedForWarnings() && extra.getWarningGraphExtension().generatesWarning(extra)) {
				addWarning(extra.getWarningGraphExtension().getWarning());
				addExtraInfo(extra);
			} else if (RouteExtraInfoFlag.isSet(request.getExtraInfo(), RouteExtraInfoFlag.getFromString(extra.getName()))) {
				addExtraInfo(extra);
			}
		}
	}

	/**
	 * set route summary values according to segments in this route and request parameters
	 * @param request for parameter lookup (units, include elevation)
	 */
	void calculateRouteSummary(RoutingRequest request) {
		calculateRouteSummary(request, null);
	}

	void calculateRouteSummary(RoutingRequest request, ResponsePath path) {
		double distance = 0.0;
		double duration = 0.0;
		for (RouteSegment seg : getSegments()) {
			distance += seg.getDistance();
			duration += seg.getDuration();
		}
		summary.setDuration(duration);
		summary.setDistance(FormatUtility.roundToDecimalsForUnits(distance, request.getUnits()));
		double averageSpeed = 0;
		if (duration > 0)
			averageSpeed = distance / (request.getUnits() == DistanceUnit.METERS ? 1000 : 1) / (duration / 3600);
		summary.setAverageSpeed(FormatUtility.roundToDecimals(averageSpeed, 1));
		summary.setBBox(GeomUtility.calculateBoundingBox(pointlist));
		if (request.getIncludeElevation()) {
			double ascent = 0.0;
			double descent = 0.0;
			for (RouteSegment seg : getSegments()) {
				ascent += seg.getAscent();
				descent += seg.getDescent();
			}
			summary.setAscent(FormatUtility.roundToDecimals(ascent, 1));
			summary.setDescent(FormatUtility.roundToDecimals(descent, 1));
		}
		if (path != null) {
			summary.setTransfers(path.getNumChanges());
			if (path.getFare() != null)
				summary.setFare(path.getFare().intValue());
		}
	}

	public String getGraphDate() {
		return graphDate;
	}

	public void setGraphDate(String graphDate) {
		this.graphDate = graphDate;
	}

	public boolean hasDepartureAndArrival() {
		return (departure!=null && arrival!=null);
	}

	public ZonedDateTime getDeparture() {
		return departure;
	}

	public void setDeparture(ZonedDateTime departure) {
	    this.departure = departure;
    }

	public ZonedDateTime getArrival() {
		return arrival;
	}

	public void setArrival(ZonedDateTime arrival) {
	    this.arrival = arrival;
    }

	public void addLeg(RouteLeg leg) {
		legs.add(leg);
	}

	public List<RouteLeg> getLegs() {
		return legs;
	}
}
