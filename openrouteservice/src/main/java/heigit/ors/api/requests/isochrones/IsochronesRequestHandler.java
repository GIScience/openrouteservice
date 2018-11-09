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

import com.graphhopper.util.Helper;
import com.vividsolutions.jts.geom.Coordinate;
import heigit.ors.api.requests.common.APIEnums;
import heigit.ors.api.requests.common.GenericHandler;
import heigit.ors.common.DistanceUnit;
import heigit.ors.common.TravelRangeType;
import heigit.ors.common.TravellerInfo;
import heigit.ors.exceptions.IncompatableParameterException;
import heigit.ors.exceptions.ParameterValueException;
import heigit.ors.exceptions.StatusCodeException;
import heigit.ors.isochrones.IsochroneMapCollection;
import heigit.ors.isochrones.IsochroneRequest;
import heigit.ors.isochrones.IsochronesErrorCodes;
import heigit.ors.routing.RouteSearchParameters;
import heigit.ors.routing.RoutingErrorCodes;
import heigit.ors.routing.RoutingProfileType;
import heigit.ors.util.DistanceUnitUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class IsochronesRequestHandler extends GenericHandler {

    public IsochronesRequestHandler() {
        super();
        this.errorCodes.put("UNKNOWN_PARAMETER", IsochronesErrorCodes.UNKNOWN_PARAMETER);
        this.errorCodes.put("INVALID_JSON_FORMAT", IsochronesErrorCodes.INVALID_JSON_FORMAT);
        this.errorCodes.put("INVALID_PARAMETER_VALUE", IsochronesErrorCodes.INVALID_PARAMETER_VALUE);
    }

    public IsochroneMapCollection generateIsochronesFromRequest(IsochronesRequest request) throws StatusCodeException {

        IsochroneRequest isochroneRequest = convertIsochroneRequest(request);

        try {

            IsochroneMapCollection isoMaps = new IsochroneMapCollection();

            /*for (int i = 0;i < travellers.size(); ++i){
                IsochroneSearchParameters searchParams = req.getSearchParameters(i);
                IsochroneMap isochroneMap = RoutingProfileManager.getInstance().buildIsochrone(searchParams, nonDefaultAttrs);
                isoMaps.add(isochroneMap);
            }*/

            return isoMaps;

        } catch (Exception e) {
            if (e instanceof StatusCodeException)
                throw (StatusCodeException) e;

            throw new StatusCodeException(IsochronesErrorCodes.UNKNOWN);
        }
    }

    public IsochroneRequest convertIsochroneRequest(IsochronesRequest request) throws StatusCodeException {

        IsochroneRequest isochroneRequest = new IsochroneRequest();

        TravellerInfo travellerInfo = new TravellerInfo();

        // profile
        int profileType = -1;

        try {
            profileType = convertRouteProfileType(request.getProfile());
            travellerInfo.getRouteSearchParameters().setProfileType(profileType);

        } catch (Exception e) {
            throw new ParameterValueException(IsochronesErrorCodes.INVALID_PARAMETER_VALUE, "profile");
        }

        //range_type
        travellerInfo.setRangeType(convertRangeType(request.getRangeType()));

        //location_type
        travellerInfo.setLocationType(convertLocationType(request.getLocationType()));

        //units
        isochroneRequest.setUnits(convertUnits(request.getUnits(), travellerInfo.getRangeType()).toString());

        //area_units
        isochroneRequest.setAreaUnits(convertAreaUnits(request.getAreaUnits()).toString());

        //range + interval
        List<Double> rangeValues = request.getRange();
        setRangeAndIntervals(travellerInfo, rangeValues);

        //attributes
        if (request.hasAttributes())
            isochroneRequest.setAttributes(convertAttributes(request.getAttributes()));

        //id
        if (request.hasId())
            isochroneRequest.setId(request.getId());

        //options
        RouteSearchParameters isochronesSearchParams = travellerInfo.getRouteSearchParameters();
        if (request.hasIsochronesOptions()) {

            IsochronesRequestOptions options = request.getIsochronesOptions();

            if (options.hasAvoidBorders())

                isochronesSearchParams.setAvoidBorders(convertAvoidBorders(options.getAvoidBorders()));

            if (options.hasAvoidPolygonFeatures())
                isochronesSearchParams.setAvoidAreas(convertAvoidAreas(options.getAvoidPolygonFeatures()));

            if (options.hasAvoidCountries())
                isochronesSearchParams.setAvoidCountries(options.getAvoidCountries());

            if (options.hasAvoidFeatures())
                isochronesSearchParams.setAvoidFeatureTypes(convertFeatureTypes(options.getAvoidFeatures(), profileType));

        }

        //intersections

        //locations (must come very last)
        Coordinate[] locations = convertLocations(request.getLocations());

        for (int i = 0; i < locations.length; i++) {

            if (i == 0) {

                travellerInfo.setLocation(locations[0]);
                isochroneRequest.addTraveller(travellerInfo);

            } else {

                TravellerInfo ti = travellerInfo.clone();
                ti.setLocation(locations[i]);
                isochroneRequest.addTraveller(ti);

            }

        }

        return isochroneRequest;
    }

    public void setRangeAndIntervals(TravellerInfo travellerInfo, List<Double> rangeValues) throws ParameterValueException{

        boolean skipInterval = false;
        double rangeValue = -1.0;

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

            skipInterval = true;
        }

        //interval
        if (!skipInterval) {
            value = request.getParameter("interval");
            if (!Helper.isEmpty(value)) {
                if (rangeValue != -1) {
                    travellerInfo.setRanges(rangeValue, Double.parseDouble(value));
                }
            }
        }

    }



    private String[] convertAttributes(IsochronesRequestEnums.Attributes[] attributes) {
        return convertAPIEnumListToStrings(attributes);
    }


    private static String convertLocationType(IsochronesRequestEnums.LocationType locationType) throws ParameterValueException {

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

    private static TravelRangeType convertRangeType(IsochronesRequestEnums.RangeType rangeType) throws ParameterValueException {

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


    private static DistanceUnit convertAreaUnits(APIEnums.Units unitsIn) throws
            ParameterValueException {

        DistanceUnit units = DistanceUnitUtil.getFromString(unitsIn.toString(), DistanceUnit.Unknown);

        if (units == DistanceUnit.Unknown)

            throw new ParameterValueException(RoutingErrorCodes.INVALID_PARAMETER_VALUE, "units", unitsIn.toString());


        return units;

    }

    private static DistanceUnit convertUnits(APIEnums.Units unitsIn, TravelRangeType rangeType) throws
            ParameterValueException, IncompatableParameterException {

        DistanceUnit units;

        if (rangeType.equals(TravelRangeType.Distance)) {

            units = DistanceUnitUtil.getFromString(unitsIn.toString(), DistanceUnit.Unknown);

            if (units == DistanceUnit.Unknown)

                throw new ParameterValueException(RoutingErrorCodes.INVALID_PARAMETER_VALUE, "units", unitsIn.toString());

        } else {

            throw new IncompatableParameterException(IsochronesErrorCodes.INVALID_PARAMETER_VALUE, "units", unitsIn.toString(),
                    "range_type", TravelRangeType.Distance.toString());

        }

        return units;

    }

    private static Coordinate[] convertLocations(List<List<Double>> locations) throws ParameterValueException {
        if (locations.size() < 1)
            throw new ParameterValueException(RoutingErrorCodes.INVALID_PARAMETER_VALUE, "locations");

        ArrayList<Coordinate> coords = new ArrayList<>();

        for (List<Double> coord : locations) {
            coords.add(convertSingleCoordinate(coord));
        }

        return coords.toArray(new Coordinate[coords.size()]);
    }


    private static Coordinate convertSingleCoordinate(List<Double> coordinate) throws ParameterValueException {
        if (coordinate.size() != 2)
            throw new ParameterValueException(RoutingErrorCodes.INVALID_PARAMETER_VALUE, "locations");

        return new Coordinate(coordinate.get(0), coordinate.get(1));
    }


}
