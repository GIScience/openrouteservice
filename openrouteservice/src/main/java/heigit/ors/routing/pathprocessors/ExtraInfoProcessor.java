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
import heigit.ors.routing.RouteExtraInformationFlag;
import heigit.ors.routing.graphhopper.extensions.ORSGraphHopper;
import heigit.ors.routing.graphhopper.extensions.storages.WaySurfaceTypeGraphStorage;
import heigit.ors.routing.util.extrainfobuilders.DummyRouteExtraInfoBuilder;
import heigit.ors.routing.util.extrainfobuilders.RouteExtraInfoBuilder;
import heigit.ors.routing.util.extrainfobuilders.SimpleRouteExtraInfoBuilder;
import heigit.ors.routing.util.extrainfobuilders.SteepnessExtraInfoBuilder;

public class ExtraInfoProcessor extends PathProcessor {
	private WaySurfaceTypeGraphStorage storageWaySurface;
	
	private RouteExtraInfo _surfaceInfo;
	private RouteExtraInfoBuilder _surfaceInfoBuilder;

	private RouteExtraInfo _wayTypeInfo;
	private RouteExtraInfoBuilder _wayTypeInfoBuilder;
	
	private RouteExtraInfo _steepnessInfo;
	private RouteExtraInfoBuilder _steepnessInfoBuilder;
	
	private RouteExtraInfo _waySuitabilityInfo;
	private RouteExtraInfoBuilder _waySuitabilityInfoBuilder;
	
	private FlagEncoder _encoder;
	private byte[] buffer;
	private boolean _lastSegment;

	public ExtraInfoProcessor(ORSGraphHopper graphHopper, int extraInfo) {
		storageWaySurface = graphHopper.getWaySurfaceStorage();

		if (storageWaySurface != null)
		{
			if (RouteExtraInformationFlag.isSet(extraInfo, RouteExtraInformationFlag.Surface))
			{
				_surfaceInfo = new RouteExtraInfo("surface");
				_surfaceInfoBuilder = new SimpleRouteExtraInfoBuilder(_surfaceInfo);
			}
			if (RouteExtraInformationFlag.isSet(extraInfo, RouteExtraInformationFlag.WayTypes))
			{
				_wayTypeInfo = new RouteExtraInfo("waytypes");
				_wayTypeInfoBuilder = new SimpleRouteExtraInfoBuilder(_wayTypeInfo);
			}
		}
		
		if (RouteExtraInformationFlag.isSet(extraInfo, RouteExtraInformationFlag.Steepness))
		{
			_steepnessInfo = new RouteExtraInfo("steepness");
			_steepnessInfoBuilder = new DummyRouteExtraInfoBuilder(_steepnessInfo);// new SteepnessExtraInfoBuilder(_steepnessInfo);
		}
		
		if (RouteExtraInformationFlag.isSet(extraInfo, RouteExtraInformationFlag.Suitability))
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

		return extras;
	}

	@Override
	public void processEdge(int pathIndex, EdgeIteratorState edge, boolean lastEdge, PointList geom) {
		double dist = edge.getDistance();

		if (storageWaySurface != null && _wayTypeInfo != null || _surfaceInfo != null)
		{
			WaySurfaceDescription wsd = storageWaySurface.getEdgeValue(edge.getOriginalEdge(), buffer);

			if (_surfaceInfoBuilder != null)
				_surfaceInfoBuilder.addSegment(wsd.SurfaceType, wsd.SurfaceType, geom, dist, lastEdge && _lastSegment);
			
			if (_wayTypeInfo != null)
				_wayTypeInfoBuilder.addSegment(wsd.WayType, wsd.WayType, geom, dist, lastEdge && _lastSegment);
		}

		if (_waySuitabilityInfoBuilder != null)
		{
			double priority = _encoder.getDouble(edge.getFlags(), 101);
			int priorityIndex = (int)(3 + priority*PriorityCode.BEST.getValue()); // normalize values between 3 and 10
			_waySuitabilityInfoBuilder.addSegment(priority, priorityIndex, geom, dist,  lastEdge && _lastSegment);
		}
		
		if (_steepnessInfoBuilder != null)
			_steepnessInfoBuilder.addSegment(0, 0, geom, dist, lastEdge);
	}

	@Override
	public PointList processPoints(PointList points) {

		if (points.is3D())
		{
			points.smooth(20);
		}

		return points;
	}

	public void finish()
	{
		if (_steepnessInfoBuilder != null)
			_steepnessInfoBuilder.finish();
	}

	@Override
	public void start(FlagEncoder encoder) {
		_encoder = encoder;
	}
}
