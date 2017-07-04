/*|----------------------------------------------------------------------------------------------
 *|														Heidelberg University
 *|	  _____ _____  _____      _                     	Department of Geography		
 *|	 / ____|_   _|/ ____|    (_)                    	Chair of GIScience
 *|	| |  __  | | | (___   ___ _  ___ _ __   ___ ___ 	(C) 2014
 *|	| | |_ | | |  \___ \ / __| |/ _ \ '_ \ / __/ _ \	
 *|	| |__| |_| |_ ____) | (__| |  __/ | | | (_|  __/	Berliner Strasse 48								
 *|	 \_____|_____|_____/ \___|_|\___|_| |_|\___\___|	D-69120 Heidelberg, Germany	
 *|	        	                                       	http://www.giscience.uni-hd.de
 *|								
 *|----------------------------------------------------------------------------------------------*/
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
import heigit.ors.routing.graphhopper.extensions.ORSGraphHopper;
import heigit.ors.routing.graphhopper.extensions.storages.*;
import heigit.ors.routing.util.ElevationSmoother;
import heigit.ors.routing.util.extrainfobuilders.RouteExtraInfoBuilder;
import heigit.ors.routing.util.extrainfobuilders.SimpleRouteExtraInfoBuilder;
import heigit.ors.routing.util.extrainfobuilders.SteepnessExtraInfoBuilder;

public class ExtraInfoProcessor extends PathProcessor {
	private WaySurfaceTypeGraphStorage extWaySurface;
	private WayCategoryGraphStorage extWayCategory;
	private GreenIndexGraphStorage extGreenIndex;
	private NoiseIndexGraphStorage extNoiseIndex;
	
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
	
	private FlagEncoder _encoder;
	private boolean _encoderWithPriority = false;
	private byte[] buffer;
	private boolean _lastSegment;

	public ExtraInfoProcessor(ORSGraphHopper graphHopper, int extraInfo) throws Exception {
		
		if (RouteExtraInfoFlag.isSet(extraInfo, RouteExtraInfoFlag.WayCategory))
		{
			extWayCategory = GraphStorageUtils.getGraphExtension(graphHopper.getGraphHopperStorage(), WayCategoryGraphStorage.class);
			
			if (extWayCategory == null)
				throw new Exception("WayCategory storage is not found.");
			
			_wayCategoryInfo = new RouteExtraInfo("waycategory");
			_wayCategoryInfoBuilder = new SimpleRouteExtraInfoBuilder(_wayCategoryInfo);
		}
		
		if (RouteExtraInfoFlag.isSet(extraInfo, RouteExtraInfoFlag.Surface) || RouteExtraInfoFlag.isSet(extraInfo, RouteExtraInfoFlag.WayType))
		{
			extWaySurface = GraphStorageUtils.getGraphExtension(graphHopper.getGraphHopperStorage(), WaySurfaceTypeGraphStorage.class);
			
			if (extWaySurface == null)
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
			_avgSpeedInfoBuilder = new SimpleRouteExtraInfoBuilder(_avgSpeedInfo);
		}

		if (RouteExtraInfoFlag.isSet(extraInfo, RouteExtraInfoFlag.Green)) {
			extGreenIndex = GraphStorageUtils.getGraphExtension(graphHopper.getGraphHopperStorage(), GreenIndexGraphStorage.class);

			if (extGreenIndex == null)
				throw new Exception("GreenIndex storage is not found.");
			_greenInfo = new RouteExtraInfo("green");
			_greenInfoBuilder = new SimpleRouteExtraInfoBuilder(_greenInfo);
		}

		if (RouteExtraInfoFlag.isSet(extraInfo, RouteExtraInfoFlag.Noise)) {

			extNoiseIndex = GraphStorageUtils.getGraphExtension(graphHopper.getGraphHopperStorage(), NoiseIndexGraphStorage.class);

			if (extNoiseIndex == null)
				throw new Exception("NoiseIndex storage is not found.");
			_noiseInfo = new RouteExtraInfo("noise");
			_noiseInfoBuilder = new SimpleRouteExtraInfoBuilder(_noiseInfo);
		}

		buffer = new byte[1];
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

		return extras;
	}

	@Override
	public void processEdge(int pathIndex, EdgeIteratorState edge, boolean lastEdge, PointList geom) {
		double dist = edge.getDistance();

		if (extWaySurface != null && _wayTypeInfo != null || _surfaceInfo != null)
		{
			WaySurfaceDescription wsd = extWaySurface.getEdgeValue(edge.getOriginalEdge(), buffer);

			if (_surfaceInfoBuilder != null)
				_surfaceInfoBuilder.addSegment(wsd.SurfaceType, wsd.SurfaceType, geom, dist, lastEdge && _lastSegment);
			
			if (_wayTypeInfo != null)
				_wayTypeInfoBuilder.addSegment(wsd.WayType, wsd.WayType, geom, dist, lastEdge && _lastSegment);
		}
		
		if (_wayCategoryInfoBuilder != null)
		{
			int value = extWayCategory.getEdgeValue(edge.getOriginalEdge(), buffer);
			_wayCategoryInfoBuilder.addSegment(value, value, geom, dist, lastEdge && _lastSegment);
		}
		
		if (_avgSpeedInfoBuilder != null)
		{
		    double speed = _encoder.getSpeed(edge.getFlags(_encoder.getIndex()));
		    _avgSpeedInfoBuilder.addSegment(speed, (int)Math.round(speed*10), geom, dist, lastEdge && _lastSegment);
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
			int value = extGreenIndex.getEdgeValue(edge.getOriginalEdge(), buffer);
			// This number is how many levels client can display in the stats bar
			// FIXME should be changed when the specific bar legend for green routing is finished
			int MIN_CLIENT_VAL = 3;
			int MAX_CLIENT_VAL = 10;
			int clientVal = MIN_CLIENT_VAL + value * (MAX_CLIENT_VAL - MIN_CLIENT_VAL + 1) / 64;
			_greenInfoBuilder.addSegment(value, clientVal, geom, dist, lastEdge && _lastSegment);
		}
		
		if (_noiseInfoBuilder != null) {
			int noise_level = extNoiseIndex.getEdgeValue(edge.getOriginalEdge(), buffer);
			// convert the noise level (from 0 to 3) to the values (from 7 to 10) for the client
			if (noise_level > 3)
				noise_level = 3; 
			
			int client_noise_level = noise_level + 7;
			_noiseInfoBuilder.addSegment(noise_level, client_noise_level, geom, dist, lastEdge && _lastSegment);
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
