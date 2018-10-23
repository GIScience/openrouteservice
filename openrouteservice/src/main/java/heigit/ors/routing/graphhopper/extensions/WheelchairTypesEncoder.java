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

import java.util.HashMap;

public final class WheelchairTypesEncoder {
	
	// surface
	// =======
	// http://wiki.openstreetmap.org/wiki/DE:Key:surface
	// http://wiki.openstreetmap.org/wiki/Wheelchair_routing#Path_properties.2C_in_general
	// http://wiki.openstreetmap.org/wiki/DE:Wheelchair_routing#Weg_Eigenschaften_allgemein
	// 26 Werte (5 bits)
	private final static int SURFACE_PAVED = 1;
	private final static int SURFACE_ASPHALT = 2;
	private final static int SURFACE_CONCRETE = 3;
	private final static int SURFACE_PAVING_STONES = 4;
	private final static int SURFACE_CONCRETE_PLATES = 5;
	private final static int SURFACE_COBBLESTONE_FLATTENED = 6;
	private final static int SURFACE_CONCRETE_LANES = 7;
	private final static int SURFACE_COBBLESTONE = 8;
	
	// http://wiki.openstreetmap.org/wiki/Key:surface
	// these might still be "feasable" for experienced wheelchair users
	private final static int SURFACE_UNPAVED = 9;
	private final static int SURFACE_UNPAVED_FINE_GRAVEL = 10;
	private final static int SURFACE_UNPAVED_COMPACTED = 11;
	private final static int SURFACE_UNPAVED_METAL = 12;
	private final static int SURFACE_UNPAVED_ICE = 13;
	private final static int SURFACE_UNPAVED_GRASS_PAVER = 14;
	private final static int SURFACE_UNPAVED_SAND = 15;
	// these are probably not "feasable" for wheelchair users
	private final static int SURFACE_UNPAVED_DIRT = 16;
	private final static int SURFACE_UNPAVED_EARTH = 17;
	private final static int SURFACE_UNPAVED_GRASS = 18;
	private final static int SURFACE_UNPAVED_GRAVEL = 19;
	private final static int SURFACE_UNPAVED_GROUND = 20;
	private final static int SURFACE_UNPAVED_MUD = 21;
	private final static int SURFACE_UNPAVED_PEBBLESTONE = 22;
	private final static int SURFACE_UNPAVED_SALT = 23;
	private final static int SURFACE_UNPAVED_SNOW = 24;
	private final static int SURFACE_UNPAVED_WOOD = 25;
	private final static int SURFACE_UNPAVED_WOODCHIPS = 26;
	private final static int SURFACE_WORST = 27;
	
	@SuppressWarnings("serial")
	private final static HashMap<String, Integer> SURFACE_MAP = new HashMap<String, Integer>(){{
        put("paved", SURFACE_PAVED);
        put("asphalt", SURFACE_ASPHALT);
        put("concrete", SURFACE_CONCRETE);
        put("paving_stones", SURFACE_PAVING_STONES);
        put("concrete:plates", SURFACE_CONCRETE_PLATES);
        put("cobblestone:flattened", SURFACE_COBBLESTONE_FLATTENED);
        put("sett", SURFACE_COBBLESTONE_FLATTENED);
        put("unhewn_cobblestone", SURFACE_COBBLESTONE_FLATTENED);
        put("concrete:lanes", SURFACE_CONCRETE_LANES);
        put("cobblestone", SURFACE_COBBLESTONE);
        put("unpaved", SURFACE_UNPAVED);
        put("fine_gravel", SURFACE_UNPAVED_FINE_GRAVEL);
        put("compacted", SURFACE_UNPAVED_COMPACTED);
        put("metal", SURFACE_UNPAVED_METAL);
        put("ice", SURFACE_UNPAVED_ICE);
        put("grass_paver", SURFACE_UNPAVED_GRASS_PAVER);
        put("sand", SURFACE_UNPAVED_SAND);
        put("dirt", SURFACE_UNPAVED_DIRT);
        put("earth", SURFACE_UNPAVED_EARTH);
        put("grass", SURFACE_UNPAVED_GRASS);
        put("gravel", SURFACE_UNPAVED_GRAVEL);
        put("ground", SURFACE_UNPAVED_GROUND);
        put("mud", SURFACE_UNPAVED_MUD);
        put("pebblestone", SURFACE_UNPAVED_PEBBLESTONE);
        put("salt", SURFACE_UNPAVED_SALT);
        put("snow", SURFACE_UNPAVED_SNOW);
        put("wood", SURFACE_UNPAVED_WOOD);
        put("woodchips", SURFACE_UNPAVED_WOODCHIPS);
    }};

	
    // smoothness
    // ==========
	// http://wiki.openstreetmap.org/wiki/DE:Key:smoothness
	// http://wiki.openstreetmap.org/wiki/Wheelchair_routing#Path_properties.2C_in_general
	// http://wiki.openstreetmap.org/wiki/DE:Wheelchair_routing#Weg_Eigenschaften_allgemein1
	// 8 Werte (4 Bits)
    private final static int SMOOTHNESS_EXCELLENT = 1;
    private final static int SMOOTHNESS_GOOD = 2;
    private final static int SMOOTHNESS_INTERMEDIATE = 3;
    private final static int SMOOTHNESS_BAD = 4;
    private final static int SMOOTHNESS_VERY_BAD = 5;
    private final static int SMOOTHNESS_HORRIBLE = 6;
    private final static int SMOOTHNESS_VERY_HORRIBLE = 7;
    private final static int SMOOTHNESS_IMPASSABLE = 8;
	
    @SuppressWarnings("serial")
	private final static HashMap<String, Integer> SMOOTHNESS_MAP = new HashMap<String, Integer>(){{
        put("excellent", SMOOTHNESS_EXCELLENT);
        put("good", SMOOTHNESS_GOOD);
        put("intermediate", SMOOTHNESS_INTERMEDIATE);
        put("bad", SMOOTHNESS_BAD);
        put("very_bad", SMOOTHNESS_VERY_BAD);
        put("horrible", SMOOTHNESS_HORRIBLE);
        put("very_horrible", SMOOTHNESS_VERY_HORRIBLE);
        put("impassable", SMOOTHNESS_IMPASSABLE);
    }};
	
    
    // tracktype
	// =========
	// http://wiki.openstreetmap.org/wiki/Key:tracktype
	// http://wiki.openstreetmap.org/wiki/Wheelchair_routing#Path_properties.2C_in_general
	// http://wiki.openstreetmap.org/wiki/DE:Wheelchair_routing#Weg_Eigenschaften_allgemein
	// 5 Werte (3 Bits)
    private final static int TRACKTYPE_GRADE1 = 1;
    private final static int TRACKTYPE_GRADE2 = 2;
    private final static int TRACKTYPE_GRADE3 = 3;
    private final static int TRACKTYPE_GRADE4 = 4;
    private final static int TRACKTYPE_GRADE5 = 5;
	
	@SuppressWarnings("serial")
	private final static HashMap<String, Integer> TRACKTYPE_MAP = new HashMap<String, Integer>(){{
        put("grade1", TRACKTYPE_GRADE1);
        put("grade2", TRACKTYPE_GRADE2);
        put("grade3", TRACKTYPE_GRADE3);
        put("grade4", TRACKTYPE_GRADE4);
        put("grade5", TRACKTYPE_GRADE5);	
    }};	
    
    public static int getSurfaceType(String value)
    {
    	if ("any".equalsIgnoreCase(value))
    		return 0;
    	else
    		return SURFACE_MAP.getOrDefault(value, -1);
    }

    public static int getEncodedType(WheelchairAttributes.Attribute attribute, String value) throws Exception {
        switch(attribute) {
            case SMOOTHNESS: return getSmoothnessType(value);
            case SURFACE: return getSurfaceType(value);
            case TRACK: return getTrackType(value);

            default: throw new Exception("Attribute is not a recognised encoded type");
        }
    }
    
    public static int getSmoothnessType(String value)
    {
    	return SMOOTHNESS_MAP.getOrDefault(value, -1);
    }
    
    public static int getTrackType(String value)
    {
    	return TRACKTYPE_MAP.getOrDefault(value, -1);
    }
}

