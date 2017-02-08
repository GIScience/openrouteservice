package org.freeopenls.routeservice.graphhopper.extensions.util;

import java.util.HashMap;

public final class WheelchairRestrictionCodes {
	// needs 21 Bits in total > 4 bytes should be enough
	/**
	 * 26 distinct values => 5 Bits 
	 */
	public final static int SURFACE = 0;
	/**
	 * 8 distinct values => 3 Bits 
	 */
	public final static int SMOOTHNESS = 1;
	/**
	 * maximum value 0.31cm => 5 Bits
	 */
	public final static int SLOPED_CURB = 2;
	/**
	 * 5 distinct values => 3 Bits
	 */
	public final static int TRACKTYPE = 3;

	/**
	 * 	maximum value 31% => 5 Bits
	 */
	public final static int INCLINE = 4;
	
	// surface
	// =======
	// http://wiki.openstreetmap.org/wiki/DE:Key:surface
	// http://wiki.openstreetmap.org/wiki/Wheelchair_routing#Path_properties.2C_in_general
	// http://wiki.openstreetmap.org/wiki/DE:Wheelchair_routing#Weg_Eigenschaften_allgemein
	// 26 Werte (5 bits)
	public final static int SURFACE_PAVED = 0;
	public final static int SURFACE_ASPHALT = 1;
	public final static int SURFACE_CONCRETE = 2;
	public final static int SURFACE_PAVING_STONES = 3;
	public final static int SURFACE_CONCRETE_PLATES = 4;
	public final static int SURFACE_COBBLESTONE_FLATTENED = 5;
	public final static int SURFACE_CONCRETE_LANES = 6;
	public final static int SURFACE_COBBLESTONE = 7;
	
	// http://wiki.openstreetmap.org/wiki/Key:surface
	// these might still be "feasable" for experienced wheelchair users
	public final static int SURFACE_UNPAVED = 8;
	public final static int SURFACE_UNPAVED_FINE_GRAVEL = 9;
	public final static int SURFACE_UNPAVED_COMPACTED = 10;
	public final static int SURFACE_UNPAVED_METAL = 11;
	public final static int SURFACE_UNPAVED_ICE = 12;
	public final static int SURFACE_UNPAVED_GRASS_PAVER = 13;
	public final static int SURFACE_UNPAVED_SAND = 14;
	// these are probably not "feasable" for wheelchair users
	public final static int SURFACE_UNPAVED_DIRT = 15;
	public final static int SURFACE_UNPAVED_EARTH = 16;
	public final static int SURFACE_UNPAVED_GRASS = 17;
	public final static int SURFACE_UNPAVED_GRAVEL = 18;
	public final static int SURFACE_UNPAVED_GROUND = 19;
	public final static int SURFACE_UNPAVED_MUD = 20;
	public final static int SURFACE_UNPAVED_PEBBLESTONE = 21;
	public final static int SURFACE_UNPAVED_SALT = 22;
	public final static int SURFACE_UNPAVED_SNOW = 23;
	public final static int SURFACE_UNPAVED_WOOD = 24;
	public final static int SURFACE_UNPAVED_WOODCHIPS = 25;
	public final static int SURFACE_WORST = 26;
	
	public final static HashMap<String, Integer> SURFACE_MAP = new HashMap<String, Integer>(){{
        put("paved", SURFACE_PAVED);
        put("asphalt", SURFACE_ASPHALT);
        put("concrete", SURFACE_CONCRETE);
        put("paving_stones", SURFACE_PAVING_STONES);
        put("concrete:plates", SURFACE_CONCRETE_PLATES);
        put("cobblestone:flattened", SURFACE_COBBLESTONE_FLATTENED);
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
	public final static int SMOOTHNESS_EXCELLENT = 0;
	public final static int SMOOTHNESS_GOOD = 1;
	public final static int SMOOTHNESS_INTERMEDIATE = 2;
	public final static int SMOOTHNESS_BAD = 3;
	public final static int SMOOTHNESS_VERY_BAD = 4;
	public final static int SMOOTHNESS_HORRIBLE = 5;
	public final static int SMOOTHNESS_VERY_HORRIBLE = 6;
	public final static int SMOOTHNESS_IMPASSABLE = 7;
	
	public final static HashMap<String, Integer> SMOOTHNESS_MAP = new HashMap<String, Integer>(){{
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
	public final static int TRACKTYPE_GRADE1 = 0;
	public final static int TRACKTYPE_GRADE2 = 1;
	public final static int TRACKTYPE_GRADE3 = 2;
	public final static int TRACKTYPE_GRADE4 = 3;
	public final static int TRACKTYPE_GRADE5 = 4;
	
	public final static HashMap<String, Integer> TRACKTYPE_MAP = new HashMap<String, Integer>(){{
        put("grade1", TRACKTYPE_GRADE1);
        put("grade2", TRACKTYPE_GRADE2);
        put("grade3", TRACKTYPE_GRADE3);
        put("grade4", TRACKTYPE_GRADE4);
        put("grade5", TRACKTYPE_GRADE5);	
    }};	
    
    
    public final static int INCLINE_MAXIMUM = 15;
    public final static double SLOPED_CURB_MAXIMUM = 0.15d;
}

