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

import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.routing.util.PathProcessor;
import com.graphhopper.routing.util.PriorityCode;
import com.graphhopper.routing.util.WaySurfaceDescription;
import com.graphhopper.util.EdgeIteratorState;
import com.graphhopper.util.PointList;

import heigit.ors.routing.RouteExtraInfo;
import heigit.ors.routing.RouteExtraInfoFlag;
import heigit.ors.routing.graphhopper.extensions.ORSGraphHopper;
import heigit.ors.routing.graphhopper.extensions.storages.GraphStorageUtils;
import heigit.ors.routing.graphhopper.extensions.storages.WaySurfaceTypeGraphStorage;
import heigit.ors.routing.graphhopper.extensions.storages.WayCategoryGraphStorage;
import heigit.ors.routing.util.ElevationSmoother;
import heigit.ors.routing.util.extrainfobuilders.RouteExtraInfoBuilder;
import heigit.ors.routing.util.extrainfobuilders.SimpleRouteExtraInfoBuilder;
import heigit.ors.routing.util.extrainfobuilders.SteepnessExtraInfoBuilder;

public class ExtraInfoProcessor extends PathProcessor {
	private WaySurfaceTypeGraphStorage extWaySurface;
	private WayCategoryGraphStorage extWayCategory;
	
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
	
	private FlagEncoder _encoder;
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

		if (_waySuitabilityInfoBuilder != null)
		{
			double priority = _encoder.getDouble(edge.getFlags(), 101);
			int priorityIndex = (int)(3 + priority*PriorityCode.BEST.getValue()); // normalize values between 3 and 10
			_waySuitabilityInfoBuilder.addSegment(priority, priorityIndex, geom, dist,  lastEdge && _lastSegment);
		}
		
		if (_steepnessInfoBuilder != null)
		{
			// just add dummy values
			_steepnessInfoBuilder.addSegment(0, 0, geom, dist, lastEdge && _lastSegment);
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
	public void start(FlagEncoder encoder) {
		_encoder = encoder;
	}
}
