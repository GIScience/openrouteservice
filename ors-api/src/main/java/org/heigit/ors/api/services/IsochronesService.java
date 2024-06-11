package org.heigit.ors.api.services;

import org.heigit.ors.api.EndpointsProperties;
import org.heigit.ors.api.requests.isochrones.IsochronesRequest;
import org.heigit.ors.api.requests.isochrones.IsochronesRequestEnums;
import org.heigit.ors.api.requests.routing.RouteRequestOptions;
import org.heigit.ors.common.DistanceUnit;
import org.heigit.ors.common.StatusCode;
import org.heigit.ors.common.TravelRangeType;
import org.heigit.ors.common.TravellerInfo;
import org.heigit.ors.exceptions.InternalServerException;
import org.heigit.ors.exceptions.ParameterOutOfRangeException;
import org.heigit.ors.exceptions.ParameterValueException;
import org.heigit.ors.exceptions.StatusCodeException;
import org.heigit.ors.fastisochrones.partitioning.FastIsochroneFactory;
import org.heigit.ors.isochrones.*;
import org.heigit.ors.isochrones.statistics.StatisticsProviderConfiguration;
import org.heigit.ors.api.APIEnums;
import org.heigit.ors.routing.RouteSearchParameters;
import org.heigit.ors.routing.RoutingProfileManager;
import org.heigit.ors.routing.RoutingProfileType;
import org.heigit.ors.util.DistanceUnitUtil;
import org.locationtech.jts.geom.Coordinate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.heigit.ors.api.requests.isochrones.IsochronesRequest.convertAttributes;
import static org.heigit.ors.api.requests.isochrones.IsochronesRequest.convertToIsochronesProfileType;
import static org.heigit.ors.api.requests.isochrones.IsochronesRequestEnums.CalculationMethod.CONCAVE_BALLS;
import static org.heigit.ors.api.requests.isochrones.IsochronesRequestEnums.CalculationMethod.FASTISOCHRONE;
import static org.heigit.ors.common.TravelRangeType.DISTANCE;

@Service
public class IsochronesService extends ApiService {

    @Autowired
    public IsochronesService(EndpointsProperties endpointsProperties) {
        this.endpointsProperties = endpointsProperties;
    }

    public void generateIsochronesFromRequest(IsochronesRequest isochronesRequest) throws Exception {
        isochronesRequest.setIsochroneRequest(convertIsochroneRequest(isochronesRequest));
        // request object is built, now check if ors config allows all settings
        List<TravellerInfo> travellers = isochronesRequest.getIsochroneRequest().getTravellers();

        // TODO REFACTORING where should we put the validation code?
        validateAgainstConfig(isochronesRequest.getIsochroneRequest(), travellers);

        if (!travellers.isEmpty()) {
            isochronesRequest.setIsoMaps(new IsochroneMapCollection());

            for (int i = 0; i < travellers.size(); ++i) {
                IsochroneSearchParameters searchParams = isochronesRequest.getIsochroneRequest().getSearchParameters(i);
                IsochroneMap isochroneMap = RoutingProfileManager.getInstance().buildIsochrone(searchParams);
                isochronesRequest.getIsoMaps().add(isochroneMap);
            }

        }
    }

    Float convertSmoothing(Double smoothingValue) throws ParameterValueException {
        float f = (float) smoothingValue.doubleValue();

        if (smoothingValue < 0 || smoothingValue > 100)
            throw new ParameterValueException(IsochronesErrorCodes.INVALID_PARAMETER_VALUE, IsochronesRequest.PARAM_SMOOTHING, smoothingValue.toString());

        return f;
    }

    String convertLocationType(IsochronesRequestEnums.LocationType locationType) throws ParameterValueException {
        IsochronesRequestEnums.LocationType value;

        switch (locationType) {
            case DESTINATION:
                value = IsochronesRequestEnums.LocationType.DESTINATION;
                break;
            case START:
                value = IsochronesRequestEnums.LocationType.START;
                break;
            default:
                throw new ParameterValueException(IsochronesErrorCodes.INVALID_PARAMETER_VALUE, IsochronesRequest.PARAM_LOCATION_TYPE, locationType.toString());
        }

        return value.toString();
    }

    TravelRangeType convertRangeType(IsochronesRequestEnums.RangeType rangeType) throws ParameterValueException {
        TravelRangeType travelRangeType;

        switch (rangeType) {
            case DISTANCE:
                travelRangeType = TravelRangeType.DISTANCE;
                break;
            case TIME:
                travelRangeType = TravelRangeType.TIME;
                break;
            default:
                throw new ParameterValueException(IsochronesErrorCodes.INVALID_PARAMETER_VALUE, IsochronesRequest.PARAM_RANGE_TYPE, rangeType.toString());
        }

        return travelRangeType;

    }

    String convertAreaUnit(APIEnums.Units unitsIn) throws ParameterValueException {

        DistanceUnit convertedAreaUnit;
        try {
            convertedAreaUnit = DistanceUnitUtil.getFromString(unitsIn.toString(), DistanceUnit.UNKNOWN);
            if (convertedAreaUnit == DistanceUnit.UNKNOWN)
                throw new ParameterValueException(IsochronesErrorCodes.INVALID_PARAMETER_VALUE, IsochronesRequest.PARAM_AREA_UNITS, unitsIn.toString());

            return DistanceUnitUtil.toString(convertedAreaUnit);

        } catch (Exception e) {
            throw new ParameterValueException(IsochronesErrorCodes.INVALID_PARAMETER_VALUE, IsochronesRequest.PARAM_AREA_UNITS, unitsIn.toString());
        }
    }

    String convertRangeUnit(APIEnums.Units unitsIn) throws ParameterValueException {

        DistanceUnit units;
        try {
            units = DistanceUnitUtil.getFromString(unitsIn.toString(), DistanceUnit.UNKNOWN);
            if (units == DistanceUnit.UNKNOWN)
                throw new ParameterValueException(IsochronesErrorCodes.INVALID_PARAMETER_VALUE, IsochronesRequest.PARAM_RANGE_UNITS, unitsIn.toString());
        } catch (Exception e) {
            throw new ParameterValueException(IsochronesErrorCodes.INVALID_PARAMETER_VALUE, IsochronesRequest.PARAM_RANGE_UNITS, unitsIn.toString());
        }
        return DistanceUnitUtil.toString(units);

    }

    Coordinate convertSingleCoordinate(Double[] coordinate) throws ParameterValueException {
        Coordinate realCoordinate;
        if (coordinate.length != 2) {
            throw new ParameterValueException(IsochronesErrorCodes.INVALID_PARAMETER_VALUE, IsochronesRequest.PARAM_LOCATIONS);
        }
        try {
            realCoordinate = new Coordinate(coordinate[0], coordinate[1]);
        } catch (Exception e) {
            throw new ParameterValueException(IsochronesErrorCodes.INVALID_PARAMETER_VALUE, IsochronesRequest.PARAM_LOCATIONS);
        }
        return realCoordinate;
    }

    IsochroneRequest convertIsochroneRequest(IsochronesRequest isochronesRequest) throws Exception {
        IsochroneRequest convertedIsochroneRequest = new IsochroneRequest();
        EndpointsProperties.EndpointIsochronesProperties isochroneProperties = endpointsProperties.getIsochrones();
        convertedIsochroneRequest.setMaximumLocations(isochroneProperties.getMaximumLocations());
        convertedIsochroneRequest.setAllowComputeArea(isochroneProperties.isAllowComputeArea());
        convertedIsochroneRequest.setMaximumIntervals(isochroneProperties.getMaximumIntervals());
        convertedIsochroneRequest.setMaximumRangeDistanceDefault(isochroneProperties.getMaximumRangeDistanceDefault());
        convertedIsochroneRequest.setProfileMaxRangeDistances(isochroneProperties.getProfileMaxRangeDistances());
        convertedIsochroneRequest.setMaximumRangeDistanceDefaultFastisochrones(isochroneProperties.getFastisochrones().getMaximumRangeDistanceDefault());
        convertedIsochroneRequest.setProfileMaxRangeDistancesFastisochrones(isochroneProperties.getFastisochrones().getProfileMaxRangeDistances());
        convertedIsochroneRequest.setMaximumRangeTimeDefault(isochroneProperties.getMaximumRangeTimeDefault());
        convertedIsochroneRequest.setProfileMaxRangeTimes(isochroneProperties.getProfileMaxRangeTimes());
        convertedIsochroneRequest.setMaximumRangeTimeDefaultFastisochrones(isochroneProperties.getFastisochrones().getMaximumRangeTimeDefault());
        convertedIsochroneRequest.setProfileMaxRangeTimesFastisochrones(isochroneProperties.getFastisochrones().getProfileMaxRangeTimes());
        convertedIsochroneRequest.setStatsProviders(constructStatisticsProvidersConfiguration(isochroneProperties.getStatisticsProviders()));

        for (int i = 0; i < isochronesRequest.getLocations().length; i++) {
            Double[] location = isochronesRequest.getLocations()[i];
            TravellerInfo travellerInfo = this.constructTravellerInfo(isochronesRequest, location);
            travellerInfo.setId(Integer.toString(i));
            try {
                convertedIsochroneRequest.addTraveller(travellerInfo);
            } catch (Exception ex) {
                throw new InternalServerException(IsochronesErrorCodes.UNKNOWN, IsochronesRequest.PARAM_INTERVAL);
            }
        }
        if (isochronesRequest.hasId())
            convertedIsochroneRequest.setId(isochronesRequest.getId());
        if (isochronesRequest.hasRangeUnits())
            convertedIsochroneRequest.setUnits(convertRangeUnit(isochronesRequest.getRangeUnit()));
        if (isochronesRequest.hasAreaUnits())
            convertedIsochroneRequest.setAreaUnits(convertAreaUnit(isochronesRequest.getAreaUnit()));
        if (isochronesRequest.hasAttributes())
            convertedIsochroneRequest.setAttributes(convertAttributes(isochronesRequest.getAttributes()));
        if (isochronesRequest.hasSmoothing())
            convertedIsochroneRequest.setSmoothingFactor(convertSmoothing(isochronesRequest.getSmoothing()));
        if (isochronesRequest.hasIntersections())
            convertedIsochroneRequest.setIncludeIntersections(isochronesRequest.getIntersections());
        if (isochronesRequest.hasOptions())
            convertedIsochroneRequest.setCalcMethod(convertCalcMethod(CONCAVE_BALLS));
        else
            convertedIsochroneRequest.setCalcMethod(convertCalcMethod(FASTISOCHRONE));
        return convertedIsochroneRequest;

    }

    Map<String, StatisticsProviderConfiguration> constructStatisticsProvidersConfiguration(Map<String, EndpointsProperties.EndpointIsochronesProperties.StatisticsProviderProperties> statsProperties) {
        Map<String, StatisticsProviderConfiguration> statsProviders = new HashMap<>();

        if (statsProperties != null) {
            int id = 0;
            for (EndpointsProperties.EndpointIsochronesProperties.StatisticsProviderProperties providerProperties : statsProperties.values()) {
                Map<String, String> propertyMapping = providerProperties.getPropertyMapping();
                Map<String, String> propMapping = new HashMap<>();
                for (Map.Entry<String, String> propEntry : propertyMapping.entrySet())
                    propMapping.put(propEntry.getValue(), propEntry.getKey());
                if (propMapping.size() > 0) {
                    StatisticsProviderConfiguration provConfig = new StatisticsProviderConfiguration(id++, providerProperties.getProviderName(), providerProperties.getProviderParameters(), propMapping, providerProperties.getAttribution());
                    for (Map.Entry<String, String> property : propMapping.entrySet())
                        statsProviders.put(property.getKey().toLowerCase(), provConfig);
                }
            }
        }

        return statsProviders;
    }

    TravellerInfo constructTravellerInfo(IsochronesRequest isochronesRequest, Double[] coordinate) throws Exception {
        TravellerInfo travellerInfo = new TravellerInfo();

        RouteSearchParameters routeSearchParameters = constructRouteSearchParameters(isochronesRequest);
        travellerInfo.setRouteSearchParameters(routeSearchParameters);
        if (isochronesRequest.hasRangeType())
            travellerInfo.setRangeType(convertRangeType(isochronesRequest.getRangeType()));
        if (isochronesRequest.hasLocationType())
            travellerInfo.setLocationType(convertLocationType(isochronesRequest.getLocationType()));
        travellerInfo.setLocation(convertSingleCoordinate(coordinate));
        travellerInfo.getRanges();
        //range + interval
        if (isochronesRequest.getRange() == null) {
            throw new ParameterValueException(IsochronesErrorCodes.MISSING_PARAMETER, IsochronesRequest.PARAM_RANGE);
        }
        List<Double> rangeValues = isochronesRequest.getRange();
        Double intervalValue = isochronesRequest.getInterval();
        setRangeAndIntervals(travellerInfo, rangeValues, intervalValue);
        return travellerInfo;
    }

    RouteSearchParameters constructRouteSearchParameters(IsochronesRequest isochronesRequest) throws Exception {
        RouteSearchParameters routeSearchParameters = new RouteSearchParameters();
        int profileType;
        try {
            profileType = convertToIsochronesProfileType(isochronesRequest.getProfile());
        } catch (Exception e) {
            throw new ParameterValueException(IsochronesErrorCodes.INVALID_PARAMETER_VALUE, IsochronesRequest.PARAM_PROFILE);
        }

        if (profileType == RoutingProfileType.UNKNOWN)
            throw new ParameterValueException(IsochronesErrorCodes.INVALID_PARAMETER_VALUE, IsochronesRequest.PARAM_PROFILE);
        routeSearchParameters.setProfileType(profileType);

        if (isochronesRequest.hasOptions()) {
            routeSearchParameters = processIsochronesRequestOptions(isochronesRequest, routeSearchParameters);
        }
        if (isochronesRequest.hasTime()) {
            routeSearchParameters.setDeparture(isochronesRequest.getTime());
            routeSearchParameters.setArrival(isochronesRequest.getTime());
        }
        routeSearchParameters.setConsiderTurnRestrictions(false);
        return routeSearchParameters;
    }

    RouteSearchParameters processIsochronesRequestOptions(IsochronesRequest isochronesRequest, RouteSearchParameters parameters) throws StatusCodeException {
        RouteRequestOptions options = isochronesRequest.getIsochronesOptions();
        parameters = processRequestOptions(options, parameters);
        if (options.hasProfileParams())
            parameters.setProfileParams(convertParameters(options, parameters.getProfileType()));
        return parameters;
    }

    void validateAgainstConfig(IsochroneRequest isochroneRequest, List<TravellerInfo> travellers) throws StatusCodeException {
        if (!isochroneRequest.isAllowComputeArea() && isochroneRequest.hasAttribute("area"))
            throw new StatusCodeException(StatusCode.BAD_REQUEST, IsochronesErrorCodes.FEATURE_NOT_SUPPORTED, "Area computation is not enabled.");

        if (travellers.size() > isochroneRequest.getMaximumLocations())
            throw new ParameterOutOfRangeException(IsochronesErrorCodes.PARAMETER_VALUE_EXCEEDS_MAXIMUM, IsochronesRequest.PARAM_LOCATIONS, Integer.toString(travellers.size()), Integer.toString(isochroneRequest.getMaximumLocations()));

        for (TravellerInfo traveller : travellers) {
            int maxAllowedRange = getMaximumRange(traveller, isochroneRequest);
            double maxRange = traveller.getMaximumRange();
            if (maxRange > maxAllowedRange)
                throw new ParameterOutOfRangeException(IsochronesErrorCodes.PARAMETER_VALUE_EXCEEDS_MAXIMUM, IsochronesRequest.PARAM_RANGE, Double.toString(maxRange), Integer.toString(maxAllowedRange));

            int maxIntervals = isochroneRequest.getMaximumIntervals();
            if (maxIntervals > 0 && maxIntervals < traveller.getRanges().length) {
                throw new ParameterOutOfRangeException(IsochronesErrorCodes.PARAMETER_VALUE_EXCEEDS_MINIMUM, IsochronesRequest.PARAM_INTERVAL, "Resulting number of " + traveller.getRanges().length + " isochrones exceeds maximum value of " + maxIntervals + ".");
            }
        }

    }

    private static int getMaximumRange(TravellerInfo traveller, IsochroneRequest isochroneRequest) {
        int profileType = traveller.getRouteSearchParameters().getProfileType();
        TravelRangeType range = traveller.getRangeType();
        String calcMethod = isochroneRequest.getCalcMethod();
        Integer res;

        RoutingProfileManager rpm = RoutingProfileManager.getInstance();
        FastIsochroneFactory fastIsochroneFactory = rpm.getProfiles().getRouteProfile(profileType).getGraphhopper().getFastIsochroneFactory();
        if (fastIsochroneFactory.isEnabled() && calcMethod.equalsIgnoreCase("fastisochrone"))
            return getMaximumRangeFastIsochrone(traveller, isochroneRequest);

        if (range == DISTANCE) {
            res = isochroneRequest.getProfileMaxRangeDistances().get(profileType);
            if (res == null)
                res = isochroneRequest.getMaximumRangeDistanceDefault();
        } else {
            res = isochroneRequest.getProfileMaxRangeTimes().get(profileType);
            if (res == null)
                res = isochroneRequest.getMaximumRangeTimeDefault();
        }

        return res;
    }

    private static int getMaximumRangeFastIsochrone(TravellerInfo traveller, IsochroneRequest isochroneRequest) {
        int profileType = traveller.getRouteSearchParameters().getProfileType();
        TravelRangeType range = traveller.getRangeType();
        Integer res;

        if (range == DISTANCE) {
            res = isochroneRequest.getProfileMaxRangeDistancesFastisochrones().get(profileType);
            if (res == null)
                res = isochroneRequest.getMaximumRangeDistanceDefaultFastisochrones();
        } else {
            res = isochroneRequest.getProfileMaxRangeTimesFastisochrones().get(profileType);
            if (res == null)
                res = isochroneRequest.getMaximumRangeTimeDefaultFastisochrones();
        }

        return res;
    }

    void setRangeAndIntervals(TravellerInfo travellerInfo, List<Double> rangeValues, Double intervalValue) throws ParameterValueException, ParameterOutOfRangeException {
        double rangeValue = -1;
        if (rangeValues.size() == 1) {
            try {
                rangeValue = rangeValues.get(0);
                travellerInfo.setRanges(new double[]{rangeValue});
            } catch (NumberFormatException ex) {
                throw new ParameterValueException(IsochronesErrorCodes.INVALID_PARAMETER_VALUE, "range");
            }
        } else {
            double[] ranges = new double[rangeValues.size()];
            double maxRange = Double.MIN_VALUE;
            for (int i = 0; i < ranges.length; i++) {
                double dv = rangeValues.get(i);
                if (dv > maxRange)
                    maxRange = dv;
                ranges[i] = dv;
            }
            Arrays.sort(ranges);
            travellerInfo.setRanges(ranges);
        }
        // interval, only use if one range is defined

        if (rangeValues.size() == 1 && rangeValue != -1 && intervalValue != null) {
            if (intervalValue > rangeValue) {
                throw new ParameterOutOfRangeException(IsochronesErrorCodes.PARAMETER_VALUE_EXCEEDS_MAXIMUM, IsochronesRequest.PARAM_INTERVAL, Double.toString(intervalValue), Double.toString(rangeValue));
            }
            travellerInfo.setRanges(rangeValue, intervalValue);
        }
    }

    String convertCalcMethod(IsochronesRequestEnums.CalculationMethod bareCalcMethod) throws ParameterValueException {
        try {
            switch (bareCalcMethod) {
                case CONCAVE_BALLS:
                    return "concaveballs";
                case GRID:
                    return "grid";
                case FASTISOCHRONE:
                    return "fastisochrone";
                default:
                    return "none";
            }
        } catch (Exception ex) {
            throw new ParameterValueException(IsochronesErrorCodes.INVALID_PARAMETER_VALUE, "calc_method");
        }
    }

}
