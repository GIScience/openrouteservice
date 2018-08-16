/*
 *  Licensed to GIScience Research Group, Heidelberg University (GIScience)
 *
 *   http://www.giscience.uni-hd.de
 *   http://www.heigit.org
 *
 *  under one or more contributor license agreements. See the NOTICE file 
 *  distributed with this work for additional information regarding copyright 
 *  ownership. The GIScience licenses this file to you under the Apache License, 
 *  Version 2.0 (the "License"); you may not use this file except in compliance 
 *  with the License. You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package heigit.ors.routing.pathprocessors;

import java.util.ArrayList;
import java.util.List;

import com.graphhopper.routing.PathProcessingContext;
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.routing.util.PathProcessor;
import com.graphhopper.routing.util.PriorityCode;
import com.graphhopper.routing.weighting.PriorityWeighting;
import com.graphhopper.routing.util.WaySurfaceDescription;
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
	public void processEdge(int pathIndex, EdgeIteratorState edge, boolean lastEdge, PointList geom) {
		double dist = edge.getDistance();

		if (_extWaySurface != null && _wayTypeInfo != null || _surfaceInfo != null)
		{
			WaySurfaceDescription wsd = _extWaySurface.getEdgeValue(edge.getOriginalEdge(), buffer);

			if (_surfaceInfoBuilder != null)
				_surfaceInfoBuilder.addSegment(wsd.SurfaceType, wsd.SurfaceType, geom, dist, lastEdge && _lastSegment);
			
			if (_wayTypeInfo != null)
				_wayTypeInfoBuilder.addSegment(wsd.WayType, wsd.WayType, geom, dist, lastEdge && _lastSegment);
		}
		
		if (_wayCategoryInfoBuilder != null)
		{
			int value = _extWayCategory.getEdgeValue(edge.getOriginalEdge(), buffer);
			_wayCategoryInfoBuilder.addSegment(value, value, geom, dist, lastEdge && _lastSegment);
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
					int hillIndex = _extHillIndex.getEdgeValue(edge.getOriginalEdge(), revert, buffer);
					if (hillIndex > 0)
						uphill = true;
				}
				
				value = _extTrailDifficulty.getMtbScale(edge.getOriginalEdge(), buffer, uphill);
			}
			else if (RoutingProfileType.isWalking(_profileType))
				value = _extTrailDifficulty.getHikingScale(edge.getOriginalEdge(), buffer);
			
			_trailDifficultyInfoBuilder.addSegment(value, value, geom, dist, lastEdge && _lastSegment);
		}
		
		if (_avgSpeedInfoBuilder != null)
		{
		    double speed = _encoder.getSpeed(edge.getFlags(_encoder.getIndex()));
		    if (_maximumSpeed > 0 && speed > _maximumSpeed)
		    	speed = _maximumSpeed;
		    _avgSpeedInfoBuilder.addSegment(speed, (int)Math.round(speed*_avgSpeedInfo.getFactor()), geom, dist, lastEdge && _lastSegment);
		}
		
		if (_tollwaysInfoBuilder != null)
		{
			int value = _tollwayExtractor.getValue(edge.getOriginalEdge());
		    _tollwaysInfoBuilder.addSegment(value, value, geom, dist, lastEdge && _lastSegment);
		}

		if (_waySuitabilityInfoBuilder != null)
		{
			double priority = 0.3;
			int priorityIndex = 3;
			
			if (_encoderWithPriority)
			{
				priority = _encoder.getDouble(edge.getFlags(_encoder.getIndex()), 101);
				priorityIndex = (int)(3 + priority*PriorityCode.BEST.getValue()); // normalize values between 3 and 10
			}
			else
			{
				priority = _encoder.getSpeed(edge.getFlags(_encoder.getIndex())) / _encoder.getMaxSpeed();
				if (priority < 0.3)
					priority = 0.3;
				priorityIndex = (int)(priority * 10);
			}
			
			_waySuitabilityInfoBuilder.addSegment(priority, priorityIndex, geom, dist,  lastEdge && _lastSegment);
		}
		
		if (_steepnessInfoBuilder != null)
		{
			// just add dummy values
			_steepnessInfoBuilder.addSegment(0, 0, geom, dist, lastEdge && _lastSegment);
		}

		if (_greenInfoBuilder != null) {
			int value = _extGreenIndex.getEdgeValue(edge.getOriginalEdge(), buffer);
			// This number is how many levels client can display in the stats bar
			// FIXME should be changed when the specific bar legend for green routing is finished
			int MIN_CLIENT_VAL = 3;
			int MAX_CLIENT_VAL = 10;
			int clientVal = MIN_CLIENT_VAL + value * (MAX_CLIENT_VAL - MIN_CLIENT_VAL + 1) / 64;
			_greenInfoBuilder.addSegment(value, clientVal, geom, dist, lastEdge && _lastSegment);
		}
		
		if (_noiseInfoBuilder != null) {
			int noise_level = _extNoiseIndex.getEdgeValue(edge.getOriginalEdge(), buffer);
			// convert the noise level (from 0 to 3) to the values (from 7 to 10) for the client
			if (noise_level > 3)
				noise_level = 3; 
			
			int client_noise_level = noise_level + 7;
			_noiseInfoBuilder.addSegment(noise_level, client_noise_level, geom, dist, lastEdge && _lastSegment);
		}

		if (_osmIdInfoBuilder != null) {

			long osmId = _extOsmId.getEdgeValue(edge.getOriginalEdge());

			_osmIdInfoBuilder.addSegment((double)osmId, osmId, geom, dist, lastEdge && _lastSegment);
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
	public void init(PathProcessingContext cntx) {
		_encoder = cntx.getEncoder();
		_encoderWithPriority = _encoder.supports(PriorityWeighting.class);
	}
}
