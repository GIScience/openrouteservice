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
package org.heigit.ors.routing.graphhopper.extensions;

import org.heigit.ors.api.requests.common.APIEnums;

import java.util.HashMap;
import java.util.Map;

public final class WheelchairTypesEncoder {
	
	// surface
	// =======
	// http://wiki.openstreetmap.org/wiki/DE:Key:surface
	// http://wiki.openstreetmap.org/wiki/Wheelchair_routing#Path_properties.2C_in_general
	// http://wiki.openstreetmap.org/wiki/DE:Wheelchair_routing#Weg_Eigenschaften_allgemein
	// 26 Werte (5 bits)
	private static final int SURFACE_PAVED = 1;
	private static final int SURFACE_ASPHALT = 2;
	private static final int SURFACE_CONCRETE = 3;
	private static final int SURFACE_PAVING_STONES = 4;
	private static final int SURFACE_CONCRETE_PLATES = 5;
	private static final int SURFACE_COBBLESTONE_FLATTENED = 6;
	private static final int SURFACE_CONCRETE_LANES = 7;
	private static final int SURFACE_COBBLESTONE = 8;

	// http://wiki.openstreetmap.org/wiki/Key:surface
	// these might still be "feasable" for experienced wheelchair users
	private static final int SURFACE_UNPAVED = 9;
	private static final int SURFACE_UNPAVED_FINE_GRAVEL = 10;
	private static final int SURFACE_UNPAVED_COMPACTED = 11;
	private static final int SURFACE_UNPAVED_METAL = 12;
	private static final int SURFACE_UNPAVED_ICE = 13;
	private static final int SURFACE_UNPAVED_GRASS_PAVER = 14;
	private static final int SURFACE_UNPAVED_SAND = 15;
	// these are probably not "feasible" for wheelchair users
	private static final int SURFACE_UNPAVED_DIRT = 16;
	private static final int SURFACE_UNPAVED_EARTH = 17;
	private static final int SURFACE_UNPAVED_GRASS = 18;
	private static final int SURFACE_UNPAVED_GRAVEL = 19;
	private static final int SURFACE_UNPAVED_GROUND = 20;
	private static final int SURFACE_UNPAVED_MUD = 21;
	private static final int SURFACE_UNPAVED_PEBBLESTONE = 22;
	private static final int SURFACE_UNPAVED_SALT = 23;
	private static final int SURFACE_UNPAVED_SNOW = 24;
	private static final int SURFACE_UNPAVED_WOOD = 25;
	private static final int SURFACE_UNPAVED_WOODCHIPS = 26;


	private static final Map<String, Integer> SURFACE_MAP = new HashMap<>();
    static {
        SURFACE_MAP.put("paved", SURFACE_PAVED);
        SURFACE_MAP.put("asphalt", SURFACE_ASPHALT);
        SURFACE_MAP.put("concrete", SURFACE_CONCRETE);
        SURFACE_MAP.put("paving_stones", SURFACE_PAVING_STONES);
        SURFACE_MAP.put("concrete:plates", SURFACE_CONCRETE_PLATES);
        SURFACE_MAP.put("cobblestone:flattened", SURFACE_COBBLESTONE_FLATTENED);
        SURFACE_MAP.put("sett", SURFACE_COBBLESTONE_FLATTENED);
        SURFACE_MAP.put("unhewn_cobblestone", SURFACE_COBBLESTONE_FLATTENED);
        SURFACE_MAP.put("concrete:lanes", SURFACE_CONCRETE_LANES);
        SURFACE_MAP.put("cobblestone", SURFACE_COBBLESTONE);
        SURFACE_MAP.put("unpaved", SURFACE_UNPAVED);
        SURFACE_MAP.put("fine_gravel", SURFACE_UNPAVED_FINE_GRAVEL);
        SURFACE_MAP.put("compacted", SURFACE_UNPAVED_COMPACTED);
        SURFACE_MAP.put("metal", SURFACE_UNPAVED_METAL);
        SURFACE_MAP.put("ice", SURFACE_UNPAVED_ICE);
        SURFACE_MAP.put("grass_paver", SURFACE_UNPAVED_GRASS_PAVER);
        SURFACE_MAP.put("sand", SURFACE_UNPAVED_SAND);
        SURFACE_MAP.put("dirt", SURFACE_UNPAVED_DIRT);
        SURFACE_MAP.put("earth", SURFACE_UNPAVED_EARTH);
        SURFACE_MAP.put("grass", SURFACE_UNPAVED_GRASS);
        SURFACE_MAP.put("gravel", SURFACE_UNPAVED_GRAVEL);
        SURFACE_MAP.put("ground", SURFACE_UNPAVED_GROUND);
        SURFACE_MAP.put("mud", SURFACE_UNPAVED_MUD);
        SURFACE_MAP.put("pebblestone", SURFACE_UNPAVED_PEBBLESTONE);
        SURFACE_MAP.put("salt", SURFACE_UNPAVED_SALT);
        SURFACE_MAP.put("snow", SURFACE_UNPAVED_SNOW);
        SURFACE_MAP.put("wood", SURFACE_UNPAVED_WOOD);
        SURFACE_MAP.put("woodchips", SURFACE_UNPAVED_WOODCHIPS);
    }
	
    // smoothness
    // ==========
	// http://wiki.openstreetmap.org/wiki/DE:Key:smoothness
	// http://wiki.openstreetmap.org/wiki/Wheelchair_routing#Path_properties.2C_in_general
	// http://wiki.openstreetmap.org/wiki/DE:Wheelchair_routing#Weg_Eigenschaften_allgemein1
	// 8 Werte (4 Bits)
    private static final int SMOOTHNESS_EXCELLENT = 1;
    private static final int SMOOTHNESS_GOOD = 2;
    private static final int SMOOTHNESS_INTERMEDIATE = 3;
    private static final int SMOOTHNESS_BAD = 4;
    private static final int SMOOTHNESS_VERY_BAD = 5;
    private static final int SMOOTHNESS_HORRIBLE = 6;
    private static final int SMOOTHNESS_VERY_HORRIBLE = 7;
    private static final int SMOOTHNESS_IMPASSABLE = 8;
	
    @SuppressWarnings("serial")
	private static final HashMap<String, Integer> SMOOTHNESS_MAP = new HashMap<>();
    static {
        SMOOTHNESS_MAP.put("excellent", SMOOTHNESS_EXCELLENT);
        SMOOTHNESS_MAP.put("good", SMOOTHNESS_GOOD);
        SMOOTHNESS_MAP.put("intermediate", SMOOTHNESS_INTERMEDIATE);
        SMOOTHNESS_MAP.put("bad", SMOOTHNESS_BAD);
        SMOOTHNESS_MAP.put("very_bad", SMOOTHNESS_VERY_BAD);
        SMOOTHNESS_MAP.put("horrible", SMOOTHNESS_HORRIBLE);
        SMOOTHNESS_MAP.put("very_horrible", SMOOTHNESS_VERY_HORRIBLE);
        SMOOTHNESS_MAP.put("impassable", SMOOTHNESS_IMPASSABLE);
    }

    
    // tracktype
	// =========
	// http://wiki.openstreetmap.org/wiki/Key:tracktype
	// http://wiki.openstreetmap.org/wiki/Wheelchair_routing#Path_properties.2C_in_general
	// http://wiki.openstreetmap.org/wiki/DE:Wheelchair_routing#Weg_Eigenschaften_allgemein
	// 5 Werte (3 Bits)
    private static final int TRACKTYPE_GRADE1 = 1;
    private static final int TRACKTYPE_GRADE2 = 2;
    private static final int TRACKTYPE_GRADE3 = 3;
    private static final int TRACKTYPE_GRADE4 = 4;
    private static final int TRACKTYPE_GRADE5 = 5;
	
	@SuppressWarnings("serial")
	private static final HashMap<String, Integer> TRACKTYPE_MAP = new HashMap<>();
	static {
        TRACKTYPE_MAP.put("grade1", TRACKTYPE_GRADE1);
        TRACKTYPE_MAP.put("grade2", TRACKTYPE_GRADE2);
        TRACKTYPE_MAP.put("grade3", TRACKTYPE_GRADE3);
        TRACKTYPE_MAP.put("grade4", TRACKTYPE_GRADE4);
        TRACKTYPE_MAP.put("grade5", TRACKTYPE_GRADE5);
    }

    private WheelchairTypesEncoder() {}
    
    public static int getSurfaceType(String value) {
    	if ("any".equalsIgnoreCase(value))
    		return 0;
    	else
    		return SURFACE_MAP.getOrDefault(value, -1);
    }

    public static int getEncodedType(WheelchairAttributes.Attribute attribute, String value) throws Exception {
        switch(attribute) {
            case SMOOTHNESS: return getSmoothnessType(APIEnums.SmoothnessTypes.forValue(value));
            case SURFACE: return getSurfaceType(value);
            case TRACK: return getTrackType(value);
            default: throw new Exception("Attribute is not a recognised encoded type");
        }
    }
    
    public static int getSmoothnessType(APIEnums.SmoothnessTypes smoothnessType) {
    	return SMOOTHNESS_MAP.getOrDefault(smoothnessType.toString(), -1);
    }
    
    public static int getTrackType(String value) {
    	return TRACKTYPE_MAP.getOrDefault(value, -1);
    }
}

