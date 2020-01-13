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

import java.util.ArrayList;
import java.util.List;

import com.graphhopper.util.PointList;
import com.vividsolutions.jts.geom.Coordinate;
import org.heigit.ors.common.DistanceUnit;
import org.heigit.ors.util.FormatUtility;
import org.heigit.ors.util.GeomUtility;

public class RouteResult 
{
	private RouteSummary summary;
	private Coordinate[] geometry;
	private List<RouteSegment> segments;
	private List<RouteExtraInfo> extraInfo;
	private List<Integer> wayPointsIndices;
	private List<RouteWarning> routeWarnings;
	private PointList pointlist;
	private String graphDate = "";

	public RouteResult(int routeExtras) {
		segments = new ArrayList<>();
		summary = new RouteSummary();
		if (routeExtras != 0)
			extraInfo = new ArrayList<>();
		routeWarnings = new ArrayList<>();
		wayPointsIndices = new ArrayList<>();
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
			pointlist = new PointList(pointlistToAdd.getSize(), pointlistToAdd.is3D());
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
	}

	public String getGraphDate() {
		return graphDate;
	}

	public void setGraphDate(String graphDate) {
		this.graphDate = graphDate;
	}
}
