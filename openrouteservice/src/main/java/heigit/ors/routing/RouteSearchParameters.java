package heigit.ors.routing;

import java.text.ParseException;

import org.json.JSONObject;

import com.graphhopper.util.Helper;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;

import heigit.ors.exceptions.ParameterValueException;
import heigit.ors.exceptions.UnknownParameterValueException;
import heigit.ors.geojson.GeometryJSON;
import heigit.ors.routing.graphhopper.extensions.HeavyVehicleAttributes;
import heigit.ors.routing.graphhopper.extensions.VehicleLoadCharacteristicsFlags;
import heigit.ors.routing.graphhopper.extensions.WheelchairTypesEncoder;
import heigit.ors.routing.parameters.*;
import heigit.ors.util.StringUtility;

public class RouteSearchParameters {
	private int _profileType;
	private int _weightingMethod = WeightingMethod.FASTEST;
	private Boolean _considerTraffic = false;
	private Boolean _considerTurnRestrictions = false;
	private double _maxSpeed = -1;
	private Polygon[] _avoidAreas;
	private int _avoidFeaturesTypes;
	private int _vehicleType = HeavyVehicleAttributes.GOODS;
	private ProfileParameters _profileParams;

	private String _options;

	public int getProfileType() {
		return _profileType;
	}

	public void setProfileType(int profileType) throws Exception {
		if (profileType == RoutingProfileType.UNKNOWN)
			throw new Exception("Routing profile is unknown.");

		this._profileType = profileType;
	}

	public double getMaximumSpeed() {
		return _maxSpeed;
	}

	public void setMaximumSpeed(double maxSpeed) {
		_maxSpeed = maxSpeed;
	}

	public int getWeightingMethod() {
		return _weightingMethod;
	}

	public void setWeightingMethod(int weightingMethod) {
		_weightingMethod = weightingMethod;
	}

	public Boolean getConsiderTraffic() {
		return _considerTraffic;
	}

	public void setConsiderTraffic(Boolean _considerTraffic) {
		this._considerTraffic = _considerTraffic;
	}

	public Polygon[] getAvoidAreas() {
		return _avoidAreas;
	}

	public void setAvoidAreas(Polygon[] avoidAreas) {
		_avoidAreas = avoidAreas;
	}

	public boolean hasAvoidAreas()
	{
		return _avoidAreas != null && _avoidAreas.length > 0;
	}

	public int getAvoidFeatureTypes() {
		return _avoidFeaturesTypes;
	}

	public void setAvoidFeatureTypes(int avoidFeatures) {
		_avoidFeaturesTypes = avoidFeatures;
	}

	public boolean hasAvoidFeatures()
	{
		return  _avoidFeaturesTypes > 0;
	}

	public Boolean getConsiderTurnRestrictions() {
		return _considerTurnRestrictions;
	}

	public void setConsiderTurnRestrictions(Boolean considerTurnRestrictions) {
		_considerTurnRestrictions = considerTurnRestrictions;
	}

	public int getVehicleType() {
		return _vehicleType;
	}

	public void setVehicleType(int vehicleType) {
		this._vehicleType = vehicleType;
	}

	public String getOptions()
	{
		return _options;
	}

	public void setOptions(String options) throws Exception {
		if (options == null)
			return;

		_options = StringUtility.trim(options, '\"');

		//////////////
		// FIXME Only for debugging!!
		// This option for green routing should be constructed by the client
//		_options = "{\"profile_params\":{\"green_routing\":true}}"
		//////////////

		JSONObject json = null;
		try
		{
			json =	new JSONObject(_options);
		}
		catch(Exception ex)
		{
			throw new ParseException(ex.getMessage(), 0);
		}

		if (json.has("maximum_speed"))
		{
			try
			{
			   _maxSpeed = json.getDouble("maximum_speed");
			}
			catch(Exception ex)
			{
				throw new ParameterValueException(RoutingErrorCodes.INVALID_PARAMETER_FORMAT, "maximum_speed", json.getString("maximum_speed"));
			}
		}

		if (json.has("avoid_features"))
		{
			String keyValue = json.getString("avoid_features");
			if (!Helper.isEmpty(keyValue))
			{
				String[]  avoidFeatures = keyValue.split("\\|");
				if (avoidFeatures != null && avoidFeatures.length > 0)
				{
					int flags = 0;
					for (int i = 0; i < avoidFeatures.length; i++)
					{
						String featName = avoidFeatures[i];
						if (featName != null)
						{
							int flag = AvoidFeatureFlags.getFromString(featName);
							if (flag == 0)
								throw new UnknownParameterValueException(RoutingErrorCodes.INVALID_PARAMETER_VALUE, "avoid_features", featName);

							if (!AvoidFeatureFlags.isValid(_profileType, flag))
								throw new ParameterValueException(RoutingErrorCodes.INVALID_PARAMETER_VALUE, "avoid_features", featName);
							
							flags |= flag;
						}
					}

					if (flags != 0)
						_avoidFeaturesTypes = flags;

				}
			}
		}

		if (json.has("profile_params"))
		{
			if (RoutingProfileType.isCycling(_profileType))
			{
				CyclingParameters cyclingParams = new CyclingParameters();

				JSONObject jFitnessParams = json.getJSONObject("profile_params");

				if (jFitnessParams.has("difficulty_level"))
					cyclingParams.setDifficultyLevel(jFitnessParams.getInt("difficulty_level"));
				if (jFitnessParams.has("maximum_gradient"))
					cyclingParams.setMaximumGradient(jFitnessParams.getInt("maximum_gradient"));

				_profileParams = cyclingParams;
			}
			else if (RoutingProfileType.isWalking(_profileType)) {
			    WalkingParameters walkingParams = new WalkingParameters();
			    JSONObject walkingProfileParams = json.getJSONObject("profile_params");
			    if (walkingProfileParams.has("green_routing"))
			    	walkingParams.setGreenRouting(walkingProfileParams.getBoolean("green_routing"));
			    if (walkingProfileParams.has("quiet_routing"))
			    	walkingParams.setQuietRouting(walkingProfileParams.getBoolean("quiet_routing"));
			    if (walkingProfileParams.has("difficulty_level"))
			    	walkingParams.setDifficultyLevel(walkingProfileParams.getInt("difficulty_level"));
			    if (walkingProfileParams.has("maximum_gradient"))
			    	walkingParams.setMaximumGradient(walkingProfileParams.getInt("maximum_gradient"));
			    if (walkingProfileParams.has("green_weighting_factor"))
			    	walkingParams.setGreenWeightingFactor(walkingProfileParams.getDouble("green_weighting_factor"));
			    if (walkingProfileParams.has("quiet_weighting_factor"))
			    	walkingParams.setQuietWeightingFactor(walkingProfileParams.getDouble("quiet_weighting_factor"));

			    _profileParams = walkingParams;
			}
			else if (RoutingProfileType.isHeavyVehicle(_profileType) == true)
			{
				VehicleParameters vehicleParams = new VehicleParameters();

				if (json.has("vehicle_type"))
				{
					String vehicleType = json.getString("vehicle_type");
					_vehicleType =  HeavyVehicleAttributes.getFromString(vehicleType);

					JSONObject jVehicleParams = json.getJSONObject("profile_params");

					if (jVehicleParams.has("length"))
						vehicleParams.setLength(jVehicleParams.getDouble("length"));

					if (jVehicleParams.has("width"))
						vehicleParams.setWidth(jVehicleParams.getDouble("width"));

					if (jVehicleParams.has("height"))
						vehicleParams.setHeight(jVehicleParams.getDouble("height"));

					if (jVehicleParams.has("weight"))
						vehicleParams.setWeight(jVehicleParams.getDouble("weight"));

					if (jVehicleParams.has("axleload"))
						vehicleParams.setAxleload(jVehicleParams.getDouble("axleload"));

					int loadCharacteristics = 0;
					if (jVehicleParams.has("hazmat") && jVehicleParams.getBoolean("hazmat") == true)
						loadCharacteristics |= VehicleLoadCharacteristicsFlags.HAZMAT;

					if (loadCharacteristics != 0)
						vehicleParams.setLoadCharacteristics(loadCharacteristics);
				}

				_profileParams = vehicleParams;
			}
			else if (_profileType == RoutingProfileType.WHEELCHAIR)
			{
				WheelchairParameters wheelchairParams = new WheelchairParameters();

				JSONObject jWheelchairParams = json.getJSONObject("profile_params");

				if (jWheelchairParams.has("surface_type"))
					wheelchairParams.setSurfaceType(WheelchairTypesEncoder.getSurfaceType(jWheelchairParams.getString("surface_type")));

				if (jWheelchairParams.has("track_type"))
					wheelchairParams.setTrackType(WheelchairTypesEncoder.getTrackType(jWheelchairParams.getString("track_type")));

				if (jWheelchairParams.has("smoothness_type"))
					wheelchairParams.setSmoothnessType(WheelchairTypesEncoder.getSmoothnessType(jWheelchairParams.getString("smoothness_type")));

				if (jWheelchairParams.has("maximum_sloped_curb"))
					wheelchairParams.setMaximumSlopedCurb((float)jWheelchairParams.getDouble("maximum_sloped_curb"));

				if (jWheelchairParams.has("maximum_incline"))
					wheelchairParams.setMaximumIncline((float)jWheelchairParams.getDouble("maximum_incline"));

				_profileParams = wheelchairParams;
			}
		}

		if (json.has("avoid_polygons"))
		{
			JSONObject jFeature = (JSONObject)json.get("avoid_polygons");

			Geometry geom = null;
			try
			{
			   geom = GeometryJSON.parse(jFeature);
			}
			catch(Exception ex)
			{
				throw new ParameterValueException(RoutingErrorCodes.INVALID_JSON_FORMAT, "avoid_polygons");
			}

			if (geom instanceof Polygon)
			{
				_avoidAreas = new Polygon[] { (Polygon)geom };
			}
			else if (geom instanceof MultiPolygon)
			{
				MultiPolygon multiPoly = (MultiPolygon)geom;
				_avoidAreas = new Polygon[multiPoly.getNumGeometries()];
				for (int i = 0; i < multiPoly.getNumGeometries(); i++)
					_avoidAreas[i] = (Polygon)multiPoly.getGeometryN(i);
			}
			else
			{
				throw new ParameterValueException(RoutingErrorCodes.INVALID_PARAMETER_VALUE, "avoid_polygons");
			}
		}
	}

	public boolean hasParameters(Class<?> value)
	{
		if (_profileParams == null)
			return false;

		return _profileParams.getClass() == value;
	}

	public ProfileParameters getProfileParameters()
	{
		return _profileParams;
	}
}
