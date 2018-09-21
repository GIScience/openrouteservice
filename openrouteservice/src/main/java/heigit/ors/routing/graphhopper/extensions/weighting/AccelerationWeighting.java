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
package heigit.ors.routing.graphhopper.extensions.weighting;

import com.graphhopper.routing.VirtualEdgeIteratorState;
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.routing.weighting.FastestWeighting;
import com.graphhopper.storage.GraphHopperStorage;
import com.graphhopper.storage.GraphStorage;
import com.graphhopper.util.AngleCalc;
import com.graphhopper.util.EdgeIteratorState;
import com.graphhopper.util.PMap;
import com.graphhopper.util.PointList;

public class AccelerationWeighting extends FastestWeighting {
	private GraphHopperStorage _ghStorage;
	private AngleCalc _angleCalc = new AngleCalc();
	private long _maxEdges;

	public AccelerationWeighting(FlagEncoder encoder, PMap map, GraphStorage graphStorage) {
		super(encoder, map);
		_ghStorage = (GraphHopperStorage)graphStorage;
		_maxEdges= _ghStorage.getEdges();
	}

	private double getTurnAngle(PointList currEdgeGeom, PointList prevEdgeGeom)
	{
		if (currEdgeGeom.size() >= 1 && prevEdgeGeom.size() >= 1)
		{
			int locIndex = prevEdgeGeom.size() - 1;
			double lon0 = prevEdgeGeom.getLon(locIndex - 1);
			double lat0 = prevEdgeGeom.getLat(locIndex - 1);
			double lon1 = prevEdgeGeom.getLon(locIndex);
			double lat1 = prevEdgeGeom.getLat(locIndex);

			double bearingBefore = Math.round(_angleCalc.calcAzimuth(lat0, lon0, lat1, lon1));

			double lon2 = currEdgeGeom.getLon(1);
			double lat2 = currEdgeGeom.getLat(1);

			double bearingAfter = (int)Math.round(_angleCalc.calcAzimuth(lat1, lon1, lat2, lon2));
			//bearingAfter =  _angleCalc.alignOrientation(bearingBefore, bearingAfter);
			double res = Math.abs(bearingBefore - bearingAfter);
			if (res > 180)
			{
				res = 360 - res;
				return res;
			}
			
			return res;
		}

		return 0.0;	
	}

	@Override
	public double calcWeight(EdgeIteratorState edgeState, boolean reverse, int prevOrNextEdgeId) {
		if (prevOrNextEdgeId == -1 )
			return 1.0;
		
		if (edgeState instanceof VirtualEdgeIteratorState || prevOrNextEdgeId >= _maxEdges || edgeState.getEdge() >= _maxEdges)
		{
			//TODO
			return 1.0;
		}

		PointList currEdgeGeom, prevEdgeGeom;
		if (reverse)
		{
			prevEdgeGeom =  _ghStorage.getEdgeIteratorState(edgeState.getEdge(), edgeState.getBaseNode()).fetchWayGeometry(3);
			currEdgeGeom =  _ghStorage.getEdgeIteratorState(prevOrNextEdgeId, edgeState.getBaseNode()).detach(true).fetchWayGeometry(3);
		}
		else
		{
			currEdgeGeom =  _ghStorage.getEdgeIteratorState(edgeState.getEdge(), edgeState.getAdjNode()).fetchWayGeometry(3);
			prevEdgeGeom =  _ghStorage.getEdgeIteratorState(prevOrNextEdgeId, edgeState.getBaseNode()).fetchWayGeometry(3);
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

	@Override
	public long calcMillis(EdgeIteratorState edgeState, boolean reverse, int prevOrNextEdgeId) {
		if (prevOrNextEdgeId == -1 )
			return 0;
		
		if (edgeState instanceof VirtualEdgeIteratorState || prevOrNextEdgeId >= _maxEdges || edgeState.getEdge() >= _maxEdges)
		{
			// compute acceleration for departure and finish edges.
			return 10000;
		}

		PointList currEdgeGeom, prevEdgeGeom;
		if (reverse)
		{
			prevEdgeGeom =  _ghStorage.getEdgeIteratorState(edgeState.getEdge(), edgeState.getBaseNode()).fetchWayGeometry(3);
			currEdgeGeom =  _ghStorage.getEdgeIteratorState(prevOrNextEdgeId, edgeState.getBaseNode()).detach(true).fetchWayGeometry(3);
		}
		else
		{
			currEdgeGeom =  _ghStorage.getEdgeIteratorState(edgeState.getEdge(), edgeState.getAdjNode()).fetchWayGeometry(3);
			prevEdgeGeom =  _ghStorage.getEdgeIteratorState(prevOrNextEdgeId, edgeState.getBaseNode()).fetchWayGeometry(3);
		}

		double turnAngle = getTurnAngle(currEdgeGeom, prevEdgeGeom);
		
		if (isFullTurn(turnAngle))
		{
			/*double speed = 1000*edgeState.getDistance()/weight * SPEED_CONV; 
			double distAfter = currEdgeGeom.calcDistance(Helper.DIST_EARTH);

			// compute acceleration influence only for a segment after the turn.
			int totalSeconds = (int)(weight/1000) + 100;
            int accelTime = 0;
			double accelDist = 0.0;
            
			for (int i= 0; i < totalSeconds; ++i)
			{
				double currSpeed = (i + 1)* 2.5*0.3048;

				accelTime = i + 1;
				accelDist += currSpeed;
		
				if (currSpeed >= speed/SPEED_CONV)
					break;
				if (accelDist > distAfter)
					break;
			}
			
			accelTime *= 1000;
			long fullSpeedTime = 0;
			if (accelDist < distAfter)
			{
				fullSpeedTime = (long)((distAfter - accelDist)/speed * SPEED_CONV);
			}
					
			return (long)(-weight + accelTime + fullSpeedTime);*/
			
			return (long)0;// 10 seconds for every turn
		}

		return 0;
	}
}