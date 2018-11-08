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

import heigit.ors.api.requests.common.APIEnums;
import heigit.ors.common.DistanceUnit;
import heigit.ors.common.TravelRangeType;
import heigit.ors.common.TravellerInfo;
import heigit.ors.exceptions.ParameterValueException;
import heigit.ors.exceptions.IncompatableParameterException;
import heigit.ors.exceptions.StatusCodeException;
import heigit.ors.isochrones.IsochroneMapCollection;
import heigit.ors.isochrones.IsochroneRequest;
import heigit.ors.isochrones.IsochronesErrorCodes;
import heigit.ors.routing.RoutingErrorCodes;
import heigit.ors.routing.RoutingProfileType;
import heigit.ors.util.DistanceUnitUtil;


public class IsochronesRequestHandler {

    public static IsochroneMapCollection generateIsochronesFromRequest(IsochronesRequest request) throws StatusCodeException {

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

    public static IsochroneRequest convertIsochroneRequest(IsochronesRequest request) throws StatusCodeException {

        IsochroneRequest isochroneRequest = new IsochroneRequest();

        TravellerInfo travellerInfo = new TravellerInfo();

        // profile
        travellerInfo.getRouteSearchParameters().setProfileType(convertRouteProfileType(request.getProfile()));

        //range_type
        travellerInfo.setRangeType(convertRangeType(request.getRangeType()));

        //units
        isochroneRequest.setUnits(convertUnits(request.getUnits(), travellerInfo.getRangeType()).toString());

        //area_units
        //isochroneRequest.setAreaUnits(convertUnits(request.getAreaUnits());


        //attributes

        //options

        //location_Type

        //range

        //interval

        //intersections

        //locations


        return isochroneRequest;
    }

    private static int convertRouteProfileType(APIEnums.RoutingProfile profile) throws ParameterValueException {

        int profileType;

        try {

            profileType = RoutingProfileType.getFromString(profile.toString());

        } catch (Exception e) {

            throw new ParameterValueException(IsochronesErrorCodes.INVALID_PARAMETER_VALUE, "profile", profile.toString());

        }

        return profileType;

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


}
