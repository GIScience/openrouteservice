/*
 *  Licensed to GraphHopper GmbH under one or more contributor
 *  license agreements. See the NOTICE file distributed with this work for
 *  additional information regarding copyright ownership.
 *
 *  GraphHopper GmbH licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except in
 *  compliance with the License. You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package heigit.ors.routing.graphhopper.extensions.flagencoders.bike;

import com.graphhopper.reader.ReaderWay;
import com.graphhopper.routing.util.PriorityCode;
import com.graphhopper.util.PMap;
import heigit.ors.routing.graphhopper.extensions.flagencoders.FlagEncoderNames;

import java.util.TreeMap;

import static com.graphhopper.routing.util.PriorityCode.*;

/**
 * Specifies the settings for race biking
 * <p>
 *
 * @author ratrun
 * @author Peter Karich
 */
public class RoadBikeFlagEncoder extends CommonBikeFlagEncoder {
    public RoadBikeFlagEncoder() {
        // MARQ24 MOD START
        //this(4, 2, 0);
        this(6, 2, 0, false);
        // MARQ24 MOD END
    }

    public RoadBikeFlagEncoder(PMap properties) {
        this(
            // MARQ24 MOD START
            //(int) properties.getLong("speed_bits", 4),
            properties.getInt("speed_bits", 4 + (properties.getBool("consider_elevation", false) ? 1 : 0)),
            // MARQ24 MOD END
            properties.getDouble("speed_factor", 2),
            properties.getBool("turn_costs", false) ? 1 : 0
            // MARQ24 MOD START
            ,properties.getBool("consider_elevation", false)
            // MARQ24 MOD END
        );
        this.properties = properties;
        setBlockFords(properties.getBool("block_fords", true));
    }

    public RoadBikeFlagEncoder(String propertiesStr) {
        this(new PMap(propertiesStr));
    }

    // MARQ24 MOD START
    //public NextGenRoadBikeFlagEncoder(int speedBits, double speedFactor, int maxTurnCosts) {
    public RoadBikeFlagEncoder(int speedBits, double speedFactor, int maxTurnCosts, boolean considerElevation) {
        //super(speedBits, speedFactor, maxTurnCosts);
        super(speedBits, speedFactor, maxTurnCosts, considerElevation);
    // MARQ24 MOD END
        preferHighwayTags.add("road");
        preferHighwayTags.add("secondary");
        preferHighwayTags.add("secondary_link");
        preferHighwayTags.add("tertiary");
        preferHighwayTags.add("tertiary_link");
        preferHighwayTags.add("residential");

        setTrackTypeSpeed("grade1", 20); // paved
        setTrackTypeSpeed("grade2", 10); // now unpaved ...
        setTrackTypeSpeed("grade3", PUSHING_SECTION_SPEED);
        setTrackTypeSpeed("grade4", PUSHING_SECTION_SPEED);
        setTrackTypeSpeed("grade5", PUSHING_SECTION_SPEED);

        setSurfaceSpeed("paved", 20);
        setSurfaceSpeed("asphalt", 20);
        setSurfaceSpeed("cobblestone", 10);
        setSurfaceSpeed("cobblestone:flattened", 10);
        setSurfaceSpeed("sett", 10);
        setSurfaceSpeed("concrete", 20);
        setSurfaceSpeed("concrete:lanes", 16);
        setSurfaceSpeed("concrete:plates", 16);
        setSurfaceSpeed("paving_stones", 10);
        setSurfaceSpeed("paving_stones:30", 10);
        setSurfaceSpeed("unpaved", PUSHING_SECTION_SPEED / 2);
        setSurfaceSpeed("compacted", PUSHING_SECTION_SPEED / 2);
        setSurfaceSpeed("dirt", PUSHING_SECTION_SPEED / 2);
        setSurfaceSpeed("earth", PUSHING_SECTION_SPEED / 2);
        setSurfaceSpeed("fine_gravel", PUSHING_SECTION_SPEED);
        setSurfaceSpeed("grass", PUSHING_SECTION_SPEED / 2);
        setSurfaceSpeed("grass_paver", PUSHING_SECTION_SPEED / 2);
        setSurfaceSpeed("gravel", PUSHING_SECTION_SPEED / 2);
        setSurfaceSpeed("ground", PUSHING_SECTION_SPEED / 2);
        setSurfaceSpeed("ice", PUSHING_SECTION_SPEED / 2);
        setSurfaceSpeed("metal", PUSHING_SECTION_SPEED / 2);
        setSurfaceSpeed("mud", PUSHING_SECTION_SPEED / 2);
        setSurfaceSpeed("pebblestone", PUSHING_SECTION_SPEED);
        setSurfaceSpeed("salt", PUSHING_SECTION_SPEED / 2);
        setSurfaceSpeed("sand", PUSHING_SECTION_SPEED / 2);
        setSurfaceSpeed("wood", PUSHING_SECTION_SPEED / 2);

        setHighwaySpeed("cycleway", 18);
        setHighwaySpeed("path", 8);
        setHighwaySpeed("footway", 6);
        setHighwaySpeed("pedestrian", 6);
        setHighwaySpeed("road", 12);
        setHighwaySpeed("track", PUSHING_SECTION_SPEED / 2); // assume unpaved
        setHighwaySpeed("service", 12);
        setHighwaySpeed("unclassified", 16);
        setHighwaySpeed("residential", 16);

        setHighwaySpeed("trunk", 20);
        setHighwaySpeed("trunk_link", 20);
        setHighwaySpeed("primary", 20);
        setHighwaySpeed("primary_link", 20);
        setHighwaySpeed("secondary", 20);
        setHighwaySpeed("secondary_link", 20);
        setHighwaySpeed("tertiary", 20);
        setHighwaySpeed("tertiary_link", 20);

        addPushingSection("path");
        addPushingSection("footway");
        addPushingSection("pedestrian");
        addPushingSection("steps");

        setCyclingNetworkPreference("icn", PriorityCode.BEST.getValue());
        setCyclingNetworkPreference("ncn", PriorityCode.BEST.getValue());
        setCyclingNetworkPreference("rcn", PriorityCode.VERY_NICE.getValue());
        setCyclingNetworkPreference("lcn", PriorityCode.UNCHANGED.getValue());
        setCyclingNetworkPreference("mtb", PriorityCode.UNCHANGED.getValue());

        absoluteBarriers.add("kissing_gate");

        setAvoidSpeedLimit(81);
        setSpecificClassBicycle("roadcycling");

        // MARQ24 MOD START
        //**********************************************************************
        // REQUIRED ADDON OR OVERWRITE OF Default GH-RoadBikeProfile
        // created by MARQ24
        //**********************************************************************
        preferHighwayTags.remove("residential");
        preferHighwayTags.add("unclassified");

        // adjusted speeds...
        //this.maxPossibleSpeed = Math.max(this.maxPossibleSpeed, 30);
        setHighwaySpeed("trunk",           20);
        setHighwaySpeed("trunk_link",      20);
        setHighwaySpeed("primary",         22);
        setHighwaySpeed("primary_link",    22);
        setHighwaySpeed("secondary",       24);
        setHighwaySpeed("secondary_link",  24);
        setHighwaySpeed("tertiary",        26);
        setHighwaySpeed("tertiary_link",   26);
        setHighwaySpeed("road",            20);
        setHighwaySpeed("unclassified",    20);
        setHighwaySpeed("residential",      new SpeedValue(18, UpdateType.DOWNGRADE_ONLY));

        // make sure that we will avoid 'cycleway' & 'service' ways where ever
        // it is possible...
        setHighwaySpeed("cycleway",                new SpeedValue(8, UpdateType.DOWNGRADE_ONLY));
        setHighwaySpeed("service",                 new SpeedValue(8, UpdateType.DOWNGRADE_ONLY));

        // overwriting also the SurfaceSpeeds... to the "max" of the residential speed
        setSurfaceSpeed("paved",                    new SpeedValue(18, UpdateType.UPGRADE_ONLY));
        setSurfaceSpeed("asphalt",                  new SpeedValue(18, UpdateType.UPGRADE_ONLY));
        setSurfaceSpeed("concrete",                 new SpeedValue(18, UpdateType.UPGRADE_ONLY));

        setSurfaceSpeed("concrete:lanes",           new SpeedValue(16, UpdateType.UPGRADE_ONLY));
        setSurfaceSpeed("concrete:plates",          new SpeedValue(16, UpdateType.UPGRADE_ONLY));
        setSurfaceSpeed("paving_stones",            new SpeedValue(10, UpdateType.UPGRADE_ONLY));
        setSurfaceSpeed("paving_stones:30",         new SpeedValue(10, UpdateType.UPGRADE_ONLY));
        setSurfaceSpeed("cobblestone",              new SpeedValue(10, UpdateType.UPGRADE_ONLY));
        setSurfaceSpeed("cobblestone:flattened",    new SpeedValue(10, UpdateType.UPGRADE_ONLY));
        setSurfaceSpeed("sett",                     new SpeedValue(10, UpdateType.UPGRADE_ONLY));

        // overwriting also the trackTypeSpeeds... to the "max" of the residential speed
        setTrackTypeSpeed("grade1",                new SpeedValue(18, UpdateType.UPGRADE_ONLY));
        setTrackTypeSpeed("grade2",                new SpeedValue(10, UpdateType.UPGRADE_ONLY));

        // HSW - asphalt cycleway vs asphalt roundabout
        // http://localhost:3035/directions?n1=51.965101&n2=8.24595&n3=18&a=51.965555,8.243968,51.964878,8.245057&b=1c&c=0&g1=-1&g2=0&h2=3&k1=en-US&k2=km

        // Aschloh roundabout vs cycleway (cycle relation) & service shortcut
        // http://localhost:3035/directions?n1=52.064701&n2=8.386386&n3=19&a=52.065407,8.386171,52.064821,8.386833&b=1c&c=0&g1=-1&g2=0&h2=3&k1=en-US&k2=km
        LOGGER.info("NextGen RoadBike FlagEncoder is active...");
        // MARQ24 MOD END
        this.init();
    }

    @Override
    public int getVersion() {
        return 2;
    }

    @Override
    void collect(ReaderWay way, double wayTypeSpeed, TreeMap<Double, Integer> weightToPrioMap) {
        super.collect(way, wayTypeSpeed, weightToPrioMap);

        String highway = way.getTag("highway");
        if ("service".equals(highway)) {
            weightToPrioMap.put(40d, UNCHANGED.getValue());
        } else if ("track".equals(highway)) {
            String trackType = way.getTag("tracktype");
            if ("grade1".equals(trackType)) {
                weightToPrioMap.put(110d, PREFER.getValue());
            } else if (trackType == null || trackType.startsWith("grade")) {
                weightToPrioMap.put(110d, AVOID_AT_ALL_COSTS.getValue());
            }
        }
    }

    @Override
    boolean isPushingSection(ReaderWay way) {
        String highway = way.getTag("highway");
        String trackType = way.getTag("tracktype");
        return way.hasTag("highway", pushingSectionsHighways)
                || way.hasTag("railway", "platform")
                || way.hasTag("bicycle", "dismount")
                || "track".equals(highway) && trackType != null && !"grade1".equals(trackType);
    }

    @Override
    boolean isSacScaleAllowed(String sacScale) {
        // for racing bike it is only allowed if empty
        return false;
    }

    @Override
    public String toString() {
        // MARQ24 MOD START
        //return "racebike";
        return FlagEncoderNames.ROADBIKE_ORS;
        // MARQ24 MOD END
    }

    // MARQ24 MOD START
    @Override
    protected double getDownhillMaxSpeed() {
        return 60;
    }
    // MARQ24 MOD END
}
