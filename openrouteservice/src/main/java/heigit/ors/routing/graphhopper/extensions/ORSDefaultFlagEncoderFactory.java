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

public class ORSDefaultFlagEncoderFactory extends DefaultFlagEncoderFactory implements FlagEncoderFactory {
    private static final String CAR = "car";
    private static final String CAROFFROAD = "caroffroad";
    private static final String BIKE = "bike";
    private static final String MTB = "mtb";

    private static final String ROADBIKE_NG = "roadbike-ng";
    private static final String RACINGBIKE_GH = "racingbike";
    private static final String RACINGBIKE = "racingbike-ors";

    private static final String SAFETYBIKE = "safetybike";
    private static final String ELECTROBIKE = "electrobike";
    private static final String CYCLETOURBIKE = "cycletourbike";
    private static final String HIKING = "hiking";
    private static final String WHEELCHAIR = "wheelchair";
    private static final String HEAVYVEHICLE = "heavyvehicle";
    private static final String CARTMC = "cartmc";
    private static final String EMERGENCY = "emergency";

    @Override
    public FlagEncoder createFlagEncoder(String name, PMap configuration) {
        if (name.equals(BIKE))
            return new heigit.ors.routing.graphhopper.extensions.flagencoders.BikeFlagEncoder(configuration);

        if (name.equals(MTB)) {
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
            return new heigit.ors.routing.graphhopper.extensions.flagencodersexghoverwrite.ORSMountainBikeFlagEncoder(configuration);
        }

        if (name.equals(RACINGBIKE))
            return new heigit.ors.routing.graphhopper.extensions.flagencoders.RacingBikeFlagEncoder(configuration);

        if (name.equals(RACINGBIKE_GH))
            return new com.graphhopper.routing.util.RacingBikeFlagEncoder(configuration);

        if (name.equals(ROADBIKE_NG))
            return new heigit.ors.routing.graphhopper.extensions.flagencodersnextgen.NextGenRoadBikeFlagEncoder(configuration);

        if (name.equals(SAFETYBIKE))
            return new heigit.ors.routing.graphhopper.extensions.flagencoders.SafetyBikeFlagEncoder(configuration);

        if (name.equals(ELECTROBIKE))
            return new heigit.ors.routing.graphhopper.extensions.flagencoders.ElectroBikeFlagEncoder(configuration);

        if (name.equals(CYCLETOURBIKE))
            return new heigit.ors.routing.graphhopper.extensions.flagencoders.CycleTourBikeFlagEncoder(configuration);

        if (name.equals(HIKING))
            return new heigit.ors.routing.graphhopper.extensions.flagencoders.HikingFlagEncoder(configuration);

        if (name.equals(WHEELCHAIR))
            return new heigit.ors.routing.graphhopper.extensions.flagencoders.WheelchairFlagEncoder(configuration);

        if (name.equals(HEAVYVEHICLE))
            return new heigit.ors.routing.graphhopper.extensions.flagencoders.HeavyVehicleFlagEncoder(configuration);

        if (name.equals(CAR))
            return new heigit.ors.routing.graphhopper.extensions.flagencoders.CarFlagEncoder(configuration);

        if (name.equals(CAROFFROAD))
            return new heigit.ors.routing.graphhopper.extensions.flagencoders.CarOffRoadFlagEncoder(configuration);

        if (name.equals(CARTMC))
            return new heigit.ors.routing.graphhopper.extensions.flagencoders.CarTmcFlagEncoder(configuration);

        if (name.equals(EMERGENCY))
            return new heigit.ors.routing.graphhopper.extensions.flagencoders.EmergencyFlagEncoder(configuration);

        return super.createFlagEncoder(name, configuration);
    }
}
