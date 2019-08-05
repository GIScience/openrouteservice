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
import com.graphhopper.routing.util.AbstractFlagEncoder;
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.routing.util.PathProcessor;
import com.graphhopper.routing.util.PriorityCode;
import com.graphhopper.routing.weighting.PriorityWeighting;
import com.graphhopper.storage.GraphExtension;
import com.graphhopper.storage.GraphHopperStorage;
import com.graphhopper.util.EdgeIteratorState;
import com.graphhopper.util.PMap;
import com.graphhopper.util.PointList;
import com.vividsolutions.jts.geom.Coordinate;
import heigit.ors.routing.*;
import heigit.ors.routing.graphhopper.extensions.flagencoders.FlagEncoderKeys;
import heigit.ors.routing.graphhopper.extensions.reader.borders.CountryBordersPolygon;
import heigit.ors.routing.graphhopper.extensions.reader.borders.CountryBordersReader;
import heigit.ors.routing.graphhopper.extensions.storages.*;
import heigit.ors.routing.graphhopper.extensions.util.ORSPMap;
import heigit.ors.routing.parameters.ProfileParameters;
import heigit.ors.routing.util.ElevationSmoother;
import heigit.ors.routing.util.WaySurfaceDescription;
import heigit.ors.routing.util.extrainfobuilders.AppendableSteepnessExtraInfoBuilder;
import heigit.ors.routing.util.extrainfobuilders.RouteExtraInfoBuilder;
import heigit.ors.routing.util.extrainfobuilders.AppendableRouteExtraInfoBuilder;
import heigit.ors.routing.util.extrainfobuilders.SteepnessExtraInfoBuilder;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class ExtraInfoProcessor implements PathProcessor {
	private WaySurfaceTypeGraphStorage _extWaySurface;
	private WayCategoryGraphStorage _extWayCategory;
	private GreenIndexGraphStorage _extGreenIndex;
	private NoiseIndexGraphStorage _extNoiseIndex;
	private TollwaysGraphStorage _extTollways;
	private TrailDifficultyScaleGraphStorage _extTrailDifficulty;
	private HillIndexGraphStorage _extHillIndex;
	private OsmIdGraphStorage _extOsmId;
	private RoadAccessRestrictionsGraphStorage _extRoadAccessRestrictions;
	private BordersGraphStorage _extCountryTraversalInfo;

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

	private RouteExtraInfo _roadAccessRestrictionsInfo;
	private RouteExtraInfoBuilder _roadAccessRestrictionsInfoBuilder;

	private RouteExtraInfo _countryTraversalInfo;
	private RouteExtraInfoBuilder _countryTraversalBuilder;

	private List<Integer> warningExtensions;

	private int _profileType = RoutingProfileType.UNKNOWN;
	private FlagEncoder _encoder;
	private boolean _encoderWithPriority = false;
	private byte[] buffer;
	private boolean _lastSegment;
	private static final Logger LOGGER = Logger.getLogger(ExtraInfoProcessor.class.getName());

	private CountryBordersReader cbreader;

	public ExtraInfoProcessor(PMap opts, GraphHopperStorage graphHopperStorage, FlagEncoder enc, CountryBordersReader cbReader) throws Exception {
		this(opts, graphHopperStorage, enc);
		this.cbreader = cbReader;
	}

	public ExtraInfoProcessor(PMap opts, GraphHopperStorage graphHopperStorage, FlagEncoder enc) throws Exception {
		_encoder = enc;
		_encoderWithPriority = _encoder.supports(PriorityWeighting.class);

		try {
			ORSPMap params = (ORSPMap)opts;

			int extraInfo = params.getInt("routing_extra_info", 0);
			_profileType = params.getInt("routing_profile_type", 0);
			ProfileParameters profileParameters = (ProfileParameters) params.getObj("routing_profile_params");
			boolean suppressWarnings = opts.getBool("routing_suppress_warnings", false);

			warningExtensions = new ArrayList<>();

			if(!suppressWarnings)
				applyWarningExtensions(graphHopperStorage);

			if (includeExtraInfo(extraInfo, RouteExtraInfoFlag.WayCategory))
			{
				_extWayCategory = GraphStorageUtils.getGraphExtension(graphHopperStorage, WayCategoryGraphStorage.class);

				if (_extWayCategory == null)
					throw new Exception("WayCategory storage is not found.");

				_wayCategoryInfo = new RouteExtraInfo("waycategory");
				_wayCategoryInfoBuilder = new AppendableRouteExtraInfoBuilder(_wayCategoryInfo);
			}

			if (includeExtraInfo(extraInfo, RouteExtraInfoFlag.Surface) || includeExtraInfo(extraInfo, RouteExtraInfoFlag.WayType))
			{
				_extWaySurface = GraphStorageUtils.getGraphExtension(graphHopperStorage, WaySurfaceTypeGraphStorage.class);

				if (_extWaySurface == null)
					throw new Exception("WaySurfaceType storage is not found.");

				if (includeExtraInfo(extraInfo, RouteExtraInfoFlag.Surface))
				{
					_surfaceInfo = new RouteExtraInfo("surface");
					_surfaceInfoBuilder = new AppendableRouteExtraInfoBuilder(_surfaceInfo);
				}
				if (includeExtraInfo(extraInfo, RouteExtraInfoFlag.WayType))
				{
					_wayTypeInfo = new RouteExtraInfo("waytypes");
					_wayTypeInfoBuilder = new AppendableRouteExtraInfoBuilder(_wayTypeInfo);
				}
			}

			if (includeExtraInfo(extraInfo, RouteExtraInfoFlag.Steepness))
			{
				_steepnessInfo = new RouteExtraInfo("steepness");
				_steepnessInfoBuilder = new AppendableSteepnessExtraInfoBuilder(_steepnessInfo);
			}

			if (includeExtraInfo(extraInfo, RouteExtraInfoFlag.Suitability))
			{
				_waySuitabilityInfo = new RouteExtraInfo("suitability");
				_waySuitabilityInfoBuilder = new AppendableRouteExtraInfoBuilder(_waySuitabilityInfo);
			}

			if (includeExtraInfo(extraInfo, RouteExtraInfoFlag.AvgSpeed))
			{
				_avgSpeedInfo = new RouteExtraInfo("avgspeed");
				_avgSpeedInfo.setFactor(10);
				_avgSpeedInfoBuilder = new AppendableRouteExtraInfoBuilder(_avgSpeedInfo);
			}

			if (includeExtraInfo(extraInfo, RouteExtraInfoFlag.Tollways))
			{
				_extTollways = GraphStorageUtils.getGraphExtension(graphHopperStorage, TollwaysGraphStorage.class);

				if (_extTollways == null)
					throw new Exception("Tollways storage is not found.");

				_tollwaysInfo = new RouteExtraInfo("tollways", _extTollways);
				_tollwaysInfoBuilder = new AppendableRouteExtraInfoBuilder(_tollwaysInfo);
				_tollwayExtractor = new TollwayExtractor(_extTollways, _profileType, profileParameters);
			}

			if (includeExtraInfo(extraInfo, RouteExtraInfoFlag.TrailDifficulty))
			{
				_extTrailDifficulty  = GraphStorageUtils.getGraphExtension(graphHopperStorage, TrailDifficultyScaleGraphStorage.class);
				_extHillIndex = GraphStorageUtils.getGraphExtension(graphHopperStorage, HillIndexGraphStorage.class);

				_trailDifficultyInfo = new RouteExtraInfo("traildifficulty");
				_trailDifficultyInfoBuilder = new AppendableRouteExtraInfoBuilder(_trailDifficultyInfo);
			}

			if (includeExtraInfo(extraInfo, RouteExtraInfoFlag.Green)) {
				_extGreenIndex = GraphStorageUtils.getGraphExtension(graphHopperStorage, GreenIndexGraphStorage.class);

				if (_extGreenIndex == null)
					throw new Exception("GreenIndex storage is not found.");
				_greenInfo = new RouteExtraInfo("green");
				_greenInfoBuilder = new AppendableRouteExtraInfoBuilder(_greenInfo);
			}

			if (includeExtraInfo(extraInfo, RouteExtraInfoFlag.Noise)) {

				_extNoiseIndex = GraphStorageUtils.getGraphExtension(graphHopperStorage, NoiseIndexGraphStorage.class);

				if (_extNoiseIndex == null)
					throw new Exception("NoiseIndex storage is not found.");
				_noiseInfo = new RouteExtraInfo("noise");
				_noiseInfoBuilder = new AppendableRouteExtraInfoBuilder(_noiseInfo);
			}

			if (includeExtraInfo(extraInfo, RouteExtraInfoFlag.OsmId)) {
				_extOsmId = GraphStorageUtils.getGraphExtension(graphHopperStorage, OsmIdGraphStorage.class);

				if(_extOsmId == null)
					throw new Exception("OsmId storage is not found");
				_osmIdInfo = new RouteExtraInfo("osmId");
				_osmIdInfoBuilder = new AppendableRouteExtraInfoBuilder(_osmIdInfo);
			}

			if (includeExtraInfo(extraInfo, RouteExtraInfoFlag.RoadAccessRestrictions)) {
				_extRoadAccessRestrictions = GraphStorageUtils.getGraphExtension(graphHopperStorage, RoadAccessRestrictionsGraphStorage.class);

				if(_extRoadAccessRestrictions == null)
					throw new Exception("RoadAccessRestrictions storage is not found");
				_roadAccessRestrictionsInfo = new RouteExtraInfo("roadaccessrestrictions", _extRoadAccessRestrictions);
				_roadAccessRestrictionsInfoBuilder = new AppendableRouteExtraInfoBuilder(_roadAccessRestrictionsInfo);
			}

			if (includeExtraInfo(extraInfo, RouteExtraInfoFlag.CountryInfo)) {
				_extCountryTraversalInfo = GraphStorageUtils.getGraphExtension(graphHopperStorage, BordersGraphStorage.class);
				if (_extCountryTraversalInfo != null) {
					_countryTraversalInfo = new RouteExtraInfo("countryinfo", _extCountryTraversalInfo);
					_countryTraversalBuilder = new AppendableRouteExtraInfoBuilder(_countryTraversalInfo);
				}
			}

		} catch (Exception ex) {
			LOGGER.error(ex);
		}

		buffer = new byte[4];
	}

	/**
	 * Loop through the GraphExtensions of the storage and store in the warningExtensions object those that implement
	 * the WarningGraphExtension interface and are set to be used for generating warnings.
	 *
	 * @param graphHopperStorage
	 */
	private void applyWarningExtensions(GraphHopperStorage graphHopperStorage) {
		GraphExtension[] extensions = GraphStorageUtils.getGraphExtensions(graphHopperStorage);
		for(GraphExtension ge : extensions) {
			if (ge instanceof WarningGraphExtension) {
				if(((WarningGraphExtension)ge).isUsedForWarning()) {
					warningExtensions.add(RouteExtraInfoFlag.getFromString(((WarningGraphExtension) ge).getName()));
				}
			}
		}
	}

	/**
	 * Check if the extra info should be included in the generation or not by looking at the encoded extras value and
	 * the list of warning extras.
	 *
	 * @param encodedExtras		The encoded value stating which extras were passed explicitly
	 * @param infoFlag			The id of the extra info whos inclusion needs to be decided
	 *
	 * @return
	 */
	private boolean includeExtraInfo(int encodedExtras, int infoFlag) {
		boolean include = false;

		if(RouteExtraInfoFlag.isSet(encodedExtras, infoFlag) || warningExtensions.contains(infoFlag))
			include = true;

		return include;
	}

	public List<RouteExtraInfo> getExtras()
	{
		List<RouteExtraInfo> extras = new ArrayList<RouteExtraInfo>();
		if (_surfaceInfo != null) {
			_surfaceInfoBuilder.finish();
			extras.add(_surfaceInfo);
		}
		if (_wayTypeInfo != null) {
			_wayTypeInfoBuilder.finish();
			extras.add(_wayTypeInfo);
		}
		if (_steepnessInfo != null) {
			_steepnessInfoBuilder.finish();
			extras.add(_steepnessInfo);
		}
		if (_waySuitabilityInfo != null) {
			_waySuitabilityInfoBuilder.finish();
			extras.add(_waySuitabilityInfo);
		}
		if (_wayCategoryInfo != null) {
			_wayCategoryInfoBuilder.finish();
			extras.add(_wayCategoryInfo);
		}
		if (_avgSpeedInfo != null) {
			_avgSpeedInfoBuilder.finish();
			extras.add(_avgSpeedInfo);
		}
		if (_greenInfo != null) {
			_greenInfoBuilder.finish();
			extras.add(_greenInfo);
		}
		if (_noiseInfo != null) {
			_noiseInfoBuilder.finish();
			extras.add(_noiseInfo);
		}
		if (_tollwaysInfo != null) {
			_tollwaysInfoBuilder.finish();
			extras.add(_tollwaysInfo);
		}
		if (_trailDifficultyInfo != null) {
			_trailDifficultyInfoBuilder.finish();
			extras.add(_trailDifficultyInfo);
		}
		if (_osmIdInfo != null) {
			_osmIdInfoBuilder.finish();
			extras.add(_osmIdInfo);
		}
		if (_roadAccessRestrictionsInfo != null) {
			_roadAccessRestrictionsInfoBuilder.finish();
			extras.add(_roadAccessRestrictionsInfo);
		}
		if (_countryTraversalInfo != null) {
			_countryTraversalBuilder.finish();
			extras.add(_countryTraversalInfo);
		}
		return extras;
	}

	public void appendData(ExtraInfoProcessor more) {
		if (_surfaceInfo != null)
			((AppendableRouteExtraInfoBuilder)_surfaceInfoBuilder).append((AppendableRouteExtraInfoBuilder)more._surfaceInfoBuilder);
		if (_wayTypeInfo != null)
			((AppendableRouteExtraInfoBuilder)_wayTypeInfoBuilder).append((AppendableRouteExtraInfoBuilder)more._wayTypeInfoBuilder);
		if (_steepnessInfo != null)
			((AppendableSteepnessExtraInfoBuilder)_steepnessInfoBuilder).append((AppendableSteepnessExtraInfoBuilder) more._steepnessInfoBuilder);
		if (_waySuitabilityInfo != null)
			((AppendableRouteExtraInfoBuilder)_waySuitabilityInfoBuilder).append((AppendableRouteExtraInfoBuilder)more._waySuitabilityInfoBuilder);
		if (_wayCategoryInfo != null)
			((AppendableRouteExtraInfoBuilder)_wayCategoryInfoBuilder).append((AppendableRouteExtraInfoBuilder)more._wayCategoryInfoBuilder);
		if (_avgSpeedInfo != null)
			((AppendableRouteExtraInfoBuilder)_avgSpeedInfoBuilder).append((AppendableRouteExtraInfoBuilder)more._avgSpeedInfoBuilder);
		if (_greenInfo != null)
			((AppendableRouteExtraInfoBuilder)_greenInfoBuilder).append((AppendableRouteExtraInfoBuilder)more._greenInfoBuilder);
		if (_noiseInfo != null)
			((AppendableRouteExtraInfoBuilder)_noiseInfoBuilder).append((AppendableRouteExtraInfoBuilder)more._noiseInfoBuilder);
		if (_tollwaysInfo != null)
			((AppendableRouteExtraInfoBuilder)_tollwaysInfoBuilder).append((AppendableRouteExtraInfoBuilder)more._tollwaysInfoBuilder);
		if (_trailDifficultyInfo != null)
			((AppendableRouteExtraInfoBuilder)_trailDifficultyInfoBuilder).append((AppendableRouteExtraInfoBuilder)more._trailDifficultyInfoBuilder);
		if (_osmIdInfo != null)
			((AppendableRouteExtraInfoBuilder)_osmIdInfoBuilder).append((AppendableRouteExtraInfoBuilder)more._osmIdInfoBuilder);
		if (_roadAccessRestrictionsInfo != null)
			((AppendableRouteExtraInfoBuilder)_roadAccessRestrictionsInfoBuilder).append((AppendableRouteExtraInfoBuilder)more._roadAccessRestrictionsInfoBuilder);
		if (_countryTraversalBuilder != null)
			((AppendableRouteExtraInfoBuilder)_countryTraversalBuilder).append((AppendableRouteExtraInfoBuilder)more._countryTraversalBuilder);
	}

	@Override
	public void processPathEdge(EdgeIteratorState edge, PointList geom) {
		double dist = edge.getDistance();

		// TODO Add extra info for crossed countries
		if (_extCountryTraversalInfo != null && cbreader != null) {
			short country1 = _extCountryTraversalInfo.getEdgeValue(EdgeIteratorStateHelper.getOriginalEdge(edge), BordersGraphStorage.Property.START);
			short country2 = _extCountryTraversalInfo.getEdgeValue(EdgeIteratorStateHelper.getOriginalEdge(edge), BordersGraphStorage.Property.END);
			// This check will correct the countries of an edge if the starting coordinate of the route lies in a different country than the start of the edge.
			if (country1 != country2 && geom.getSize() > 0) {
				Coordinate coordinate = new Coordinate();
				coordinate.x = geom.getLon(0);
				coordinate.y = geom.getLat(0);
				CountryBordersPolygon[] countries = cbreader.getCountry(coordinate);
				if (countries.length >= 1) {
					country1 = Short.parseShort(cbreader.getId(cbreader.getCountry(coordinate)[0].getName()));
				}
			}
			if (_countryTraversalBuilder != null && country1 != 0) {
				_countryTraversalBuilder.addSegment(country1, country1, geom, dist);
			}
		}

		if (_extWaySurface != null && _wayTypeInfo != null || _surfaceInfo != null)
		{
			WaySurfaceDescription wsd = _extWaySurface.getEdgeValue(EdgeIteratorStateHelper.getOriginalEdge(edge), buffer);

			if (_surfaceInfoBuilder != null)
				_surfaceInfoBuilder.addSegment(wsd.getSurfaceType(), wsd.getSurfaceType(), geom, dist);
			
			if (_wayTypeInfo != null)
				_wayTypeInfoBuilder.addSegment(wsd.getWayType(), wsd.getWayType(), geom, dist);
		}
		
		if (_wayCategoryInfoBuilder != null)
		{
			int value = _extWayCategory.getEdgeValue(EdgeIteratorStateHelper.getOriginalEdge(edge), buffer);
			_wayCategoryInfoBuilder.addSegment(value, value, geom, dist);
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
			
			_trailDifficultyInfoBuilder.addSegment(value, value, geom, dist);
		}
		
		if (_avgSpeedInfoBuilder != null)
		{
		    double speed = ((AbstractFlagEncoder)_encoder).getSpeed(edge.getFlags());
		    _avgSpeedInfoBuilder.addSegment(speed, (int)Math.round(speed*_avgSpeedInfo.getFactor()), geom, dist);
		}
		
		if (_tollwaysInfoBuilder != null)
		{
			int value = _tollwayExtractor.getValue(EdgeIteratorStateHelper.getOriginalEdge(edge));
		    _tollwaysInfoBuilder.addSegment(value, value, geom, dist);
		}

		if (_waySuitabilityInfoBuilder != null)
		{
			double priority = 0.3;
			int priorityIndex = 3;
			
			if (_encoderWithPriority)
			{
				priority = edge.get(_encoder.getDecimalEncodedValue(_encoder.toString()+"."+FlagEncoderKeys.PRIORITY_KEY));
				priorityIndex = (int)(3 + priority*PriorityCode.BEST.getValue()); // normalize values between 3 and 10
			}
			else
			{
				priority = ((AbstractFlagEncoder)_encoder).getSpeed(edge.getFlags()) / _encoder.getMaxSpeed();
				if (priority < 0.3)
					priority = 0.3;
				priorityIndex = (int)(priority * 10);
			}
			
			_waySuitabilityInfoBuilder.addSegment(priority, priorityIndex, geom, dist);
		}

		if (_greenInfoBuilder != null) {
			int value = _extGreenIndex.getEdgeValue(EdgeIteratorStateHelper.getOriginalEdge(edge), buffer);
			// This number is how many levels client can display in the stats bar
			// FIXME should be changed when the specific bar legend for green routing is finished
			int MIN_CLIENT_VAL = 3;
			int MAX_CLIENT_VAL = 10;
			int clientVal = MIN_CLIENT_VAL + value * (MAX_CLIENT_VAL - MIN_CLIENT_VAL + 1) / 64;
			_greenInfoBuilder.addSegment(value, clientVal, geom, dist);
		}
		
		if (_noiseInfoBuilder != null) {
			int noise_level = _extNoiseIndex.getEdgeValue(EdgeIteratorStateHelper.getOriginalEdge(edge), buffer);
			// convert the noise level (from 0 to 3) to the values (from 7 to 10) for the client
			if (noise_level > 3)
				noise_level = 3; 
			
			int client_noise_level = noise_level + 7;
			_noiseInfoBuilder.addSegment(noise_level, client_noise_level, geom, dist);
		}

		if (_osmIdInfoBuilder != null) {

			long osmId = _extOsmId.getEdgeValue(EdgeIteratorStateHelper.getOriginalEdge(edge));

			_osmIdInfoBuilder.addSegment((double)osmId, osmId, geom, dist);
		}

		if (_roadAccessRestrictionsInfoBuilder != null) {
			int value = _extRoadAccessRestrictions.getEdgeValue(EdgeIteratorStateHelper.getOriginalEdge(edge), buffer);
			_roadAccessRestrictionsInfoBuilder.addSegment(value, value, geom, dist);
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
}
