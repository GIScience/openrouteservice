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
import heigit.ors.routing.graphhopper.extensions.flagencoders.*;

public class ORSDefaultFlagEncoderFactory extends DefaultFlagEncoderFactory implements FlagEncoderFactory {
    private static final String CAR = "car";
    private static final String CAROFFROAD = "caroffroad";
    private static final String BIKE = "bike";
    // MARQ24 WARNING!!! - please note, that currently the MTB profile of the ORS implementation
    // is NOT USED!!! (heigit.ors.routing.graphhopper.extensions.flagencoders.MountainBikeFlagEncoder)
	//  the createFlagEncoder method will return the ORIGNAL GH MountainBikeFlagEncoder!!!
    // (which is still a "patched" version in the case of the graphopper fork version of ors!!!
    private static final String MTB = "bike";
    private static final String RACINGBIKE = "racingbike";
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
            return new BikeFlagEncoder(configuration);

        if (name.equals(MTB)) {
            // MARQ24 - THIS will be never reached!!!! - simply cause
            // MTB = BIKE = "bike" - see comment above
            return new MountainBikeFlagEncoder(configuration);
        }
        if (name.equals(RACINGBIKE))
            return new RacingBikeFlagEncoder(configuration);

        if (name.equals(SAFETYBIKE))
            return new SafetyBikeFlagEncoder(configuration);

        if (name.equals(ELECTROBIKE))
            return new ElectroBikeFlagEncoder(configuration);

        if (name.equals(CYCLETOURBIKE))
            return new CycleTourBikeFlagEncoder(configuration);

        if (name.equals(HIKING))
            return new HikingFlagEncoder(configuration);

        if (name.equals(WHEELCHAIR))
            return new WheelchairFlagEncoder(configuration);

        if (name.equals(HEAVYVEHICLE))
            return new HeavyVehicleFlagEncoder(configuration);

        if (name.equals(CAR))
            return new CarFlagEncoder(configuration);

        if (name.equals(CAROFFROAD))
            return new CarOffRoadFlagEncoder(configuration);

        if (name.equals(CARTMC))
            return new CarTmcFlagEncoder(configuration);

        if (name.equals(EMERGENCY))
            return new EmergencyFlagEncoder(configuration);

        return super.createFlagEncoder(name, configuration);
    }
}
