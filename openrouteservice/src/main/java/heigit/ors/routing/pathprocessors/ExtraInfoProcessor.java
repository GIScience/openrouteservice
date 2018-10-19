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
package heigit.ors.routing.pathprocessors;

import com.graphhopper.routing.EdgeIteratorStateHelper;
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.routing.util.PathProcessor;
import com.graphhopper.routing.util.PriorityCode;
import com.graphhopper.routing.util.WaySurfaceDescription;
import com.graphhopper.routing.weighting.PriorityWeighting;
import com.graphhopper.util.EdgeIteratorState;
import com.graphhopper.util.PointList;
import heigit.ors.routing.RouteExtraInfo;
import heigit.ors.routing.RouteExtraInfoFlag;
import heigit.ors.routing.RoutingProfileType;
import heigit.ors.routing.RoutingRequest;
import heigit.ors.routing.graphhopper.extensions.ORSGraphHopper;
import heigit.ors.routing.graphhopper.extensions.storages.*;
import heigit.ors.routing.util.ElevationSmoother;
import heigit.ors.routing.util.extrainfobuilders.RouteExtraInfoBuilder;
import heigit.ors.routing.util.extrainfobuilders.SimpleRouteExtraInfoBuilder;
import heigit.ors.routing.util.extrainfobuilders.SteepnessExtraInfoBuilder;

import java.util.ArrayList;
import java.util.List;

public class ExtraInfoProcessor extends PathProcessor {
	private WaySurfaceTypeGraphStorage _extWaySurface;
	private WayCategoryGraphStorage _extWayCategory;
	private GreenIndexGraphStorage _extGreenIndex;
	private NoiseIndexGraphStorage _extNoiseIndex;
	private TollwaysGraphStorage _extTollways;
	private TrailDifficultyScaleGraphStorage _extTrailDifficulty;
	private HillIndexGraphStorage _extHillIndex;
	private OsmIdGraphStorage _extOsmId;
	
	private RouteExtraInfo _surfaceInfo;
	private RouteExtraInfoBuilder _surfaceInfoBuilder;

	private RouteExtraInfo _wayTypeInfo;
	private RouteExtraInfoBuilder _wayTypeInfoBuilder;
	
	private RouteExtraInfo _steepnessInfo;
	private SteepnessExtraInfoBuilder _steepnessInfoBuilder;
	
	private RouteExtraInfo _waySuitabilityInfo;
	private RouteExtraInfoBuilder _waySuitabilityInfoBuilder;
	
	private RouteExtraInfo _wayCategoryInfo;
	private RouteExtraInfoBuilder _wayCategoryInfoBuilder;

	private RouteExtraInfo _greenInfo;
	private RouteExtraInfoBuilder _greenInfoBuilder;
	
	private RouteExtraInfo _noiseInfo;
	private RouteExtraInfoBuilder _noiseInfoBuilder;
	
	private RouteExtraInfo _avgSpeedInfo;
	private RouteExtraInfoBuilder _avgSpeedInfoBuilder;
	
	private RouteExtraInfo _tollwaysInfo;
	private RouteExtraInfoBuilder _tollwaysInfoBuilder;
	private TollwayExtractor _tollwayExtractor;
	
	private RouteExtraInfo _trailDifficultyInfo;
	private RouteExtraInfoBuilder _trailDifficultyInfoBuilder;

	private RouteExtraInfo _osmIdInfo;
	private RouteExtraInfoBuilder _osmIdInfoBuilder;
	
	private int _profileType = RoutingProfileType.UNKNOWN;
	private FlagEncoder _encoder;
	private double _maximumSpeed = -1;
	private boolean _encoderWithPriority = false;
	private byte[] buffer;
	private boolean _lastSegment;

	public ExtraInfoProcessor(ORSGraphHopper graphHopper, RoutingRequest req) throws Exception 
	{
		_profileType = req.getSearchParameters().getProfileType();
		_maximumSpeed = req.getSearchParameters().getMaximumSpeed();
		int extraInfo = req.getExtraInfo();
		
		if (RouteExtraInfoFlag.isSet(extraInfo, RouteExtraInfoFlag.WayCategory))
		{
			_extWayCategory = GraphStorageUtils.getGraphExtension(graphHopper.getGraphHopperStorage(), WayCategoryGraphStorage.class);
			
			if (_extWayCategory == null)
				throw new Exception("WayCategory storage is not found.");
			
			_wayCategoryInfo = new RouteExtraInfo("waycategory");
			_wayCategoryInfoBuilder = new SimpleRouteExtraInfoBuilder(_wayCategoryInfo);
		}
		
		if (RouteExtraInfoFlag.isSet(extraInfo, RouteExtraInfoFlag.Surface) || RouteExtraInfoFlag.isSet(extraInfo, RouteExtraInfoFlag.WayType))
		{
			_extWaySurface = GraphStorageUtils.getGraphExtension(graphHopper.getGraphHopperStorage(), WaySurfaceTypeGraphStorage.class);
			
			if (_extWaySurface == null)
				throw new Exception("WaySurfaceType storage is not found.");

			if (RouteExtraInfoFlag.isSet(extraInfo, RouteExtraInfoFlag.Surface))
			{
				_surfaceInfo = new RouteExtraInfo("surface");
				_surfaceInfoBuilder = new SimpleRouteExtraInfoBuilder(_surfaceInfo);
			}
			if (RouteExtraInfoFlag.isSet(extraInfo, RouteExtraInfoFlag.WayType))
			{
				_wayTypeInfo = new RouteExtraInfo("waytypes");
				_wayTypeInfoBuilder = new SimpleRouteExtraInfoBuilder(_wayTypeInfo);
			}
		}
		
		if (RouteExtraInfoFlag.isSet(extraInfo, RouteExtraInfoFlag.Steepness))
		{
			_steepnessInfo = new RouteExtraInfo("steepness");
			_steepnessInfoBuilder = new SteepnessExtraInfoBuilder(_steepnessInfo);
		}
		
		if (RouteExtraInfoFlag.isSet(extraInfo, RouteExtraInfoFlag.Suitability))
		{
			_waySuitabilityInfo = new RouteExtraInfo("suitability");
			_waySuitabilityInfoBuilder = new SimpleRouteExtraInfoBuilder(_waySuitabilityInfo);
		}
		
		if (RouteExtraInfoFlag.isSet(extraInfo, RouteExtraInfoFlag.AvgSpeed))
		{
			_avgSpeedInfo = new RouteExtraInfo("avgspeed");
			_avgSpeedInfo.setFactor(10);
			_avgSpeedInfoBuilder = new SimpleRouteExtraInfoBuilder(_avgSpeedInfo);
		}
		
		if (RouteExtraInfoFlag.isSet(extraInfo, RouteExtraInfoFlag.Tollways))
		{
			_extTollways = GraphStorageUtils.getGraphExtension(graphHopper.getGraphHopperStorage(), TollwaysGraphStorage.class);

			if (_extTollways == null)
				throw new Exception("Tollways storage is not found.");
			
			_tollwaysInfo = new RouteExtraInfo("tollways");
			_tollwaysInfoBuilder = new SimpleRouteExtraInfoBuilder(_tollwaysInfo);
			_tollwayExtractor = new TollwayExtractor(_extTollways, req.getSearchParameters().getVehicleType(), req.getSearchParameters().getProfileParameters());
		}

		if (RouteExtraInfoFlag.isSet(extraInfo, RouteExtraInfoFlag.TrailDifficulty))
		{
			_extTrailDifficulty  = GraphStorageUtils.getGraphExtension(graphHopper.getGraphHopperStorage(), TrailDifficultyScaleGraphStorage.class);
			_extHillIndex = GraphStorageUtils.getGraphExtension(graphHopper.getGraphHopperStorage(), HillIndexGraphStorage.class);
			
			_trailDifficultyInfo = new RouteExtraInfo("traildifficulty");
			_trailDifficultyInfoBuilder = new SimpleRouteExtraInfoBuilder(_trailDifficultyInfo);
		}

		if (RouteExtraInfoFlag.isSet(extraInfo, RouteExtraInfoFlag.Green)) {
			_extGreenIndex = GraphStorageUtils.getGraphExtension(graphHopper.getGraphHopperStorage(), GreenIndexGraphStorage.class);

			if (_extGreenIndex == null)
				throw new Exception("GreenIndex storage is not found.");
			_greenInfo = new RouteExtraInfo("green");
			_greenInfoBuilder = new SimpleRouteExtraInfoBuilder(_greenInfo);
		}

		if (RouteExtraInfoFlag.isSet(extraInfo, RouteExtraInfoFlag.Noise)) {

			_extNoiseIndex = GraphStorageUtils.getGraphExtension(graphHopper.getGraphHopperStorage(), NoiseIndexGraphStorage.class);

			if (_extNoiseIndex == null)
				throw new Exception("NoiseIndex storage is not found.");
			_noiseInfo = new RouteExtraInfo("noise");
			_noiseInfoBuilder = new SimpleRouteExtraInfoBuilder(_noiseInfo);
		}

		if (RouteExtraInfoFlag.isSet(extraInfo, RouteExtraInfoFlag.OsmId)) {
			_extOsmId = GraphStorageUtils.getGraphExtension(graphHopper.getGraphHopperStorage(), OsmIdGraphStorage.class);

			if(_extOsmId == null)
				throw new Exception("OsmId storage is not found");
			_osmIdInfo = new RouteExtraInfo("osmId");
			_osmIdInfoBuilder = new SimpleRouteExtraInfoBuilder(_osmIdInfo);
		}
		buffer = new byte[4];
	}

	public void setSegmentIndex(int index, int count)
	{
		_lastSegment = index == count - 1;
	}

	public List<RouteExtraInfo> getExtras()
	{
		List<RouteExtraInfo> extras = new ArrayList<RouteExtraInfo>();

		if (_surfaceInfo != null)
			extras.add(_surfaceInfo);
		if (_wayTypeInfo != null)
			extras.add(_wayTypeInfo);
		if (_steepnessInfo != null)
			extras.add(_steepnessInfo);
		if (_waySuitabilityInfo != null)
			extras.add(_waySuitabilityInfo);
		if (_wayCategoryInfo != null)
			extras.add(_wayCategoryInfo);
		if (_avgSpeedInfo != null)
			extras.add(_avgSpeedInfo);
		if (_greenInfo != null)
			extras.add(_greenInfo);
		if (_noiseInfo != null)
			extras.add(_noiseInfo);
		if (_tollwaysInfo != null)
			extras.add(_tollwaysInfo);
		if (_trailDifficultyInfo != null)
			extras.add(_trailDifficultyInfo);
		if (_osmIdInfo != null)
			extras.add(_osmIdInfo);

		return extras;
	}

	@Override
	public void processEdge(EdgeIteratorState edge, boolean isLastEdge, PointList geom) {
		double dist = edge.getDistance();

		if (_extWaySurface != null && _wayTypeInfo != null || _surfaceInfo != null)
		{
			WaySurfaceDescription wsd = _extWaySurface.getEdgeValue(EdgeIteratorStateHelper.getOriginalEdge(edge), buffer);

			if (_surfaceInfoBuilder != null)
				_surfaceInfoBuilder.addSegment(wsd.SurfaceType, wsd.SurfaceType, geom, dist, isLastEdge && _lastSegment);
			
			if (_wayTypeInfo != null)
				_wayTypeInfoBuilder.addSegment(wsd.WayType, wsd.WayType, geom, dist, isLastEdge && _lastSegment);
		}
		
		if (_wayCategoryInfoBuilder != null)
		{
			int value = _extWayCategory.getEdgeValue(EdgeIteratorStateHelper.getOriginalEdge(edge), buffer);
			_wayCategoryInfoBuilder.addSegment(value, value, geom, dist, isLastEdge && _lastSegment);
		}
		
		if (_trailDifficultyInfoBuilder != null)
		{
			int value = 0;
			if (RoutingProfileType.isCycling(_profileType))
			{
				boolean uphill = false;
				if (_extHillIndex != null)
				{
					boolean revert = edge.getBaseNode() > edge.getAdjNode();
					int hillIndex = _extHillIndex.getEdgeValue(EdgeIteratorStateHelper.getOriginalEdge(edge), revert, buffer);
					if (hillIndex > 0)
						uphill = true;
				}
				
				value = _extTrailDifficulty.getMtbScale(EdgeIteratorStateHelper.getOriginalEdge(edge), buffer, uphill);
			}
			else if (RoutingProfileType.isWalking(_profileType))
				value = _extTrailDifficulty.getHikingScale(EdgeIteratorStateHelper.getOriginalEdge(edge), buffer);
			
			_trailDifficultyInfoBuilder.addSegment(value, value, geom, dist, isLastEdge && _lastSegment);
		}
		
		if (_avgSpeedInfoBuilder != null)
		{
		    double speed = _encoder.getSpeed(edge.getFlags());
		    if (_maximumSpeed > 0 && speed > _maximumSpeed)
		    	speed = _maximumSpeed;
		    _avgSpeedInfoBuilder.addSegment(speed, (int)Math.round(speed*_avgSpeedInfo.getFactor()), geom, dist, isLastEdge && _lastSegment);
		}
		
		if (_tollwaysInfoBuilder != null)
		{
			int value = _tollwayExtractor.getValue(EdgeIteratorStateHelper.getOriginalEdge(edge));
		    _tollwaysInfoBuilder.addSegment(value, value, geom, dist, isLastEdge && _lastSegment);
		}

		if (_waySuitabilityInfoBuilder != null)
		{
			double priority = 0.3;
			int priorityIndex = 3;
			
			if (_encoderWithPriority)
			{
				priority = _encoder.getDouble(edge.getFlags(), 101);
				priorityIndex = (int)(3 + priority*PriorityCode.BEST.getValue()); // normalize values between 3 and 10
			}
			else
			{
				priority = _encoder.getSpeed(edge.getFlags()) / _encoder.getMaxSpeed();
				if (priority < 0.3)
					priority = 0.3;
				priorityIndex = (int)(priority * 10);
			}
			
			_waySuitabilityInfoBuilder.addSegment(priority, priorityIndex, geom, dist,  isLastEdge && _lastSegment);
		}
		
		if (_steepnessInfoBuilder != null)
		{
			// just add dummy values
			_steepnessInfoBuilder.addSegment(0, 0, geom, dist, isLastEdge && _lastSegment);
		}

		if (_greenInfoBuilder != null) {
			int value = _extGreenIndex.getEdgeValue(EdgeIteratorStateHelper.getOriginalEdge(edge), buffer);
			// This number is how many levels client can display in the stats bar
			// FIXME should be changed when the specific bar legend for green routing is finished
			int MIN_CLIENT_VAL = 3;
			int MAX_CLIENT_VAL = 10;
			int clientVal = MIN_CLIENT_VAL + value * (MAX_CLIENT_VAL - MIN_CLIENT_VAL + 1) / 64;
			_greenInfoBuilder.addSegment(value, clientVal, geom, dist, isLastEdge && _lastSegment);
		}
		
		if (_noiseInfoBuilder != null) {
			int noise_level = _extNoiseIndex.getEdgeValue(EdgeIteratorStateHelper.getOriginalEdge(edge), buffer);
			// convert the noise level (from 0 to 3) to the values (from 7 to 10) for the client
			if (noise_level > 3)
				noise_level = 3; 
			
			int client_noise_level = noise_level + 7;
			_noiseInfoBuilder.addSegment(noise_level, client_noise_level, geom, dist, isLastEdge && _lastSegment);
		}

		if (_osmIdInfoBuilder != null) {

			long osmId = _extOsmId.getEdgeValue(EdgeIteratorStateHelper.getOriginalEdge(edge));

			_osmIdInfoBuilder.addSegment((double)osmId, osmId, geom, dist, isLastEdge && _lastSegment);
		}
	}

	@Override
	public PointList processPoints(PointList points) 
	{
        PointList result = points;
		
		if (points.is3D())
			result = ElevationSmoother.smooth(points);
		
		if (_steepnessInfoBuilder != null)
		{
			// compute steepness information only after elevation data is smoothed.
			_steepnessInfoBuilder.addPoints(result);
		}

		return result;
	}

	public void finish()
	{
		
	}

	@Override
	public void init(FlagEncoder encoder) {
		_encoder = encoder;
		_encoderWithPriority = _encoder.supports(PriorityWeighting.class);
	}
}
