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
package org.heigit.ors.routing.graphhopper.extensions.weighting;

import com.graphhopper.routing.querygraph.VirtualEdgeIteratorState;
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.routing.weighting.FastestWeighting;
import com.graphhopper.storage.GraphHopperStorage;
import com.graphhopper.util.*;

// TODO: this class seems to be unfinished since years. Can it be removed?
public class AccelerationWeighting extends FastestWeighting {
	private final GraphHopperStorage ghStorage;
	private final AngleCalc angleCalc = new AngleCalc();
	private final long maxEdges;

	public AccelerationWeighting(FlagEncoder encoder, PMap map, GraphHopperStorage graphStorage) {
		super(encoder, map);
		ghStorage = graphStorage;
		maxEdges = ghStorage.getEdges();
	}

	private double getTurnAngle(PointList currEdgeGeom, PointList prevEdgeGeom) {
		if (currEdgeGeom.size() >= 1 && prevEdgeGeom.size() >= 1) {
			int locIndex = prevEdgeGeom.size() - 1;
			double lon0 = prevEdgeGeom.getLon(locIndex - 1);
			double lat0 = prevEdgeGeom.getLat(locIndex - 1);
			double lon1 = prevEdgeGeom.getLon(locIndex);
			double lat1 = prevEdgeGeom.getLat(locIndex);

			double bearingBefore = Math.round(angleCalc.calcAzimuth(lat0, lon0, lat1, lon1));

			double lon2 = currEdgeGeom.getLon(1);
			double lat2 = currEdgeGeom.getLat(1);

			double bearingAfter = (int)Math.round(angleCalc.calcAzimuth(lat1, lon1, lat2, lon2));
			double res = Math.abs(bearingBefore - bearingAfter);
			if (res > 180) {
				res = 360 - res;
				return res;
			}
			return res;
		}
		return 0.0;	
	}

	public double calcWeight(EdgeIteratorState edgeState, boolean reverse, int prevOrNextEdgeId) {
		if (prevOrNextEdgeId == -1 )
			return 1.0;
		
		if (edgeState instanceof VirtualEdgeIteratorState || prevOrNextEdgeId >= maxEdges || edgeState.getEdge() >= maxEdges)
		{
			//TODO
			return 1.0;
		}

		PointList currEdgeGeom;
		PointList prevEdgeGeom;
		if (reverse)
		{
			prevEdgeGeom =  ghStorage.getEdgeIteratorState(edgeState.getEdge(), edgeState.getBaseNode()).fetchWayGeometry(FetchMode.ALL);
			currEdgeGeom =  ghStorage.getEdgeIteratorState(prevOrNextEdgeId, edgeState.getBaseNode()).detach(true).fetchWayGeometry(FetchMode.ALL);
		}
		else
		{
			currEdgeGeom =  ghStorage.getEdgeIteratorState(edgeState.getEdge(), edgeState.getAdjNode()).fetchWayGeometry(FetchMode.ALL);
			prevEdgeGeom =  ghStorage.getEdgeIteratorState(prevOrNextEdgeId, edgeState.getBaseNode()).fetchWayGeometry(FetchMode.ALL);
		}
 
		double turnAngle = getTurnAngle(currEdgeGeom, prevEdgeGeom);
		
		if (isFullTurn(turnAngle))
		{
			// TODO
			return 1.0;
		}

		return 1.0;
	}
	
	private boolean isFullTurn(double angle)
	{
		return angle > 50 && angle <= 140;
	}

	public long calcMillis(EdgeIteratorState edgeState, boolean reverse, int prevOrNextEdgeId) {
		if (prevOrNextEdgeId == -1 )
			return 0;
		
		if (edgeState instanceof VirtualEdgeIteratorState || prevOrNextEdgeId >= maxEdges || edgeState.getEdge() >= maxEdges)
		{
			// compute acceleration for departure and finish edges.
			return 10000;
		}

		PointList currEdgeGeom;
		PointList prevEdgeGeom;
		if (reverse)
		{
			prevEdgeGeom =  ghStorage.getEdgeIteratorState(edgeState.getEdge(), edgeState.getBaseNode()).fetchWayGeometry(FetchMode.ALL);
			currEdgeGeom =  ghStorage.getEdgeIteratorState(prevOrNextEdgeId, edgeState.getBaseNode()).detach(true).fetchWayGeometry(FetchMode.ALL);
		}
		else
		{
			currEdgeGeom =  ghStorage.getEdgeIteratorState(edgeState.getEdge(), edgeState.getAdjNode()).fetchWayGeometry(FetchMode.ALL);
			prevEdgeGeom =  ghStorage.getEdgeIteratorState(prevOrNextEdgeId, edgeState.getBaseNode()).fetchWayGeometry(FetchMode.ALL);
		}

		double turnAngle = getTurnAngle(currEdgeGeom, prevEdgeGeom);
		
		if (isFullTurn(turnAngle)) {
			return 0; // 10 seconds for every turn
		}

		return 0;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final AccelerationWeighting other = (AccelerationWeighting) obj;
		return toString().equals(other.toString());
	}

	@Override
	public int hashCode() {
		return ("AccWeighting" + this).hashCode();
	}
}