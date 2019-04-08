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
package heigit.ors.routing.graphhopper.extensions;

import com.graphhopper.routing.util.DefaultFlagEncoderFactory;
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.routing.util.FlagEncoderFactory;
import com.graphhopper.util.PMap;
import heigit.ors.routing.graphhopper.extensions.flagencoders.FlagEncoderNames;

public class ORSDefaultFlagEncoderFactory extends DefaultFlagEncoderFactory implements FlagEncoderFactory {

    @Override
    public FlagEncoder createFlagEncoder(String name, PMap configuration) {
        switch(name){
            default:
                // for all the types that DOES not match the ORS specific encoders we make
                // use of the GH defaults - PLEASE note, that even if the GH defaults are
                // used, the ors-fork of gh includes an adjustment in the
                // 'AbstractFlagEncoder' (which is the parent class of ALL vehicles - so
                // even if a gh default flagEncoder impl ist used - we have some additional
                // features inside!
                return super.createFlagEncoder(name, configuration);

            case FlagEncoderNames.CAR_ORS:
                return new heigit.ors.routing.graphhopper.extensions.flagencoders.CarFlagEncoder(configuration);

            case FlagEncoderNames.CAROFFROAD:
                return new heigit.ors.routing.graphhopper.extensions.flagencoders.currentlynotinuse.CarOffRoadFlagEncoder(configuration);

            case FlagEncoderNames.CARTMC:
                return new heigit.ors.routing.graphhopper.extensions.flagencoders.currentlynotinuse.CarTmcFlagEncoder(configuration);

            case FlagEncoderNames.EMERGENCY:
                return new heigit.ors.routing.graphhopper.extensions.flagencoders.EmergencyFlagEncoder(configuration);

            case FlagEncoderNames.HEAVYVEHICLE:
                return new heigit.ors.routing.graphhopper.extensions.flagencoders.HeavyVehicleFlagEncoder(configuration);

            case FlagEncoderNames.BIKE_ORS:
                if (configuration.getBool("consider_elevation", false)) {
                    configuration.remove("consider_elevation");
                }
                return new heigit.ors.routing.graphhopper.extensions.flagencoders.bike.RegularBikeFlagEncoder(configuration);

            case FlagEncoderNames.MTB_ORS:
                // MARQ24 hardcoded "ignore" consider_elevation for the NextGenMountainBike FlagEncoder - when
                // consider_elevation is enabled we have various detours (over smaler tracks)
                if(configuration.getBool("consider_elevation", false)){
                    configuration.remove("consider_elevation");
                }
                return new heigit.ors.routing.graphhopper.extensions.flagencoders.bike.MountainBikeFlagEncoder(configuration);

            case FlagEncoderNames.BIKE_ELECTRO:
                // MARQ24 hardcoded "ignore" consider_elevation for the NextGenMountainBike FlagEncoder - when
                // consider_elevation is enabled we have various detours (over smaler tracks)
                if(configuration.getBool("consider_elevation", false)){
                    configuration.remove("consider_elevation");
                }
                return new heigit.ors.routing.graphhopper.extensions.flagencoders.bike.ElectroBikeFlagEncoder(configuration);

            case FlagEncoderNames.ROADBIKE_ORS:
                // MARQ24 hardcoded "ignore" consider_elevation for the NextGenRoadbike FlagEncoder - when
                // consider_elevation is enabled we have various detours (over smaler tracks)
                // see http://localhost:3035/directions?n1=51.562385&n2=8.724582&n3=15&a=51.573202,8.709326,51.54879,8.710184&b=1c&c=0&g1=-1&g2=0&h2=3&k1=en-US&k2=km
                if(configuration.getBool("consider_elevation", false)){
                    configuration.remove("consider_elevation");
                }
                return new heigit.ors.routing.graphhopper.extensions.flagencoders.bike.RoadBikeFlagEncoder(configuration);

            case FlagEncoderNames.WHEELCHAIR:
                return new heigit.ors.routing.graphhopper.extensions.flagencoders.WheelchairFlagEncoder(configuration);
        }
    }
}
