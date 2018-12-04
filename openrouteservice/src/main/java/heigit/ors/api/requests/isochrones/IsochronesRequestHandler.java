/*
 * This file is part of Openrouteservice.
 *
 * Openrouteservice is free software; you can redistribute it and/or modify it under the terms of the
 * GNU Lesser General Public License as published by the Free Software Foundation; either version 2.1
 * of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this library;
 * if not, see <https://www.gnu.org/licenses/>.
 */

package heigit.ors.api.requests.isochrones;

import com.vividsolutions.jts.geom.Coordinate;
import heigit.ors.api.requests.common.APIEnums;
import heigit.ors.api.requests.common.GenericHandler;
import heigit.ors.api.requests.routing.RouteRequestHandler;
import heigit.ors.api.requests.routing.RouteRequestOptions;
import heigit.ors.common.DistanceUnit;
import heigit.ors.common.StatusCode;
import heigit.ors.common.TravelRangeType;
import heigit.ors.common.TravellerInfo;
import heigit.ors.exceptions.InternalServerException;
import heigit.ors.exceptions.ParameterOutOfRangeException;
import heigit.ors.exceptions.ParameterValueException;
import heigit.ors.exceptions.StatusCodeException;
import heigit.ors.isochrones.*;
import heigit.ors.routing.RouteSearchParameters;
import heigit.ors.routing.RoutingProfileManager;
import heigit.ors.routing.RoutingProfileType;
import heigit.ors.services.isochrones.IsochronesServiceSettings;
import heigit.ors.util.DistanceUnitUtil;

import java.util.Arrays;
import java.util.List;

public class IsochronesRequestHandler extends GenericHandler {

    private IsochroneMapCollection isoMaps;
    private IsochroneRequest isochroneRequest;

    public IsochronesRequestHandler() {
        super();
        this.errorCodes.put("UNKNOWN_PARAMETER", IsochronesErrorCodes.UNKNOWN_PARAMETER);
        this.errorCodes.put("INVALID_JSON_FORMAT", IsochronesErrorCodes.INVALID_JSON_FORMAT);
        this.errorCodes.put("INVALID_PARAMETER_VALUE", IsochronesErrorCodes.INVALID_PARAMETER_VALUE);
    }

    public void generateIsochronesFromRequest(IsochronesRequest request) throws Exception {
        isochroneRequest = convertIsochroneRequest(request);
        // request object is built, now check if app config allows all settings
        List<TravellerInfo> travellers = isochroneRequest.getTravellers();

        // TODO where should we put the validation code?
        validateAgainstConfig(isochroneRequest, travellers);

        if (travellers.size() > 0) {
            String[] attrs = isochroneRequest.getAttributes();
            isoMaps = new IsochroneMapCollection();

            for (int i = 0; i < travellers.size(); ++i) {
                IsochroneSearchParameters searchParams = isochroneRequest.getSearchParameters(i);
                // searchParams.setAttributes(attrs);
                IsochroneMap isochroneMap = RoutingProfileManager.getInstance().buildIsochrone(searchParams);
                isoMaps.add(isochroneMap);
            }

        }
    }

    static Float convertSmoothing(Double smoothingValue) throws ParameterValueException {

        float f = (float) smoothingValue.doubleValue();

        if (smoothingValue < 0 || smoothingValue > 100)
            throw new ParameterValueException(IsochronesErrorCodes.INVALID_PARAMETER_VALUE, "smoothing", smoothingValue.toString());

        return f;

    }

    static String convertLocationType(IsochronesRequestEnums.LocationType locationType) throws ParameterValueException {

        IsochronesRequestEnums.LocationType value;

        switch (locationType) {

            case DESTINATION:

                value = IsochronesRequestEnums.LocationType.DESTINATION;

                break;

            case START:

                value = IsochronesRequestEnums.LocationType.START;

                break;

            default:

                throw new ParameterValueException(IsochronesErrorCodes.INVALID_PARAMETER_VALUE, "location_type", locationType.toString());
        }

        return value.toString();

    }

    static TravelRangeType convertRangeType(IsochronesRequestEnums.RangeType rangeType) throws ParameterValueException {

        TravelRangeType travelRangeType;

        switch (rangeType) {

            case DISTANCE:

                travelRangeType = TravelRangeType.Distance;

                break;
            case TIME:

                travelRangeType = TravelRangeType.Time;

                break;

            default:

                throw new ParameterValueException(IsochronesErrorCodes.INVALID_PARAMETER_VALUE, "range_type", rangeType.toString());
        }

        return travelRangeType;

    }

    static DistanceUnit convertAreaUnit(APIEnums.Units unitsIn) throws ParameterValueException {

        DistanceUnit are_unit;
        try {
            are_unit = DistanceUnitUtil.getFromString(unitsIn.toString(), DistanceUnit.Unknown);
            if (are_unit == DistanceUnit.Unknown)

                throw new ParameterValueException(IsochronesErrorCodes.INVALID_PARAMETER_VALUE, "are_unit", unitsIn.toString());

            return are_unit;

        } catch (Exception e) {
            throw new ParameterValueException(IsochronesErrorCodes.INVALID_PARAMETER_VALUE, "are_unit", unitsIn.toString());
        }
    }

    static DistanceUnit convertRangeUnit(APIEnums.Units unitsIn) throws ParameterValueException {

        DistanceUnit units;
        try {
            units = DistanceUnitUtil.getFromString(unitsIn.toString(), DistanceUnit.Unknown);
            if (units == DistanceUnit.Unknown)
                throw new ParameterValueException(IsochronesErrorCodes.INVALID_PARAMETER_VALUE, "range_unit", unitsIn.toString());
        } catch (Exception e) {
            throw new ParameterValueException(IsochronesErrorCodes.INVALID_PARAMETER_VALUE, "range_unit", unitsIn.toString());
        }
        return units;

    }

    static Coordinate convertSingleCoordinate(Double[] coordinate) throws ParameterValueException {
        Coordinate realCoordinate;
        if (coordinate.length != 2) {
            throw new ParameterValueException(IsochronesErrorCodes.INVALID_PARAMETER_VALUE, "location");
        }
        try {
            realCoordinate = new Coordinate(coordinate[0], coordinate[1]);
        } catch (Exception e) {
            throw new ParameterValueException(IsochronesErrorCodes.INVALID_PARAMETER_VALUE, "location");
        }
        return realCoordinate;
    }

    IsochroneRequest convertIsochroneRequest(IsochronesRequest request) throws Exception {

        IsochroneRequest isochroneRequest = new IsochroneRequest();
        for (IsochronesRequestTraveller traveller : request.getTravellers()) {
            TravellerInfo travellerInfo = constructTravellerInfo(traveller, request);
            try {
                isochroneRequest.addTraveller(travellerInfo);
            } catch (Exception ex) {
                throw new InternalServerException(IsochronesErrorCodes.UNKNOWN, "interval");
            }
        }
        if (request.hasId())
            isochroneRequest.setId(request.getId());
        isochroneRequest.setUnits(convertRangeUnit(request.getRangeUnits()).toString());
        isochroneRequest.setAreaUnits(convertAreaUnit(request.getAreaUnit()).toString());
        if (request.hasAttributes())
            isochroneRequest.setAttributes(convertAttributes(request.getAttributes()));
        if (request.hasSmoothing())
            isochroneRequest.setSmoothingFactor(convertSmoothing(request.getSmoothing()));
        if (request.getIntersections()) isochroneRequest.setIncludeIntersections(request.getIntersections());
        isochroneRequest.setCalcMethod(convertCalcMethod(request.getCalcMethod()));
        return isochroneRequest;

    }

    TravellerInfo constructTravellerInfo(IsochronesRequestTraveller traveller, IsochronesRequest request) throws StatusCodeException {
        TravellerInfo travellerInfo = new TravellerInfo();
        RouteSearchParameters routeSearchParameters = constructRouteSearchParameters(traveller, request);
        travellerInfo.setRouteSearchParameters(routeSearchParameters);
        if (traveller.hasId())
            travellerInfo.setId(traveller.getId());
        travellerInfo.setRangeType(convertRangeType(traveller.getRangeType()));

        travellerInfo.setLocationType(convertLocationType(traveller.getLocationType()));
        travellerInfo.setLocation(convertSingleCoordinate(traveller.getLocation()));
        travellerInfo.getRanges();
        //range + interval
        List<Double> rangeValues = traveller.getRange();
        Double intervalValue = traveller.getInterval();
        setRangeAndIntervals(travellerInfo, rangeValues, intervalValue);
        return travellerInfo;
    }

    RouteSearchParameters constructRouteSearchParameters(IsochronesRequestTraveller traveller, IsochronesRequest request) throws StatusCodeException {
        RouteSearchParameters routeSearchParameters = new RouteSearchParameters();
        int profileType;
        try {
            profileType = convertRouteProfileType(traveller.getProfile());
        } catch (Exception e) {
            throw new ParameterValueException(IsochronesErrorCodes.INVALID_PARAMETER_VALUE, "profile");
        }

        if (profileType == RoutingProfileType.UNKNOWN)
            throw new ParameterValueException(IsochronesErrorCodes.INVALID_PARAMETER_VALUE, "profile");
        routeSearchParameters.setProfileType(profileType);

        if (traveller.hasIsochronesOptions()) {
            routeSearchParameters = processIsochronesRequestOptions(traveller, routeSearchParameters);
        }
        routeSearchParameters.setConsiderTraffic(false);
        routeSearchParameters.setConsiderTurnRestrictions(false);
        return routeSearchParameters;
    }

    RouteSearchParameters processIsochronesRequestOptions(IsochronesRequestTraveller traveller, RouteSearchParameters parameters) throws StatusCodeException {
        RouteRequestOptions options = traveller.getIsochronesOptions();
        parameters = new RouteRequestHandler().processRequestOptions(options, parameters);
        if (options.hasProfileParams())
            parameters.setProfileParams(convertParameters(traveller, parameters.getProfileType()));
        return parameters;
    }

    void validateAgainstConfig(IsochroneRequest isochroneRequest, List<TravellerInfo> travellers) throws StatusCodeException {

        if (IsochronesServiceSettings.getAllowComputeArea() == false && isochroneRequest.hasAttribute("area"))
            throw new StatusCodeException(StatusCode.BAD_REQUEST, IsochronesErrorCodes.FEATURE_NOT_SUPPORTED, "Area computation is not enabled.");

        if (travellers.size() > IsochronesServiceSettings.getMaximumLocations())
            throw new ParameterOutOfRangeException(IsochronesErrorCodes.PARAMETER_VALUE_EXCEEDS_MAXIMUM, "locations", Integer.toString(travellers.size()), Integer.toString(IsochronesServiceSettings.getMaximumLocations()));

        for (int i = 0; i < travellers.size(); ++i) {
            TravellerInfo traveller = travellers.get(i);
            int maxAllowedRange = IsochronesServiceSettings.getMaximumRange(traveller.getRouteSearchParameters().getProfileType(), traveller.getRangeType());
            double maxRange = traveller.getMaximumRange();
            if (maxRange > maxAllowedRange)
                throw new ParameterOutOfRangeException(IsochronesErrorCodes.PARAMETER_VALUE_EXCEEDS_MAXIMUM, "range", Double.toString(maxRange), Integer.toString(maxAllowedRange));

            if (IsochronesServiceSettings.getMaximumIntervals() > 0) {
                if (IsochronesServiceSettings.getMaximumIntervals() < traveller.getRanges().length)
                    throw new ParameterOutOfRangeException(IsochronesErrorCodes.PARAMETER_VALUE_EXCEEDS_MAXIMUM, "range", Integer.toString(traveller.getRanges().length), Integer.toString(IsochronesServiceSettings.getMaximumIntervals()));
            }
        }

    }

    void setRangeAndIntervals(TravellerInfo travellerInfo, List<Double> rangeValues, Double intervalValue) throws ParameterValueException {
        double rangeValue = -1;
        if (rangeValues.size() == 1) {
            try {
                rangeValue = rangeValues.get(0);
                travellerInfo.setRanges(new double[]{rangeValue});
            } catch (NumberFormatException ex) {
                throw new ParameterValueException(IsochronesErrorCodes.INVALID_PARAMETER_FORMAT, "range");
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
        if (rangeValues.size() == 1 && rangeValue != -1)
            travellerInfo.setRanges(rangeValue, intervalValue);

    }

    String[] convertAttributes(IsochronesRequestEnums.Attributes[] attributes) {
        return convertAPIEnumListToStrings(attributes);
    }

    String convertCalcMethod(String bareCalcMethod) throws ParameterValueException {
        try {
            switch (bareCalcMethod.toLowerCase()) {
                case "concaveballs":
                    return "concaveballs";
                case "grid":
                    return "grid";
                default:
                    return "none";
            }
        } catch (Exception ex) {
            throw new ParameterValueException(IsochronesErrorCodes.INVALID_PARAMETER_VALUE, "calc_method");
        }
    }

    public IsochroneMapCollection getIsoMaps() {
        return isoMaps;
    }

    public IsochroneRequest getIsochroneRequest() {
        return isochroneRequest;
    }

}
