/*
 *  Licensed to GIScience Research Group, Heidelberg University (GIScience)
 *
 *   http://www.giscience.uni-hd.de
 *   http://www.heigit.org
 *
 *  under one or more contributor license agreements. See the NOTICE file
 *  distributed with this work for additional information regarding copyright
 *  ownership. The GIScience licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except in compliance
 *  with the License. You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
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
                // for all the types that DOES not macht the ORS specific encoders we make
                // use of the GH defualts - PLEASE note, that even if the GH defaults are
                // used, the ors-fork of gh includes an adjustment in the
                // 'AbstractFlagEncoder' (which is the parent class of ALL vehicles - so
                // even if a gh default flagEncoder impl ist used - we hase some additional
                // features inside!
                return super.createFlagEncoder(name, configuration);

            case FlagEncoderNames.CAR_ORS:
                return new heigit.ors.routing.graphhopper.extensions.flagencoders.CarFlagEncoder(configuration);

            case FlagEncoderNames.CAROFFROAD:
                return new heigit.ors.routing.graphhopper.extensions.flagencoders.CarOffRoadFlagEncoder(configuration);

            case FlagEncoderNames.CARTMC:
                return new heigit.ors.routing.graphhopper.extensions.flagencoders.CarTmcFlagEncoder(configuration);

            case FlagEncoderNames.EMERGENCY:
                return new heigit.ors.routing.graphhopper.extensions.flagencoders.EmergencyFlagEncoder(configuration);

            case FlagEncoderNames.HEAVYVEHICLE:
                return new heigit.ors.routing.graphhopper.extensions.flagencoders.HeavyVehicleFlagEncoder(configuration);

            case FlagEncoderNames.BIKE_ORS:
                return new heigit.ors.routing.graphhopper.extensions.flagencoders.nextgen.NextGenBikeFlagEncoder(configuration);

            case FlagEncoderNames.MTB_ORS:
                return new heigit.ors.routing.graphhopper.extensions.flagencoders.nextgen.NextGenMountainBikeFlagEncoder(configuration);

            case FlagEncoderNames.ROADBIKE_ORS:
                return new heigit.ors.routing.graphhopper.extensions.flagencoders.nextgen.NextGenRoadBikeFlagEncoder(configuration);

            case FlagEncoderNames.WHEELCHAIR:
                return new heigit.ors.routing.graphhopper.extensions.flagencoders.WheelchairFlagEncoder(configuration);

            case FlagEncoderNames.HIKING:
                return new heigit.ors.routing.graphhopper.extensions.flagencoders.deprecated.HikingFlagEncoder(configuration);

            case FlagEncoderNames.BIKE_OLD:
                return new heigit.ors.routing.graphhopper.extensions.flagencoders.deprecated.BikeFlagEncoder(configuration);

            case FlagEncoderNames.MTB_OLD:
                // MARQ24: in previous ors this line of code was never used... MTB was set to "bike" - so when this method
                // was called with the param name="mtb" the original DefaultFlagEncoderFactory was called!!!
                // and the DefaultFlagEncoderFactory then returned the 'default' graphhopper MountainBikeFlagEncoder...
                // But this is not the complete story...
                // 'of course' the original gh MountainBikeFlagEncoder was patched in the ors fork of gh - so at the end
                // of the day the com.graphhopper.routing.util.flagencoder.MountainBikeFlagEncoder as it was present
                // in the ors fork would be returned...
                // I have decided (with the migration to gh 0.10.1) that the original FlagEncoders should not been modified
                // and instead the modified version have been paced in the package:
                // heigit.ors.routing.graphhopper.extensions.flagencodersexghoverwrite
                // and the FlagEncoderNames have got the Prefix ORS...
                //return new MountainBikeFlagEncoder(configuration);
                return new heigit.ors.routing.graphhopper.extensions.flagencoders.exghoverwrite.ExGhORSMountainBikeFlagEncoder(configuration);

            case FlagEncoderNames.RACINGBIKE_ORS:
                return new heigit.ors.routing.graphhopper.extensions.flagencoders.deprecated.RacingBikeFlagEncoder(configuration);

            case FlagEncoderNames.BIKE_SAFTY:
                return new heigit.ors.routing.graphhopper.extensions.flagencoders.deprecated.SafetyBikeFlagEncoder(configuration);

            case FlagEncoderNames.BIKE_ELECTRO:
                return new heigit.ors.routing.graphhopper.extensions.flagencoders.deprecated.ElectroBikeFlagEncoder(configuration);

            case FlagEncoderNames.BIKE_TOUR:
                return new heigit.ors.routing.graphhopper.extensions.flagencoders.deprecated.CycleTourBikeFlagEncoder(configuration);
        }
    }
}
