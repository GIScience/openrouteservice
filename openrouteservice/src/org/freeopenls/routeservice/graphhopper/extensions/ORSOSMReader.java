/*|----------------------------------------------------------------------------------------------
 *|														Heidelberg University
 *|	  _____ _____  _____      _                     	Department of Geography		
 *|	 / ____|_   _|/ ____|    (_)                    	Chair of GIScience
 *|	| |  __  | | | (___   ___ _  ___ _ __   ___ ___ 	(C) 2014
 *|	| | |_ | | |  \___ \ / __| |/ _ \ '_ \ / __/ _ \	
 *|	| |__| |_| |_ ____) | (__| |  __/ | | | (_|  __/	Berliner Strasse 48								
 *|	 \_____|_____|_____/ \___|_|\___|_| |_|\___\___|	D-69120 Heidelberg, Germany	
 *|	        	                                       	http://www.giscience.uni-hd.de
 *|								
 *|----------------------------------------------------------------------------------------------*/

// Authors: M. Rylov

package org.freeopenls.routeservice.graphhopper.extensions;

import gnu.trove.list.TLongList;
import gnu.trove.list.array.TLongArrayList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.freeopenls.routeservice.graphhopper.extensions.storages.BikeAttributesGraphStorage;
import org.freeopenls.routeservice.graphhopper.extensions.storages.HeavyVehicleAttributesGraphStorage;
import org.freeopenls.routeservice.graphhopper.extensions.storages.MotorcarAttributesGraphStorage;
import org.freeopenls.routeservice.graphhopper.extensions.storages.WaySurfaceTypeStorage;
import org.freeopenls.routeservice.graphhopper.extensions.storages.WheelchairAttributesGraphStorage;
import org.freeopenls.routeservice.graphhopper.extensions.util.ConvertUtils;
import org.freeopenls.routeservice.graphhopper.extensions.util.VehicleRestrictionCodes;
import org.freeopenls.routeservice.graphhopper.extensions.util.WheelchairRestrictionCodes;
import org.freeopenls.routeservice.routing.AvoidFeatureFlags;
import org.freeopenls.routeservice.routing.RouteProfile;
import org.freeopenls.routeservice.routing.RouteProfileManager;

import com.graphhopper.reader.OSMNode;
import com.graphhopper.reader.OSMReader;
import com.graphhopper.reader.OSMWay;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.routing.util.WaySurfaceDescription;
import com.graphhopper.storage.GraphExtension;
import com.graphhopper.storage.GraphExtension.ExtendedStorageSequence;
import com.graphhopper.storage.GraphHopperStorage;
import com.graphhopper.util.EdgeIteratorState;
import com.graphhopper.util.Helper;
import com.graphhopper.util.PointList;
import com.vividsolutions.jts.geom.Envelope;

public class ORSOSMReader extends OSMReader {
	
	private MotorcarAttributesGraphStorage gsMotorcarAttrs; // 0
	private HeavyVehicleAttributesGraphStorage gsHeavyVehicleAttrs; // 1
	private BikeAttributesGraphStorage gsBikeAttrs;  // 2
	private WheelchairAttributesGraphStorage gsWheelchair; // 3
	
	private WaySurfaceTypeStorage gsWaySurfaceType;
	
	private int[] wayFlags = new int[4];
	private int heavyVehicleType = 0;
	private int heavyVehicleDestination = 0;
	private WaySurfaceDescription waySurfaceDesc = new WaySurfaceDescription();
	private boolean hasRestrictionValues;
	private boolean hasPassabilityValues;
	private boolean hasWheelchairAttributes;
	private boolean hasWheelchairLeftSidewalkAttributes;
	private boolean hasWheelchairRightSidewalkAttributes;
	private double[] restrictionValues = new double[VehicleRestrictionCodes.Count];
	private double[] passabilityValues = new double[4];
	private double[] wheelchairAttributes = new double[5];
	private double[] wheelchairLeftSidewalkAttributes = new double[5];
	private double[] wheelchairRightSidewalkAttributes = new double[5];
	private List<EdgeIteratorState> wheelchairLeftSidewalkEdges = null;
	private List<EdgeIteratorState> wheelchairRightSidewalkEdges = null;
	private List<String> motorVehicleRestrictions = new ArrayList<String>(5);
	private Set<String> motorVehicleRestrictedValues = new HashSet<String>(5);
	private int attributeTypes = -1;
	private Envelope bbox;
	private HashMap<Integer, Long> tmcEdges;
	private RouteProfile refProfile;
	private boolean enrichInstructions;
	private Boolean isWheelchair = false;
	private HashMap<Long, List<WayWithSidewalk>> sidewalkJunctions;

	private Pattern patternHeight = Pattern.compile("(?:\\s*(\\d+)\\s*(?:feet|ft\\.|ft|'))?(?:(\\d+)\\s*(?:inches|in\\.|in|''|\"))?");

	protected final HashSet<String> ferries = new HashSet<String>(5);

	private String[] TMC_ROAD_TYPES = new String[] { "motorway", "motorway_link", "trunk", "trunk_link", "primary",
			"primary_link", "secondary", "secondary_link", "tertiary", "tertiary_link", "unclassified", "residential" };

	public ORSOSMReader(GraphHopperStorage storage, WaySurfaceTypeStorage waySurfaceStorage, Envelope bbox, HashMap<Integer, Long> tmcEdges, RouteProfile refProfile) {
		super(storage);
		
		this.bbox = bbox;
		this.tmcEdges = tmcEdges;
		this.refProfile = refProfile;
		this.gsWaySurfaceType = waySurfaceStorage;

		motorVehicleRestrictions.addAll(Arrays.asList("motorcar", "motor_vehicle", "vehicle", "access"));

		motorVehicleRestrictedValues.add("private");
		motorVehicleRestrictedValues.add("no");
		motorVehicleRestrictedValues.add("restricted");
		motorVehicleRestrictedValues.add("military");
	        
		enrichInstructions = (refProfile != null) && (storage.getEncodingManager().supports("foot")
				          || storage.getEncodingManager().supports("bike")  
						  || storage.getEncodingManager().supports("MTB")
						  || storage.getEncodingManager().supports("RACINGBIKE")
						  || storage.getEncodingManager().supports("SAFETYBIKE"));

		ferries.add("shuttle_train");
		ferries.add("ferry");

		if (storage instanceof GraphHopperStorage) {
			GraphHopperStorage ghs = (GraphHopperStorage) storage;
			GraphExtension ge = ghs.getExtension();

			if (ge instanceof ExtendedStorageSequence) {
				ExtendedStorageSequence ess = (ExtendedStorageSequence) ge;
				GraphExtension[] exts = ess.getExtensions();
				for (int i = 0; i < exts.length; i++) {
					assignExtension(exts[i]);
					//if (assignExtension(exts[i]))
					//	break;
				}
			} else {
				assignExtension(ge);
			}
		}
		
		sidewalkJunctions = new HashMap<Long, List<WayWithSidewalk>>();
	}

	private boolean assignExtension(GraphExtension ext) {
		if (ext instanceof MotorcarAttributesGraphStorage) {
			this.gsMotorcarAttrs = (MotorcarAttributesGraphStorage) ext;
			this.attributeTypes = gsMotorcarAttrs.getAttributeTypes();
			return true;
		} else if (ext instanceof HeavyVehicleAttributesGraphStorage) {
			this.gsHeavyVehicleAttrs = (HeavyVehicleAttributesGraphStorage) ext;
			this.attributeTypes = gsHeavyVehicleAttrs.getAttributeTypes();
			return true;
		} else if (ext instanceof BikeAttributesGraphStorage) {
			this.gsBikeAttrs = (BikeAttributesGraphStorage) ext;
			this.attributeTypes = gsBikeAttrs.getAttributeTypes();
			return true;
		} else if (ext instanceof WheelchairAttributesGraphStorage) {
			this.gsWheelchair = (WheelchairAttributesGraphStorage) ext;
			return true;
		}

		return false;
	}
	
	
	public OSMReader setEncodingManager( EncodingManager em )
	{
	    super.setEncodingManager(em);
	    this.isWheelchair = encodingManager.supports("wheelchair");
		 
        return this;
    }

	@Override
	protected boolean isInBounds(OSMNode node) {
		if (bbox != null) {
			double x = node.getLon();
			double y = node.getLat();

			return bbox.contains(x, y);
		}

		return super.isInBounds(node);
	}

	private boolean isFerryRoute(OSMWay way) {
		if (way.hasTag("route", ferries)) {

			if (gsMotorcarAttrs != null) {
				String motorcarTag = way.getTag("motorcar");
				if (motorcarTag == null)
					motorcarTag = way.getTag("motor_vehicle");

				if (motorcarTag == null && !way.hasTag("foot") && !way.hasTag("bicycle") || "yes".equals(motorcarTag))
					return true;
			} else if (gsBikeAttrs != null) {
				//return way.hasTag("bicycle", "yes");
				String bikeTag = way.getTag("bicycle");
				if (bikeTag == null && !way.hasTag("foot") || "yes".equals(bikeTag))
					return true;
			} else if (gsWheelchair != null)
			{
				return true;
			}
		}

		return false;
	}
	
    protected Boolean isBarrier(OSMWay way, long nodeFlags, long wayFlags)
    {
    	if (isWheelchair)
    	{
    		if (encodingManager.isBarrier(nodeFlags) || encodingManager.isKerb(nodeFlags))
    		{
    			if ((encodingManager.isBarrier(nodeFlags) && encodingManager.isForward(wayFlags) && encodingManager.isBackward(wayFlags)) || encodingManager.isKerb(nodeFlags))
    			{
    				return true;
    			}      
    		}
    	}
    	
    	return false;
    }

    protected Boolean isKerb(long nodeFlags)
    {
    	if (isWheelchair)
    	{
    		if (encodingManager.isKerb(nodeFlags))
    			return true;
    	}
    	
    	return false;
    }
    @Override
	public void processWay(OSMWay way) {
		
		if (gsMotorcarAttrs != null || gsHeavyVehicleAttrs != null || gsBikeAttrs != null || gsWheelchair != null) {
			   // ignore multipolygon geometry
	        if (!way.hasTags())
	            return;

	        long includeWay = encodingManager.acceptWay(way);
	        if (includeWay == 0)
	            return;
	        
	        wayFlags[0] = 0;
	        wayFlags[1] = 0;
	        wayFlags[2] = 0;
	        wayFlags[3] = 0;
			heavyVehicleType = 0;
			heavyVehicleDestination = 0;
			waySurfaceDesc.Reset();
			
			boolean hasHighway = way.containsTag("highway");
			boolean isFerryRoute = isFerryRoute(way);
			if (gsHeavyVehicleAttrs != null) {
				if (hasHighway && way.hasTag(motorVehicleRestrictions, motorVehicleRestrictedValues))
				{
					heavyVehicleType |= HeavyVehicleAttributes.Bus;
					heavyVehicleType |= HeavyVehicleAttributes.Agricultural;
					heavyVehicleType |= HeavyVehicleAttributes.Forestry;
					heavyVehicleType |= HeavyVehicleAttributes.Delivery;
					heavyVehicleType |= HeavyVehicleAttributes.Goods;
					heavyVehicleType |= HeavyVehicleAttributes.Hgv;
				}
			}
			
			try {
				clearAdditionalValues();
				java.util.Iterator<Entry<String, Object>> it = way.getProperties();

				while (it.hasNext()) {
					Map.Entry<String, Object> pairs = it.next();
					String key = pairs.getKey();
					String value = pairs.getValue().toString();

					if (hasHighway || isFerryRoute) {
						if (key.equals("highway")) {
							if (gsWaySurfaceType != null)
							{
								byte wayType = (isFerryRoute) ? WayType.Ferry : (byte)WayType.getFromString(value);
								
								if (waySurfaceDesc.SurfaceType == 0)
								{
									if (wayType == WayType.Road ||  wayType == WayType.StateRoad || wayType == WayType.Street)
										waySurfaceDesc.SurfaceType = (byte)SurfaceType.Asphalt;
									else if (wayType == WayType.Path)
										waySurfaceDesc.SurfaceType = (byte)SurfaceType.Unpaved;
								}
								
								waySurfaceDesc.WayType = wayType;
							}

							if (value.equals("motorway") || value.equals("motorway_link"))
							{
								wayFlags[0] |= AvoidFeatureFlags.Highway;
								wayFlags[1] |= AvoidFeatureFlags.Highway;
							}
							else if (value.equals("steps"))
							{
								wayFlags[2] |= AvoidFeatureFlags.Steps;
								wayFlags[3] |= AvoidFeatureFlags.Steps;
							}
							else if ("track".equals(value)) {
								String tracktype = way.getTag("tracktype");
								if (tracktype != null
										&& (tracktype.equals("grade1") || tracktype.equals("grade2")
												|| tracktype.equals("grade3") || tracktype.equals("grade4") || tracktype
													.equals("grade5"))) {
									if (gsHeavyVehicleAttrs != null)
									{
										heavyVehicleType |= HeavyVehicleAttributes.Agricultural;
										heavyVehicleType |= HeavyVehicleAttributes.Forestry;
									}
									
									wayFlags[0] |= AvoidFeatureFlags.Tracks;
									wayFlags[1] |= AvoidFeatureFlags.Tracks;
								}
							}

						} else if (key.equals("toll") && value.equals("yes")) {
							wayFlags[0] |= AvoidFeatureFlags.Tollway;
						} else if (gsHeavyVehicleAttrs != null && key.equals("toll:hgv") && value.equals("yes")) {
							wayFlags[1]  |= AvoidFeatureFlags.Tollway;
						} else if (key.equals("route") && isFerryRoute) {
							setWayFlags(AvoidFeatureFlags.Ferries);
						} else if (key.equals("tunnel") && value.equals("yes")) {
							setWayFlags(AvoidFeatureFlags.Tunnels);
						} else if (key.equals("bridge") && value.equals("yes")) {
							setWayFlags(AvoidFeatureFlags.Bridges);
						}
						else if (("ford".equals(key) && value.equals("yes")))
						{
							setWayFlags(AvoidFeatureFlags.Fords);
						}
						/*
						 * todo borders
						 */
						else if (key.equals("surface")) {
							
							if (gsWaySurfaceType != null)
							{
								waySurfaceDesc.SurfaceType = (byte)SurfaceType.getFromString(value);
							}

							if (value.equals("paved") || value.equals("asphalt") || value.equals("cobblestone")
									|| value.equals("cobblestone") || value.equals("cobblestone:flattened")
									|| value.equals("sett") || value.equals("concrete")
									|| value.equals("concrete:lanes") || value.equals("concrete:plates")
									|| value.equals("paving_stones") || value.equals("metal") || value.equals("wood"))
							{
								setWayFlags(AvoidFeatureFlags.PavedRoads);
							}

							if (value.equals("unpaved") || value.equals("compacted") || value.equals("dirt")
									|| value.equals("earth") || value.equals("fine_gravel") || value.equals("grass")
									|| value.equals("grass_paver") || value.equals("gravel") || value.equals("ground")
									|| value.equals("ice") || value.equals("metal") || value.equals("mud")
									|| value.equals("pebblestone") || value.equals("salt") || value.equals("sand")
									|| value.equals("snow") || value.equals("wood") || value.equals("woodchips"))
							{
								setWayFlags(AvoidFeatureFlags.UnpavedRoads);
							}
						} else {
							if ((gsMotorcarAttrs != null && (this.attributeTypes & MotorcarAttributesType.Restrictions) == MotorcarAttributesType.Restrictions)
									|| (gsHeavyVehicleAttrs != null && (this.attributeTypes & HeavyVehicleAttributesType.Restrictions) == HeavyVehicleAttributesType.Restrictions)) {
								int valueIndex = -1;

								if (key.equals("maxheight")) {
									valueIndex = VehicleRestrictionCodes.MaxHeight;
								} else if (key.equals("maxweight")) {
									valueIndex = VehicleRestrictionCodes.MaxWeight;
								} else if (key.equals("maxweight:hgv")) {
								    valueIndex = VehicleRestrictionCodes.MaxWeight;
							    }	else if (key.equals("maxwidth")) {
									valueIndex = VehicleRestrictionCodes.MaxWidth;
								} else if (key.equals("maxlength")) {
									valueIndex = VehicleRestrictionCodes.MaxLength;
								} else if (key.equals("maxlength:hgv")) {
									valueIndex = VehicleRestrictionCodes.MaxLength;
								}
								else if (key.equals("maxaxleload")) {
									valueIndex = VehicleRestrictionCodes.MaxAxleLoad;
								}

								if (valueIndex >= 0 && !("none".equals(value) || "default".equals(value))) {
									if (valueIndex == VehicleRestrictionCodes.MaxWeight || valueIndex == VehicleRestrictionCodes.MaxAxleLoad) {
										if (value.contains("t")) {
											value = value.replace('t', ' ');
										} else if (value.contains("lbs")) {
											value = value.replace("lbs", " ");
											value = Double.toString(Double.parseDouble(value) / 2204.622);
										}
									} else {
										if (value.contains("m")) {
											value = value.replace('m', ' ');
										}
										else if (value.contains("'"))
										{
											Matcher m = patternHeight.matcher(value);
											if (m.find())
											{
												int feet = Integer.parseInt(m.group(1));
												int inches = 0;
												if (m.groupCount() > 1)
												 inches = Integer.parseInt(m.group(2));
												double newValue = feet * 0.3048 + inches * 0.0254*feet;
												value = Double.toString(newValue);
											}
										}
									}

									restrictionValues[valueIndex] = Double.parseDouble(value);
									hasRestrictionValues = true;
								}
							}

							if (gsHeavyVehicleAttrs != null) {
								String hgvTag = getHeavyVehicleValue(key, "hgv", value); //key.equals("hgv") ? value : null;
								String goodsTag = getHeavyVehicleValue(key, "goods", value); // key.equals("goods") ? value : null;
								String busTag = getHeavyVehicleValue(key, "bus", value); //key.equals("bus") ? value : null;
								String agriculturalTag = getHeavyVehicleValue(key, "agricultural", value); //key.equals("agricultural") ? value : null;
								String forestryTag = getHeavyVehicleValue(key, "forestry", value); // key.equals("forestry") ? value : null;
								String deliveryTag = getHeavyVehicleValue(key, "delivery", value); //key.equals("delivery") ? value : null;

								String accessTag = key.equals("access") ? value : null;

								if (Helper.isEmpty(accessTag)) {
									if ("agricultural".equals(accessTag))
										agriculturalTag = "yes";
									else if ("forestry".equals(accessTag))
										forestryTag = "yes";
									else if ("bus".equals(accessTag))
										busTag = "yes";
								}

								String motorVehicle = key.equals("motor_vehicle") ? value : null;
								if (motorVehicle == null)
									motorVehicle = key.equals("motorcar") ? value : null;

								if (motorVehicle != null) {
									if ("agricultural".equals(motorVehicle))
										agriculturalTag = "yes";
									else if ("forestry".equals(motorVehicle))
										forestryTag = "yes";
									else if ("delivery".equals(motorVehicle))
										deliveryTag = "yes";

									//if ("destination".equals(motorVehicle))
									//	heavyVehicleFlag |= HeavyVehicleAttributes.Destination;
								}

								if (goodsTag != null) {
									if ("no".equals(goodsTag))
										heavyVehicleType |= HeavyVehicleAttributes.Goods;
									else if ("yes".equals(busTag))
										heavyVehicleType &= ~HeavyVehicleAttributes.Goods;
									else if ("destination".equals(goodsTag))
									{
										heavyVehicleType |= HeavyVehicleAttributes.Goods;
										heavyVehicleDestination |= HeavyVehicleAttributes.Goods;//(1 << (HeavyVehicleAttributes.Goods >> 1));
									}
								}

								if (hgvTag != null) {
									if ("no".equals(hgvTag))
										heavyVehicleType |= HeavyVehicleAttributes.Hgv;
									else if ("yes".equals(busTag))
										heavyVehicleType &= ~HeavyVehicleAttributes.Hgv;
									else if ("destination".equals(hgvTag))
									{
										heavyVehicleType |= HeavyVehicleAttributes.Hgv;
										heavyVehicleDestination |= HeavyVehicleAttributes.Hgv;// (1 << (HeavyVehicleAttributes.Hgv >> 1));
									}
								}

								if (busTag != null) {
									if ("no".equals(busTag))
										heavyVehicleType |= HeavyVehicleAttributes.Bus;
									else if ("yes".equals(busTag))
										heavyVehicleType &= ~HeavyVehicleAttributes.Bus;
									else if ("destination".equals(busTag))
									{
										heavyVehicleType |= HeavyVehicleAttributes.Bus;
										heavyVehicleDestination |= HeavyVehicleAttributes.Bus; //(1 << (HeavyVehicleAttributes.Bus >> 1));
									}
								}

								if (agriculturalTag != null) {
									if ("no".equals(agriculturalTag))
										heavyVehicleType |= HeavyVehicleAttributes.Agricultural;
									else if ("yes".equals(busTag))
										heavyVehicleType &= ~HeavyVehicleAttributes.Agricultural;
									else if ("destination".equals(agriculturalTag))
									{
										heavyVehicleType |= HeavyVehicleAttributes.Agricultural;
										heavyVehicleDestination |= HeavyVehicleAttributes.Agricultural;// (1 << (HeavyVehicleAttributes.Agricultural >> 1));
									}
								} else

								if (forestryTag != null) {
									if ("no".equals(forestryTag))
										heavyVehicleType |= HeavyVehicleAttributes.Forestry;
									else if ("yes".equals(busTag))
										heavyVehicleType &= ~HeavyVehicleAttributes.Forestry;
									else if ("destination".equals(forestryTag))
									{
										heavyVehicleType |= HeavyVehicleAttributes.Forestry;
										heavyVehicleDestination |= HeavyVehicleAttributes.Forestry;//(1 << (HeavyVehicleAttributes.Forestry >> 1));
									}
								}

								if (deliveryTag != null) {
									if ("no".equals(deliveryTag))
										heavyVehicleType |= HeavyVehicleAttributes.Delivery;
									else if ("yes".equals(busTag))
										heavyVehicleType &= ~HeavyVehicleAttributes.Delivery;
									else if ("destination".equals(deliveryTag) || "delivery".equals(deliveryTag) )
									{
										heavyVehicleType |= HeavyVehicleAttributes.Delivery;
										heavyVehicleDestination |= HeavyVehicleAttributes.Delivery; //(1 << (HeavyVehicleAttributes.Delivery >> 1));
									}
								}

								String hazmatTag = key.equals("hazmat") ? value : null;
								if ("no".equals(hazmatTag)) {
									heavyVehicleType |= HeavyVehicleAttributes.Hazmat;
								}

								// (access=no) + access:conditional=delivery @
								// (07:00-11:00); customer @ (07:00-17:00)
							}

							if (gsMotorcarAttrs != null
									&& (this.attributeTypes & MotorcarAttributesType.Passability) == MotorcarAttributesType.Passability) {
								if (key.equals("track")) {
									passabilityValues[0] = ConvertUtils.getTrackValue(way.getTag("track"));
									hasPassabilityValues = true;
								} else if (key.equals("surface")) {
									passabilityValues[1] = ConvertUtils.getSmoothnessValue(way.getTag("surface"));
									hasPassabilityValues = true;
								} else if (key.equals("smoothness")) {
									passabilityValues[2] = ConvertUtils.getSmoothnessValue(way.getTag("smoothness"));
									hasPassabilityValues = true;
								}
							}
						}
					}
				}
				// wheelchair stuff from WheelchairFlagEncoder
				if (gsWheelchair != null) {
					
					// default value for surface (paved=0)
					wheelchairAttributes[WheelchairRestrictionCodes.SURFACE] = WheelchairRestrictionCodes.SURFACE_PAVED;
		         	
		         	// surface value
		         	// extract value for surface
		         	if (way.hasTag("sidewalk")) {
		         		if (way.hasTag("surface")) {
		    				String tagValue = way.getTag("surface").toLowerCase();
		    				if (WheelchairRestrictionCodes.SURFACE_MAP.containsKey(tagValue)) {
		    					// set value
		    					wheelchairAttributes[WheelchairRestrictionCodes.SURFACE]=WheelchairRestrictionCodes.SURFACE_MAP.get(tagValue);
		    					hasWheelchairAttributes = true;
		    				}
		         		}
		         	}
		         	// if no separate sidewalk surface is available try to use surface from the highway
		         	else {
		         		if (way.hasTag("surface")) {
		    				String tagValue = way.getTag("surface").toLowerCase();
		    				if (WheelchairRestrictionCodes.SURFACE_MAP.containsKey(tagValue)) {
		    					// set value
		    					wheelchairAttributes[WheelchairRestrictionCodes.SURFACE]=WheelchairRestrictionCodes.SURFACE_MAP.get(tagValue);
		    					hasWheelchairAttributes = true;
		    				}
		         		}
		         	}
		         	
		         	// default value for smoothness (excellent=0)
		         	wheelchairAttributes[WheelchairRestrictionCodes.SMOOTHNESS] = WheelchairRestrictionCodes.SMOOTHNESS_EXCELLENT;
		         	
		         	// smoothness value
		         	// extract value for surface
		         	if (way.hasTag("sidewalk")) {
		         		if (way.hasTag("smoothness")) {
		    				String tagValue = way.getTag("smoothness").toLowerCase();
		    				if (WheelchairRestrictionCodes.SMOOTHNESS_MAP.containsKey(tagValue)) {
		    					// set value
		    					wheelchairAttributes[WheelchairRestrictionCodes.SMOOTHNESS] = WheelchairRestrictionCodes.SMOOTHNESS_MAP.get(tagValue);
		    					hasWheelchairAttributes = true;
		    				}
		         		}
		         	}
		         	// if no separate sidewalk smoothness is available try to use smoothness directly from the highway
		         	else {
		         		if (way.hasTag("smoothness")) {
		    				String tagValue = way.getTag("smoothness").toLowerCase();
		    				if (WheelchairRestrictionCodes.SMOOTHNESS_MAP.containsKey(tagValue)) {
		    					// set value
		    					wheelchairAttributes[1] = WheelchairRestrictionCodes.SMOOTHNESS_MAP.get(tagValue);
		    					hasWheelchairAttributes = true;
		    				}
		         		}
		         	}
		         	
		         	/*
		         	// TODO: also move to WheelchairAttributeGraphStorage?
		         	// sloped_curb
					// ===========
					// http://wiki.openstreetmap.org/wiki/Wheelchair_routing#Curb_heights
					// http://wiki.openstreetmap.org/wiki/DE:Wheelchair_routing#B.C3.BCrgersteige
					// http://wiki.openstreetmap.org/wiki/DE:Wheelchair_routing#B.C3.BCrgersteige_und_Eigenschaften
					// http://wiki.openstreetmap.org/wiki/Key:sloped_curb
		         	
		         	// only use sloped_curb|kerb|curb values on ways that are crossing. there are cases (e.g. platform) where these tags are also used but in fact indicate wheelchair accessibility (e.g. platform=yes, kerb=raised)
		         	if ((way.hasTag("sloped_curb") || way.hasTag("kerb") || way.hasTag("curb")) && (way.hasTag("footway", "crossing") || way.hasTag("cycleway", "crossing") || way.hasTag("highway", "crossing") || way.hasTag("crossing"))) {
		         		double doubleValue = getKerbHeight(way);
						
						// set value
						encoded |= slopedCurbEncoder.setDoubleValue(0, doubleValue);
		         	}
		         	*/

		         	
		         	
		         	// default value for tracktype (tracktype1=0)
		         	wheelchairAttributes[WheelchairRestrictionCodes.TRACKTYPE] = WheelchairRestrictionCodes.TRACKTYPE_GRADE1;
		         	
		         	// tracktype value
		         	// extract value for tracktype
		         	// if no separate sidewalk tracktype is available try to use tracktype from the highway
		     		if (way.hasTag("tracktype")) {
						String tagValue = way.getTag("tracktype").toLowerCase();
						if (WheelchairRestrictionCodes.TRACKTYPE_MAP.containsKey(tagValue)) {
							// set value
							wheelchairAttributes[WheelchairRestrictionCodes.TRACKTYPE] = WheelchairRestrictionCodes.TRACKTYPE_MAP.get(tagValue);
							hasWheelchairAttributes = true;
						}
		     		}
		         	
		         	
		         	// default value for incline
		     		wheelchairAttributes[WheelchairRestrictionCodes.INCLINE] = 0;
		         	
		         	// incline
					// =======
					// http://wiki.openstreetmap.org/wiki/Key:incline
					// http://wiki.openstreetmap.org/wiki/Wheelchair_routing#Path_properties.2C_in_general
					// http://wiki.openstreetmap.org/wiki/DE:Wheelchair_routing#Weg_Eigenschaften_allgemein
		     		String inclineValue = way.getTag("incline");
		         	if (inclineValue != null) {
						double v = getIncline(inclineValue);
						wheelchairAttributes[WheelchairRestrictionCodes.INCLINE] = v;
						hasWheelchairAttributes = true;
		         	}
				}
			} 
			catch (Exception ex) {
				Logger logger = Logger.getLogger(RouteProfileManager.class.getName());
				logger.warning(ex.getMessage());
			}
		}

		super.processWay(way);
	}
	
	private void setWayFlags(int value)
	{
		wayFlags[0] |= value;
		wayFlags[1] |= value;
		wayFlags[2] |= value;
		wayFlags[3] |= value;
	}

	private String getHeavyVehicleValue(String key, String hv, String value)
	{
		if (value.equals(hv) || (key.equals(hv) && ("yes".equals(value) || "no".equals(value))))
			return value;
		else 
		{
			if (key.equals(hv+":forward") || key.equals(hv+":backward"))
				return value;
		}
		
		return null;
	}
	
	private void clearAdditionalValues() {
		if (!hasRestrictionValues && !hasPassabilityValues && !hasWheelchairAttributes && !hasWheelchairLeftSidewalkAttributes && !hasWheelchairRightSidewalkAttributes && wheelchairLeftSidewalkEdges == null && wheelchairRightSidewalkEdges == null)
			return;

		if (hasRestrictionValues) {
			restrictionValues[0] = 0.0;
			restrictionValues[1] = 0.0;
			restrictionValues[2] = 0.0;
			restrictionValues[3] = 0.0;
			restrictionValues[4] = 0.0;
		}
		
		if (hasPassabilityValues) {
			passabilityValues[0] = 0.0;
			passabilityValues[1] = 0.0;
			passabilityValues[2] = 0.0;
			passabilityValues[3] = 0.0;
		}
		
		if (hasWheelchairAttributes) {
			wheelchairAttributes[WheelchairRestrictionCodes.SURFACE] = 0d;
			wheelchairAttributes[WheelchairRestrictionCodes.SMOOTHNESS] = 0d;
			wheelchairAttributes[WheelchairRestrictionCodes.SLOPED_CURB] = 0d;
			wheelchairAttributes[WheelchairRestrictionCodes.TRACKTYPE] = 0d;
			wheelchairAttributes[WheelchairRestrictionCodes.INCLINE] = 0d;
		}
		
		if (hasWheelchairLeftSidewalkAttributes) {
			wheelchairLeftSidewalkAttributes[WheelchairRestrictionCodes.SURFACE] = 0d;
			wheelchairLeftSidewalkAttributes[WheelchairRestrictionCodes.SMOOTHNESS] = 0d;
			wheelchairLeftSidewalkAttributes[WheelchairRestrictionCodes.SLOPED_CURB] = 0d;
			wheelchairLeftSidewalkAttributes[WheelchairRestrictionCodes.TRACKTYPE] = 0d;
			wheelchairLeftSidewalkAttributes[WheelchairRestrictionCodes.INCLINE] = 0d;
		}
		
		if (hasWheelchairRightSidewalkAttributes) {
			wheelchairRightSidewalkAttributes[WheelchairRestrictionCodes.SURFACE] = 0d;
			wheelchairRightSidewalkAttributes[WheelchairRestrictionCodes.SMOOTHNESS] = 0d;
			wheelchairRightSidewalkAttributes[WheelchairRestrictionCodes.SLOPED_CURB] = 0d;
			wheelchairRightSidewalkAttributes[WheelchairRestrictionCodes.TRACKTYPE] = 0d;
			wheelchairRightSidewalkAttributes[WheelchairRestrictionCodes.INCLINE] = 0d;
		}
		
		if (wheelchairLeftSidewalkEdges != null) {
			wheelchairLeftSidewalkEdges = null;
		}
		
		if (wheelchairRightSidewalkEdges != null) {
			wheelchairRightSidewalkEdges = null;
		}

		hasRestrictionValues = false;
		hasPassabilityValues = false;
		hasWheelchairAttributes = false;
		hasWheelchairLeftSidewalkAttributes = false;
		hasWheelchairRightSidewalkAttributes = false;
	}

	private long prevMatchedWayId = -1;
	private String matchedEdgeName = null;
	
	protected void processEdge(OSMWay way, EdgeIteratorState edge) {

		if (enrichInstructions && Helper.isEmpty(way.getTag("name")) && Helper.isEmpty(way.getTag("ref"))) {
			try {
			/*	if (way.getId() != prevMatchedWayId)
				{
					prevMatchedWayId = way.getId();
					PointList pl = getWayPoints(way);
					matchedEdgeName = null;
					RouteSegmentInfo rsi = refProfile.getMatchedSegment(pl, 15.0);

					if (rsi != null) {
						String objName = rsi.getNearbyStreetName(pl, true);
						if (!Helper.isEmpty(objName)) {
							matchedEdgeName = objName;
							way.setTag("name", matchedEdgeName);
						}
					}
				}
				
				if (!Helper.isEmpty(matchedEdgeName)) {
					edge.setName(matchedEdgeName);
				}*/
				
			} 
			catch (Exception ex) {
			}
		}

		super.processEdge(way, edge);

		try {
			if (tmcEdges != null) {
				String highwayValue = way.getTag("highway");

				if (!Helper.isEmpty(highwayValue)) {

					for (int i = 0; i < TMC_ROAD_TYPES.length; i++) {
						if (TMC_ROAD_TYPES[i].equalsIgnoreCase(highwayValue)) {
							tmcEdges.put(edge.getEdge(), way.getId());
							break;
						}
					}
				}
			}
			
			if (gsWaySurfaceType != null) {
				gsWaySurfaceType.setEdgeValue(edge.getEdge(), waySurfaceDesc);
			}

			if (gsMotorcarAttrs != null) {
				if (wayFlags[0] > 0 || hasRestrictionValues || hasPassabilityValues) {
					if (hasRestrictionValues && hasPassabilityValues)
						gsMotorcarAttrs.setEdgeValue(edge.getEdge(), wayFlags[0], restrictionValues, passabilityValues);
					else if (hasRestrictionValues)
						gsMotorcarAttrs.setEdgeValue(edge.getEdge(), wayFlags[0], restrictionValues, null);
					else
						gsMotorcarAttrs.setEdgeValue(edge.getEdge(), wayFlags[0], null, null);
				}
			} 
			
			if (gsHeavyVehicleAttrs != null) {
				if (wayFlags[1] > 0 || heavyVehicleType > HeavyVehicleAttributes.Unknown || heavyVehicleDestination > 0 || hasRestrictionValues) {
					if (hasRestrictionValues)
						gsHeavyVehicleAttrs.setEdgeValue(edge.getEdge(), wayFlags[1], heavyVehicleType, heavyVehicleDestination, restrictionValues);
					else
						gsHeavyVehicleAttrs.setEdgeValue(edge.getEdge(), wayFlags[1], heavyVehicleType, heavyVehicleDestination, null);
				}
			} 
			
			if (gsBikeAttrs != null) {
				if (wayFlags[2] > 0) {
					if ((wayFlags[2] & AvoidFeatureFlags.Ferries) == AvoidFeatureFlags.Ferries
							|| (wayFlags[2] & AvoidFeatureFlags.Steps) == AvoidFeatureFlags.Steps
							|| (wayFlags[2] & AvoidFeatureFlags.UnpavedRoads) == AvoidFeatureFlags.UnpavedRoads) {
						gsBikeAttrs.setEdgeValue(edge.getEdge(), wayFlags[2]);
					}
				}
			} 
			
			if (gsWheelchair != null) {
				if ((wayFlags[3] & AvoidFeatureFlags.Ferries) == AvoidFeatureFlags.Ferries || hasWheelchairAttributes || hasWheelchairLeftSidewalkAttributes || hasWheelchairRightSidewalkAttributes || wheelchairLeftSidewalkEdges != null || wheelchairRightSidewalkEdges != null) {
					
					double wheelchairAttributes[] = new double[5];
					if (hasWheelchairAttributes) {
						System.arraycopy(this.wheelchairAttributes, 0, wheelchairAttributes, 0, 5);
					}
					// System.out.println("1 ORSOSMReader.processEdge(), wayName	="+way.getTag("name")+", hasWheelchairRightSidewalkAttributes="+hasWheelchairRightSidewalkAttributes+", edgeId="+edge.getEdge()+", wheelchairRightSidewalkEdges="+wheelchairRightSidewalkEdges);
					
					if (wheelchairLeftSidewalkEdges != null && hasWheelchairLeftSidewalkAttributes) {
						for (int i = 0; i < wheelchairLeftSidewalkEdges.size(); i++) {
							if (edge.getEdge() == wheelchairLeftSidewalkEdges.get(i).getEdge()) {
								if (wheelchairLeftSidewalkAttributes[WheelchairRestrictionCodes.SURFACE] != 0) {
									wheelchairAttributes[WheelchairRestrictionCodes.SURFACE] = wheelchairLeftSidewalkAttributes[WheelchairRestrictionCodes.SURFACE];
								}
								if (wheelchairLeftSidewalkAttributes[WheelchairRestrictionCodes.SMOOTHNESS] != 0) {
									wheelchairAttributes[WheelchairRestrictionCodes.SMOOTHNESS] = wheelchairLeftSidewalkAttributes[WheelchairRestrictionCodes.SMOOTHNESS];
								}
								break;
							}
						}
					}
					if (wheelchairRightSidewalkEdges != null && hasWheelchairRightSidewalkAttributes) {
						// System.out.println("2 ORSOSMReader.processEdge(), wheelchairRightSidewalkEdges.size()="+wheelchairRightSidewalkEdges.size());
						for (int i = 0; i < wheelchairRightSidewalkEdges.size(); i++) {
							// System.out.println("3 ORSOSMReader.processEdge(), edgeId="+edge.getEdge()+", wheelchairRightSidewalkEdges.get("+i+").getEdge()="+wheelchairRightSidewalkEdges.get(i).getEdge());
							if (edge.getEdge() == wheelchairRightSidewalkEdges.get(i).getEdge()) {
								if (wheelchairRightSidewalkAttributes[WheelchairRestrictionCodes.SURFACE] != 0) {
									wheelchairAttributes[WheelchairRestrictionCodes.SURFACE] = wheelchairRightSidewalkAttributes[WheelchairRestrictionCodes.SURFACE];
								}
								if (wheelchairRightSidewalkAttributes[WheelchairRestrictionCodes.SMOOTHNESS] != 0) {
									wheelchairAttributes[WheelchairRestrictionCodes.SMOOTHNESS] = wheelchairRightSidewalkAttributes[WheelchairRestrictionCodes.SMOOTHNESS];
								}
								break;
							}
						}
					}
					gsWheelchair.setEdgeValue(edge.getEdge(), wayFlags[3], wheelchairAttributes);
				}
			}
			
		} catch (Exception ex) {
			Logger logger = Logger.getLogger(RouteProfileManager.class.getName());
			logger.warning(ex.getMessage() + ". Way id = " + way.getId());
		}
	}
	
		// needs a mechanism that ensures that non-wheelchair and non-pedestrian profiles are not using the interpolated edges
		// 2 possible options: 
		// 1. EdgeFilter for all non wheelchair/pedestrian profiles
		// 2. Separate Graph for wheelchair/pedestrian profile (perhaps easier in terms of implementation than option 1, but quite memory consuming...)
		// solution 2. has been implemented (cf. OSMReader class)
		@Override
	    protected Collection<EdgeIteratorState> addSidewalks(OSMWay way, long wayFlags) {
	    	List<EdgeIteratorState> createdEdges = new ArrayList<EdgeIteratorState>();
	    	if (way.containsTag("highway") && way.containsTag("sidewalk")) {
	    		boolean doLeft = false;
	    		boolean doRight = false;
	    		if (way.getTag("sidewalk").contains("both") || way.getTag("sidewalk").contains("yes")) {
	    			doLeft = true;
	    			doRight = true;
	    		}
	    		else if (way.getTag("sidewalk").contains("left")) {
	    			doLeft = true;
	    		}
	    		else if (way.getTag("sidewalk").contains("right")) {
	    			doRight = true;
	    		}
	    		// ~5 meters
	    		double offset = 0.00005;
	    		
	    		encodingManager.getEncoder(EncodingManager.WHEELCHAIR);
	        	wayFlags = encodingManager.setSidewalkSpeed(wayFlags);
	        	
	        	double[] leftSidewalkAttributes = getSidewalkAttributes(way, "left");
	        	if (leftSidewalkAttributes != null) {
	        		hasWheelchairLeftSidewalkAttributes = true;
	        		wheelchairLeftSidewalkAttributes = leftSidewalkAttributes;
	        	}
				double[] rightSidewalkAttributes = getSidewalkAttributes(way, "right");
				if (rightSidewalkAttributes != null) {
					hasWheelchairRightSidewalkAttributes = true;
					wheelchairRightSidewalkAttributes = rightSidewalkAttributes;
				}
				
	    		if (doLeft) {
	    			// System.out.println("---- ORSOSMReader.addSidewalks(), left, wayNodes="+way.getNodes());
	    			List<EdgeIteratorState> leftSidewalkEdges = (List<EdgeIteratorState>)addSidewalk(way, wayFlags, -offset, leftSidewalkAttributes, rightSidewalkAttributes);
	    			this.wheelchairLeftSidewalkEdges = leftSidewalkEdges;
	    			// System.out.println("ORSOSMReader.addSidewalks(), wayName="+way.getTag("name")+", leftSidewalkEdges.size()="+leftSidewalkEdges.size()+", leftSidewalkAttributes="+leftSidewalkAttributes);
	        		createdEdges.addAll(leftSidewalkEdges);
	    		}
	    		if (doRight) {
	    			// System.out.println("---- ORSOSMReader.addSidewalks(), right, wayNodes="+way.getNodes());
	    			List<EdgeIteratorState> rightSidewalkEdges = (List<EdgeIteratorState>)addSidewalk(way, wayFlags, offset, leftSidewalkAttributes, rightSidewalkAttributes);
	    			this.wheelchairRightSidewalkEdges = rightSidewalkEdges;
	    			// System.out.println("ORSOSMReader.addSidewalks(), wayName="+way.getTag("name")+", rightSidewalkEdges.size()="+rightSidewalkEdges.size()+", rightSidewalkAttributes="+rightSidewalkAttributes);
	        		createdEdges.addAll(rightSidewalkEdges);
	    		}
	    		
	        	return createdEdges;
	    	}
	    	return createdEdges;
		}
		
	    /**
	     * Derives a sidewalk that is parallel to an OSM <code>way</code> at a constant <code>offset</code>.
	     * The flags of the way are transfered to the derived sidewalk
	     * 
	     * @param way
	     * @param wayFlags
	     * @param offset
	     * @return a collection of edges of the derived sidewalk
	     */
	    protected Collection<EdgeIteratorState> addSidewalk(OSMWay way, long wayFlags, double offset, double[] leftSidewalkAttributes, double[] rightSidewalkAttributes) {
	    	// String wayName = way.getTag("name");
	    	// the flags of the way are transfered to the sidewalk
	    	// ~33 meters
	    	double close = 0.0003;
	    	// the interpolated sidewalk nodes
			TLongList interpolatedSidewalkNodes = new TLongArrayList(5);
			// temporarily store the flags of crossing nodes
			TLongList crossingNodeFlags = new TLongArrayList();
			// Transfer of crossing nodes to interpolated crossing edges depends on whether the first node is a crossing/junction
			boolean isFirstNodeCrossing = false;
			// the actual nodes of the way that serve as a base for interpolation
			TLongList wayNodes = way.getNodes();
			// the OSM way id
			long wayOsmId = way.getId();
			// the actual created new sidewalk edges		
			List<EdgeIteratorState> createdSidewalkEdges = new ArrayList<EdgeIteratorState>();
			// helper to store junctions on a way
			List<Long> wayJunctionsList = new ArrayList<Long>();
			
			double[] wheelchairAttributes = new double[5];
			System.arraycopy(this.wheelchairAttributes, 0, wheelchairAttributes, 0, this.wheelchairAttributes.length);
			
			// a list of inter polated sidewalk sections
			List<TLongList> interpolatedSidewalkSections = new ArrayList<TLongList>(0);
			
			// System.out.println("++ ORSOSMReader.addSidewalk(), nodes="+wayNodes.size()+", wayName="+way.getTag("name"));
			
			// interpolate points
			for(int i = 0; i < wayNodes.size(); i++) {
				
				// window of points, last point, current point, next point
				long lastOsmNodeId = Long.MIN_VALUE;
				long currentOsmNodeId = Long.MIN_VALUE;
				long nextOsmNodeId = Long.MIN_VALUE;
				
				currentOsmNodeId = wayNodes.get(i);
				if (i < wayNodes.size() - 1) {
					nextOsmNodeId = wayNodes.get(i+1);
				}
				if (i > 0) {
					lastOsmNodeId = wayNodes.get(i-1);
				}
				
				// last point
				long lastOsmNodeFlags = getNodeFlagsMap().get(lastOsmNodeId);
				int lastInternalNodeId = getNodeMap().get(lastOsmNodeId);
			    double lastLat = getTmpLatitude(lastInternalNodeId);
				double lastLon = getTmpLongitude(lastInternalNodeId);
				boolean isLastCrossing = encodingManager.isCrossing(lastOsmNodeFlags);
				boolean isLastJunction = lastInternalNodeId < TOWER_NODE && !isLastCrossing;

				// current point
				long currentOsmNodeFlags = getNodeFlagsMap().get(currentOsmNodeId);
			    int currentInternalNodeId = getNodeMap().get(currentOsmNodeId);
			    double currentLat = getTmpLatitude(currentInternalNodeId);
				double currentLon = getTmpLongitude(currentInternalNodeId);
				boolean isCurrentCrossing = encodingManager.isCrossing(currentOsmNodeFlags);
				boolean isCurrentJunction = currentInternalNodeId < TOWER_NODE && !isCurrentCrossing;

				// next point
				long nextOsmNodeFlags = getNodeFlagsMap().get(nextOsmNodeId);
			    int nextInternalNodeId = getNodeMap().get(nextOsmNodeId);
			    double nextLat = getTmpLatitude(nextInternalNodeId);
			    double nextLon = getTmpLongitude(nextInternalNodeId);
			    boolean isNextCrossing = encodingManager.isCrossing(nextOsmNodeFlags);
			    
			    // System.out.println("ORSOSMReader.addSidewalk(), i="+i+", isNextCrossing="+isNextCrossing+", nextOsmNodeId="+nextOsmNodeId+", nextOsmNodeFlags="+String.format("%64s", Long.toBinaryString(nextOsmNodeFlags)).replace(" ", "0"));
			    boolean isNextJunction = nextInternalNodeId < TOWER_NODE && !isNextCrossing;

			    // interpolated a helper point, if a way section is between two junction to keep following logics consistent
			    if (isCurrentJunction && isNextJunction) {
			    	
			    	OSMNode tempPoint = getMidPoint(currentLat, currentLon, nextLat, nextLon);
			    	
			    	// prepare interpolated point
				    prepareHighwayNode(tempPoint.getId());

				    // add interpolated point to nodes
				    addNode(tempPoint);
			    	
				    // interpolate new Node
				    wayNodes.insert(i+1, tempPoint.getId());
				    
				    nextOsmNodeFlags = currentOsmNodeFlags & nextOsmNodeFlags;
				    nextInternalNodeId = getNodeMap().get(tempPoint.getId());
				    nextLat = tempPoint.getLat();
				    nextLon = tempPoint.getLon();
				    isNextJunction = false;
			    }
			    
			    
			    // pythagoras
				double nextLength = 0d;
				double lastLength = 0d;
				if (i < wayNodes.size() - 1) {
					nextLength = Math.sqrt(((currentLon - nextLon) * (currentLon - nextLon) + (currentLat - nextLat) * (currentLat - nextLat)));
				}
				if (i > 0) {
					lastLength = Math.sqrt(((lastLon - currentLon) * (lastLon - currentLon) + (lastLat - currentLat) * (lastLat - currentLat)));
				}

				// interpolated point
				double interpolatedLat = 0d;
				double interpolatedLon = 0d;
				if (i < wayNodes.size() - 1) {
					interpolatedLat = currentLat + offset * (currentLon - nextLon) / nextLength;
					interpolatedLon = currentLon + offset * (nextLat - currentLat) / nextLength;
				}
				else {
					interpolatedLat = currentLat + offset * -1 * (currentLon - lastLon) / lastLength;
					interpolatedLon = currentLon + offset * -1 * (lastLat - currentLat) / lastLength;
				}
				
				
				// create the interpolated node
				OSMNode interpolatedPoint = new OSMNode(createNewNodeId(), interpolatedLat, interpolatedLon);
				
				// prepare interpolated point
			    prepareHighwayNode(interpolatedPoint.getId());
			    if (isCurrentCrossing || isCurrentJunction || isLastJunction || isNextJunction) {
			    	// prepare 2nd time to make interpolated point a tower node in case of a kerb/crossing
			    	prepareHighwayNode(interpolatedPoint.getId());
			    }
			    
			    // add interpolated point to nodes
			    addNode(interpolatedPoint);
			    
			    
			    // add a highway=crossing node to interpolated points and make it a tower node
			    if (isCurrentCrossing) {
			    	
		    		crossingNodeFlags.add(currentOsmNodeFlags);
			    	
			    	// add the interpolated point a 2nd time in order to allow routing from both directions
			    	// if (!(isLastJunction && lastLength < close)) {
		    		if (!isLastJunction) {
				    	interpolatedSidewalkNodes.add(interpolatedPoint.getId());
				    }
			    	
				    // make also the crossing a tower node, if it is not already one
			    	if (currentInternalNodeId >= TOWER_NODE) {
			    		addTowerNode(currentOsmNodeId, currentLat, currentLon, getTmpElevation(currentInternalNodeId));		    	
			    	}
			    	
			    	// add the highway=crossing node
			    	interpolatedSidewalkNodes.add(currentOsmNodeId);
			    	
			    	// add the interpolated point a 2nd time in order to allow routing from both directions
			    	// if (!(isNextJunction && nextLength < close)) {
			    	if (!isNextJunction) {
			    		interpolatedSidewalkNodes.add(interpolatedPoint.getId());
			    	}
			    	
			    	// is this is a point between two junctions the interpolated point needs to be added anyway
			    	if (isLastJunction && isNextJunction) {
			    		interpolatedSidewalkNodes.add(interpolatedPoint.getId());
			    	}
				}
			    else if (isCurrentJunction) {
				    // skip junction nodes (they get handled in handleSidewalkJunctions()
			    	// insert a new way section instead
			    	if (interpolatedSidewalkNodes.size() > 0) {
			    		interpolatedSidewalkSections.add(interpolatedSidewalkNodes);
			    		interpolatedSidewalkNodes = new TLongArrayList(5);
			    	}
			    	
			    	/*
			    	// add the interpolated point a 2nd time in order to allow routing from both directions
			    	if (i > 0) {
			    		interpolatedSidewalkNodes.add(interpolatedPoint.getId());
			    	}			    	
			    	
			    	// add the highway=crossing node
			    	interpolatedSidewalkNodes.add(currentOsmNodeId);
			    	
			    	// add the interpolated point a 2nd time in order to allow routing from both directions
			    	if (i < wayNodes.size() -1) {
			    		interpolatedSidewalkNodes.add(interpolatedPoint.getId());
			    	}
			    	*/
			    }
			    else {
			    	// insert the interpolated sidewalk node
			    	interpolatedSidewalkNodes.add(interpolatedPoint.getId());
			    }
			    
			    // System.out.println("ORSOSMReader.addSidewalk(), wayId="+way.getId()+", wayName="+way.getTag("name")+", wheelchairAttributes[WheelchairRestrictionCodes.INCLINE]="+wheelchairAttributes[WheelchairRestrictionCodes.INCLINE]);
			    
			    // handle junction nodes
			    if (isCurrentJunction) {
					if (!Double.isNaN(lastLat) && !Double.isNaN(lastLat)) {
						float bearing =  (float)Math.toDegrees(Math.atan2((currentLon-lastLon), (currentLat-lastLat))) + 180;
						List<WayWithSidewalk> waysWithSidewalk = null;
						waysWithSidewalk = sidewalkJunctions.get(currentOsmNodeId);
						if (waysWithSidewalk == null) {
		 					waysWithSidewalk = new ArrayList<WayWithSidewalk>();
		 				}
		 				WayWithSidewalk wayWithSidewalk = null;
		 				long wayId = way.getId();
	 					if (wayJunctionsList.contains(currentOsmNodeId)) {
	 						wayId = - wayId;
	 					}
		 				for (WayWithSidewalk wws : waysWithSidewalk) {
							if (wws.osmWayId == wayId) {
								wayWithSidewalk = wws;
							}
						}
		 				if (wayWithSidewalk == null) {
		 					wayWithSidewalk = new WayWithSidewalk(wayId, wayFlags, bearing, this.wayFlags[3], wheelchairAttributes);
		 					// wayWithSidewalk.wayName = way.getTag("name");
		 					waysWithSidewalk.add(wayWithSidewalk);
		 				}

			 			// ----
			 			// define nodes at junction that can be interconnected
		 				int interpolatedInternalId = getNodeMap().get(interpolatedPoint.getId());
		 				// left in way direction
						if (offset < 0) {
							wayWithSidewalk.leftJunctionNodeId =  interpolatedInternalId;
							wayWithSidewalk.setWheelchairLeftSidewalkAttibutes(leftSidewalkAttributes);
							if (!wayJunctionsList.contains(currentOsmNodeId) && wayWithSidewalk.leftJunctionNodeId != 0 && wayWithSidewalk.leftPreJunctionNodeId != 0) {
		 						wayJunctionsList.add(currentOsmNodeId);
		 					}
						}
						// right in way direction
						if (offset > 0) {
							wayWithSidewalk.rightJunctionNodeId = interpolatedInternalId;
							wayWithSidewalk.setWheelchairRightSidewalkAttibutes(rightSidewalkAttributes);
							if (!wayJunctionsList.contains(currentOsmNodeId) && wayWithSidewalk.rightJunctionNodeId != 0 && wayWithSidewalk.rightPreJunctionNodeId != 0) {
		 						wayJunctionsList.add(currentOsmNodeId);
		 					}
						}
						
						if (isLastCrossing && lastLength < close) {
							wayWithSidewalk.hasCloseCrossing = true;
						}
						
						sidewalkJunctions.put(currentOsmNodeId, waysWithSidewalk);
					}
					
					if (!Double.isNaN(nextLon) && !Double.isNaN(nextLat)) {
						float bearing =  (float)Math.toDegrees(Math.atan2((currentLon-nextLon), (currentLat-nextLat))) + 180;
						List<WayWithSidewalk> waysWithSidewalk = null;
						waysWithSidewalk = sidewalkJunctions.get(currentOsmNodeId);
						if (waysWithSidewalk == null) {
		 					waysWithSidewalk = new ArrayList<WayWithSidewalk>();
		 				}
		 				WayWithSidewalk wayWithSidewalk = null;
		 				long wayId = way.getId();
	 					if (wayJunctionsList.contains(currentOsmNodeId)) {
	 						wayId = - wayId;
	 					}
		 				for (WayWithSidewalk wws : waysWithSidewalk) {
							if (wws.osmWayId == wayId) {
								wayWithSidewalk = wws;
							}
						}
		 				if (wayWithSidewalk == null) {
		 					wayWithSidewalk = new WayWithSidewalk(wayId, wayFlags, bearing, this.wayFlags[3], wheelchairAttributes);
		 					// wayWithSidewalk.wayName = way.getTag("name");
		 					waysWithSidewalk.add(wayWithSidewalk);
		 				}

			 			// ----
			 			// define nodes at junction that can be interconnected
		 				int interpolatedInternalId = getNodeMap().get(interpolatedPoint.getId());
						// left in way direction
						if (offset < 0) {
							wayWithSidewalk.rightJunctionNodeId = interpolatedInternalId;
							wayWithSidewalk.setWheelchairRightSidewalkAttibutes(leftSidewalkAttributes);
							if (!wayJunctionsList.contains(currentOsmNodeId) && wayWithSidewalk.rightJunctionNodeId != 0 && wayWithSidewalk.rightPreJunctionNodeId != 0) {
		 						wayJunctionsList.add(currentOsmNodeId);
		 					}
						}
						// right in way direction
						if (offset > 0) {
							wayWithSidewalk.leftJunctionNodeId = interpolatedInternalId;
							wayWithSidewalk.setWheelchairLeftSidewalkAttibutes(rightSidewalkAttributes);
							if (!wayJunctionsList.contains(currentOsmNodeId) && wayWithSidewalk.leftJunctionNodeId != 0 && wayWithSidewalk.leftPreJunctionNodeId != 0) {
		 						wayJunctionsList.add(currentOsmNodeId);
		 					}
						}
						
						if (isNextCrossing && nextLength < close) {
							wayWithSidewalk.hasCloseCrossing = true;
						}
						
						sidewalkJunctions.put(currentOsmNodeId, waysWithSidewalk);
					}
				}
				
			    // handle junction nodes
				if (isLastJunction) {
	 				List<WayWithSidewalk> waysWithSidewalk = null;
	 				float bearing = (float)Math.toDegrees(Math.atan2((lastLon-currentLon), (lastLat-currentLat))) + 180;
	 	 			waysWithSidewalk = sidewalkJunctions.get(lastOsmNodeId);
	 	 			if (waysWithSidewalk == null) {
	 					waysWithSidewalk = new ArrayList<WayWithSidewalk>();
	 				}
	 				WayWithSidewalk wayWithSidewalk = null;
	 				long wayId = way.getId();
						if (wayJunctionsList.contains(lastOsmNodeId)) {
							wayId = - wayId;
						}
	 				for (WayWithSidewalk wws : waysWithSidewalk) {
						if (wws.osmWayId == wayId) {
							wayWithSidewalk = wws;
						}
					}
	 				if (wayWithSidewalk == null) {
	 					wayWithSidewalk = new WayWithSidewalk(wayId, wayFlags, bearing, this.wayFlags[3], wheelchairAttributes);
	 					// wayWithSidewalk.wayName = way.getTag("name");
	 					waysWithSidewalk.add(wayWithSidewalk);
	 				}
	 				
		 			// ----
		 			// define nodes at junction that can be interconnected
	 				int interpolatedInternalId = getNodeMap().get(interpolatedPoint.getId());
	 				// left in way direction
					if (offset < 0) {
						wayWithSidewalk.rightPreJunctionNodeId = interpolatedInternalId;
						wayWithSidewalk.setWheelchairRightSidewalkAttibutes(leftSidewalkAttributes);
						if (!wayJunctionsList.contains(lastOsmNodeId) && wayWithSidewalk.rightJunctionNodeId != 0 && wayWithSidewalk.rightPreJunctionNodeId != 0) {
	 						wayJunctionsList.add(lastOsmNodeId);
	 					}
						
					}
					// right in way direction
					if (offset > 0) {
						wayWithSidewalk.leftPreJunctionNodeId = interpolatedInternalId;
						wayWithSidewalk.setWheelchairLeftSidewalkAttibutes(rightSidewalkAttributes);
						if (!wayJunctionsList.contains(lastOsmNodeId) && wayWithSidewalk.leftJunctionNodeId != 0 && wayWithSidewalk.leftPreJunctionNodeId != 0) {
	 						wayJunctionsList.add(lastOsmNodeId);
	 					}
					}
					
					sidewalkJunctions.put(lastOsmNodeId, waysWithSidewalk);
	 			}
				
				// handle junction nodes
				if (isNextJunction) {
					float bearing =  (float)Math.toDegrees(Math.atan2((nextLon-currentLon), (nextLat-currentLat))) + 180;
					List<WayWithSidewalk> waysWithSidewalk = null;
					waysWithSidewalk = sidewalkJunctions.get(nextOsmNodeId);
					if (waysWithSidewalk == null) {
	 					waysWithSidewalk = new ArrayList<WayWithSidewalk>();
	 				}
	 				WayWithSidewalk wayWithSidewalk = null;
	 				long wayId = way.getId();
						if (wayJunctionsList.contains(nextOsmNodeId)) {
							wayId = - wayId;
						}
	 				for (WayWithSidewalk wws : waysWithSidewalk) {
						if (wws.osmWayId == wayId) {
							wayWithSidewalk = wws;
						}
					}
	 				if (wayWithSidewalk == null) {
	 					wayWithSidewalk = new WayWithSidewalk(wayId, wayFlags, bearing, this.wayFlags[3], wheelchairAttributes);
	 					// wayWithSidewalk.wayName = way.getTag("name");
	 					waysWithSidewalk.add(wayWithSidewalk);
	 				}

		 			// ----
		 			// define nodes at junction that can be interconnected
	 				int interpolatedInternalId = getNodeMap().get(interpolatedPoint.getId());
	 				// left in way direction
					if (offset < 0) {
						wayWithSidewalk.leftPreJunctionNodeId = interpolatedInternalId;
						wayWithSidewalk.setWheelchairLeftSidewalkAttibutes(leftSidewalkAttributes);
						if (!wayJunctionsList.contains(nextOsmNodeId) && wayWithSidewalk.leftJunctionNodeId != 0 && wayWithSidewalk.leftPreJunctionNodeId != 0) {
	 						wayJunctionsList.add(nextOsmNodeId);
	 					}
					}
					// right in way direction
					if (offset > 0) {
						wayWithSidewalk.rightPreJunctionNodeId = interpolatedInternalId;
						wayWithSidewalk.setWheelchairRightSidewalkAttibutes(rightSidewalkAttributes);
						if (!wayJunctionsList.contains(nextOsmNodeId) && wayWithSidewalk.rightJunctionNodeId != 0 && wayWithSidewalk.rightPreJunctionNodeId != 0) {
	 						wayJunctionsList.add(nextOsmNodeId);
	 					}
					}
					
					sidewalkJunctions.put(nextOsmNodeId, waysWithSidewalk);
				}

			    // System.out.println("ORSOSMReader.addSidewalk(), wayName="+way.getTag("name")+", i="+i+", interpolatedNodes="+interpolatedSidewalkNodes.size()+", interpolated="+getNodeMap().get(interpolatedPoint.getId())+", intOsm="+interpolatedPoint.getId()+", lat="+getTmpLatitude(getNodeMap().get(interpolatedPoint.getId()))+", lon="+getTmpLongitude(getNodeMap().get(interpolatedPoint.getId())));
				
				// transferring the flags of crossing nodes depends on whether the first node is a crossing or not
	 			if (interpolatedSidewalkNodes.size() == 2 && (encodingManager.isCrossing(currentOsmNodeFlags) || isCurrentJunction)) {
	 				isFirstNodeCrossing = true;
	 			}
			}
			
			// -------------------------------		
	        // look for crossings along the interpolated way
			for (TLongList interpolatedSidewalkSectionNodes : interpolatedSidewalkSections) {
				List<EdgeIteratorState> edges = (List<EdgeIteratorState>)addOSMWay(interpolatedSidewalkSectionNodes, wayFlags, wayOsmId);
				// transfer flags of crossing nodes to edges that are connected to these crossing nodes
				if (crossingNodeFlags.size() > 0) {
			    	int c = 1;
			    	int d = 2;
			    	if (isFirstNodeCrossing) {
		    			c-=2;
		    			d-=2;
		    		}
			    	for (int i = 0; i < crossingNodeFlags.size(); i++) {
			    		int j = 3 * i + c;
			    		if (j >= 0 && j < edges.size()) {
			    			long edgeFlags = edges.get(j).getFlags();
			    			// System.out.println("1 ORSOSMReader.addSidewalk(), i="+i+", j="+j+", isCrossing="+encodingManager.isCrossing(kerbNodeFlags.get(i)));
			    			// edgeFlags = encodingManager.setSidewalkSpeed(edgeFlags);
			    			edgeFlags |= crossingNodeFlags.get(i);
			    			edges.get(j).setFlags(edgeFlags);
			    		}
			    		j = 3 * i + d;
			    		if (j >= 0 && j < edges.size()) {
			    			long edgeFlags = edges.get(j).getFlags();
			    			// System.out.println("2 ORSOSMReader.addSidewalk(), i="+i+", j="+j+", isCrossing="+encodingManager.isCrossing(kerbNodeFlags.get(i)));
			    			//edgeFlags = encodingManager.setSidewalkSpeed(edgeFlags);
			    			edgeFlags |= crossingNodeFlags.get(i);
			    			edges.get(j).setFlags(edgeFlags);
			    		}
			    	}
		    	}
				// System.out.println("---- ORSOSMReader.addSidewalk(), wayName="+way.getTag("name")+", interpolatedNodes="+interpolatedSidewalkSectionNodes.size()+", edges="+edges.size()+", crossingNodes="+crossingNodeFlags.size());
		    	createdSidewalkEdges.addAll(edges);	
			}
			return createdSidewalkEdges;
		}
	    
	    @Override 
	    protected void finishedReading() {
	    	handleSidewalkJunctions();
	    	// System.out.println("----------  ORSOSMReader.finishedReading()");
	    	super.finishedReading();
	    }

	    /**
		 * Interpolates additional sidewalk edges at junction depending on the availability of sidewalks and crossing nodes
		 */
		private void handleSidewalkJunctions() {
			
			// handle junctions
			int n = 0;
	    	for (Long junctionOsmNodeId : sidewalkJunctions.keySet()) {
				List<WayWithSidewalk> waysWithSidewalk = sidewalkJunctions.get(junctionOsmNodeId);
				int count = waysWithSidewalk.size();
				// System.out.println("ORSOSMReader.handleSidewalkJunctions(), junctionOsmNodeId="+junctionOsmNodeId+", ways="+count);

				// tower nodes that have at least two way segments that branch of are considered as junctions
				if (count > 1) {
					for (int i = 0; i < waysWithSidewalk.size(); i++) {
						WayWithSidewalk wayWithSidewalkI = waysWithSidewalk.get(i);
						// for each way at the junction, get its counter clockwise neighbor
						WayWithSidewalk wayWithSidewalkCounterClockwiseNeighbour = getCounterClockwiseNeighbourWay(wayWithSidewalkI, waysWithSidewalk);
						
						if (wayWithSidewalkCounterClockwiseNeighbour == null) {
							continue;
						}
						
						// System.out.println("0 ORSOSMReader.finishedReading(), wayWithSidewalkI="+wayWithSidewalkI.wayName+" ("+wayWithSidewalkI.osmWayId+", "+wayWithSidewalkI.bearing+")"+", wayWithSidewalkCounterClockwiseNeighbour="+wayWithSidewalkCounterClockwiseNeighbour.wayName+" ("+wayWithSidewalkCounterClockwiseNeighbour.osmWayId+", "+wayWithSidewalkCounterClockwiseNeighbour.bearing+")");
						// System.out.println("1 ORSOSMReader.finishedReading(), wayWithSidewalkI.hasCloseCrossing="+wayWithSidewalkI.hasCloseCrossing+", wayWithSidewalkCounterClockwiseNeighbour.hasCloseCrossing="+wayWithSidewalkCounterClockwiseNeighbour.hasCloseCrossing);
						// System.out.println("2 ORSOSMReader.finishedReading(), wayI.rightNodeId="+wayWithSidewalkI.rightJunctionNodeId+" ("+getTmpLatitude(wayWithSidewalkI.rightJunctionNodeId)+", "+getTmpLongitude(wayWithSidewalkI.rightJunctionNodeId)+"), wayWithSidewalkI.rightPreNodeId="+wayWithSidewalkI.rightPreJunctionNodeId+" ("+getTmpLatitude(wayWithSidewalkI.rightPreJunctionNodeId)+", "+getTmpLongitude(wayWithSidewalkI.rightPreJunctionNodeId)+"), ccwNeighbour.leftNodeId="+wayWithSidewalkCounterClockwiseNeighbour.leftJunctionNodeId+" ("+getTmpLatitude(wayWithSidewalkCounterClockwiseNeighbour.leftJunctionNodeId)+", "+getTmpLongitude(wayWithSidewalkCounterClockwiseNeighbour.leftJunctionNodeId)+"), ccwNeighbour.leftPreNodeId="+wayWithSidewalkCounterClockwiseNeighbour.leftPreJunctionNodeId+" ("+getTmpLatitude(wayWithSidewalkCounterClockwiseNeighbour.leftPreJunctionNodeId)+", "+getTmpLongitude(wayWithSidewalkCounterClockwiseNeighbour.leftPreJunctionNodeId)+")");
						// System.out.println("ORSOSMReader.handleSidewalkJunctions(), wayWithSidewalkI="+wayWithSidewalkI+", wayWithSidewalkCounterClockwiseNeighbour="+wayWithSidewalkCounterClockwiseNeighbour);
						if (wayWithSidewalkI.rightJunctionNodeId != 0 && wayWithSidewalkI.rightPreJunctionNodeId != 0 && wayWithSidewalkCounterClockwiseNeighbour.leftJunctionNodeId != 0 && wayWithSidewalkCounterClockwiseNeighbour.leftPreJunctionNodeId != 0) {
							
							// the graphhopper internal id of the intersection point
							int intersectionInternalId;
							
							// in cases, where sidewalk nodes are interpolated along a way where another way branches off, no new intersection node needs to be computed
							//
							// o-------x------o
							// OO=====OO======OO
							// o----x || x----o
							//      | || |
							//      | || |
							// 		o OO o
							if (wayWithSidewalkI.rightJunctionNodeId == wayWithSidewalkCounterClockwiseNeighbour.leftJunctionNodeId) {
								intersectionInternalId = wayWithSidewalkI.rightJunctionNodeId;
							}
							else {
								// The following code computes an intersection point of 2 sidewalks at "x" (cf. pseudo graphics below)
								// The intersection point "x" is connected with edges with the previous interpolated sidewalk nodes "o"
								// 
								//      o OO o
								//      | || |
								//      | || |
								//      | || |
								// o----x || x----o
								// OO=====OO======OO
								// o----x || x----o
								//      | || |
								//      | || |
								// 		o OO o
								
								// coordinates of junction sidewalk node of way i
								double junctionNodeILat = getTmpLatitude(wayWithSidewalkI.rightJunctionNodeId);
								double junctionNodeILon = getTmpLongitude(wayWithSidewalkI.rightJunctionNodeId);
								
								// coordinates of pre-junction sidewalk node of way i
								double preJunctionNodeILat = getTmpLatitude(wayWithSidewalkI.rightPreJunctionNodeId);
								double preJunctionNodeILon = getTmpLongitude(wayWithSidewalkI.rightPreJunctionNodeId);
								
								// coordinates of junction sidewalk node of way j
								double junctionNodeJLat = getTmpLatitude(wayWithSidewalkCounterClockwiseNeighbour.leftJunctionNodeId);
								double junctionNodeJLon = getTmpLongitude(wayWithSidewalkCounterClockwiseNeighbour.leftJunctionNodeId);
								
								// coordinates of pre-junction sidewalk node of way j
								double preJunctionNodeJLat = getTmpLatitude(wayWithSidewalkCounterClockwiseNeighbour.leftPreJunctionNodeId);
								double preJunctionNodeJLon = getTmpLongitude(wayWithSidewalkCounterClockwiseNeighbour.leftPreJunctionNodeId);
								
								// the intersection point
								OSMNode intersectionPoint = getLineIntersection(junctionNodeILat, junctionNodeILon, preJunctionNodeILat, preJunctionNodeILon, junctionNodeJLat, junctionNodeJLon, preJunctionNodeJLat, preJunctionNodeJLon);
								
								// prepare interpolated point (graphhopper specific)
							    prepareHighwayNode(intersectionPoint.getId());
						    	// prepare 2nd time to make interpolated point a tower node
							    prepareHighwayNode(intersectionPoint.getId());
							    // add node to graph
								addNode(intersectionPoint);
								intersectionInternalId = getNodeMap().get(intersectionPoint.getId()); 
							}
							
							// add an edge between the pre-junction node way i to the intersection node 
							PointList pillarNodes = new PointList(2, nodeAccess.is3D());
					        pillarNodes.add(getTmpLatitude(wayWithSidewalkI.rightPreJunctionNodeId), getTmpLongitude(wayWithSidewalkI.rightPreJunctionNodeId), getTmpElevation(wayWithSidewalkI.rightPreJunctionNodeId));
					        pillarNodes.add(getTmpLatitude(intersectionInternalId), getTmpLongitude(intersectionInternalId), getTmpElevation(intersectionInternalId));
					        double[] sidewalkIWheelchairAttributes = new double[5];
					        double[] wayWithSidewalkCounterClockwiseNeighbourWheelchairAttributes = new double[5];
					        if (wayWithSidewalkI.wheelchairRightSidewalkAttibutes == null) {
					        	System.arraycopy(wayWithSidewalkI.wheelchairAttributes, 0, sidewalkIWheelchairAttributes, 0, wayWithSidewalkI.wheelchairAttributes.length);
					        }
					        else {
					        	System.arraycopy(wayWithSidewalkI.wheelchairRightSidewalkAttibutes, 0, sidewalkIWheelchairAttributes, 0, wayWithSidewalkI.wheelchairRightSidewalkAttibutes.length);
					        }
					        if (wayWithSidewalkCounterClockwiseNeighbour.wheelchairLeftSidewalkAttibutes == null) {
					        	System.arraycopy(wayWithSidewalkCounterClockwiseNeighbour.wheelchairAttributes, 0, wayWithSidewalkCounterClockwiseNeighbourWheelchairAttributes, 0, wayWithSidewalkCounterClockwiseNeighbour.wheelchairAttributes.length);
					        }
					        else {
					        	System.arraycopy(wayWithSidewalkCounterClockwiseNeighbour.wheelchairLeftSidewalkAttibutes, 0, wayWithSidewalkCounterClockwiseNeighbourWheelchairAttributes, 0, wayWithSidewalkCounterClockwiseNeighbour.wheelchairLeftSidewalkAttibutes.length);
					        }
					        // System.out.println("ORSOSMReader.handleSidewalkJunctions(), wayNameI="+wayWithSidewalkI.wayName+", smoothness(i)="+sidewalkIWheelchairAttributes[WheelchairRestrictionCodes.SMOOTHNESS]);
					        // System.out.println("ORSOSMReader.handleSidewalkJunctions(), wayNameJ="+wayWithSidewalkCounterClockwiseNeighbour.wayName+", smoothness(j)="+wayWithSidewalkCounterClockwiseNeighbourWheelchairAttributes[WheelchairRestrictionCodes.SMOOTHNESS]);
					        EdgeIteratorState newEdge = addEdge(-wayWithSidewalkI.rightPreJunctionNodeId-3, -intersectionInternalId-3, pillarNodes, wayWithSidewalkI.wayFlags, 0);
					        gsWheelchair.setEdgeValue(newEdge.getEdge(), wayWithSidewalkI.wayFeatureType, sidewalkIWheelchairAttributes);
					        
					        // add an edge between the pre-junction node way j to the intersection node
					        pillarNodes = new PointList(2, nodeAccess.is3D());
					        pillarNodes.add(getTmpLatitude(wayWithSidewalkCounterClockwiseNeighbour.leftPreJunctionNodeId), getTmpLongitude(wayWithSidewalkCounterClockwiseNeighbour.leftPreJunctionNodeId), getTmpElevation(wayWithSidewalkCounterClockwiseNeighbour.leftPreJunctionNodeId));
					        pillarNodes.add(getTmpLatitude(intersectionInternalId), getTmpLongitude(intersectionInternalId), getTmpElevation(intersectionInternalId));
					        newEdge = addEdge(-wayWithSidewalkCounterClockwiseNeighbour.leftPreJunctionNodeId-3, -intersectionInternalId-3, pillarNodes, wayWithSidewalkCounterClockwiseNeighbour.wayFlags, 0);
					        gsWheelchair.setEdgeValue(newEdge.getEdge(), wayWithSidewalkCounterClockwiseNeighbour.wayFeatureType, wayWithSidewalkCounterClockwiseNeighbourWheelchairAttributes);
					        
					        // add edge that connects the intersection point with the junction point to allow traversing the way in case there is no dedicated crossing tagged close to the junction
					        // this avoids that massive detours are computed because of (most-likely) missing data
					        // the drawback is that new crossing of ways are inserted into the graph that might not exist in reality.
					        if (!(wayWithSidewalkI.hasCloseCrossing || wayWithSidewalkCounterClockwiseNeighbour.hasCloseCrossing)) {
					        	int internalJunctionId = getNodeMap().get(junctionOsmNodeId);
						        pillarNodes = new PointList(2, nodeAccess.is3D());
						        pillarNodes.add(getTmpLatitude(internalJunctionId), getTmpLongitude(internalJunctionId), getTmpElevation(internalJunctionId));
						        pillarNodes.add(getTmpLatitude(intersectionInternalId), getTmpLongitude(intersectionInternalId), getTmpElevation(intersectionInternalId));
								// intersection flags are the logical intersection of both way flags
								long intersectionFlags = wayWithSidewalkI.wayFlags & wayWithSidewalkCounterClockwiseNeighbour.wayFlags;
						        newEdge = addEdge(-internalJunctionId-3, -intersectionInternalId-3, pillarNodes, intersectionFlags, 0);
						        double[] mergedWheelchairAttributes = mergeWheelchairAttributes(sidewalkIWheelchairAttributes, wayWithSidewalkCounterClockwiseNeighbourWheelchairAttributes);
						        int mergedWayFeatureType = wayWithSidewalkI.wayFeatureType & wayWithSidewalkCounterClockwiseNeighbour.wayFeatureType;
						        gsWheelchair.setEdgeValue(newEdge.getEdge(), mergedWayFeatureType, mergedWheelchairAttributes);
					        }
					        
					     // System.out.println("4 ORSOSMReader.finishedReading(), intersectionId="+intersectionInternalId+" ("+getTmpLatitude(intersectionInternalId)+", "+getTmpLongitude(intersectionInternalId)+")");
					     // System.out.println("5 ORSOSMReader.finishedReading(), wayWithSidewalkI.rightPreNodeId="+wayWithSidewalkI.rightPreJunctionNodeId+" ("+getTmpLatitude(wayWithSidewalkI.rightPreJunctionNodeId)+", "+getTmpLongitude(wayWithSidewalkI.rightPreJunctionNodeId)+"), ccwNeight.leftPreNodeId="+wayWithSidewalkCounterClockwiseNeighbour.leftPreJunctionNodeId+" ("+getTmpLatitude(wayWithSidewalkCounterClockwiseNeighbour.leftJunctionNodeId)+", "+getTmpLongitude(wayWithSidewalkCounterClockwiseNeighbour.leftJunctionNodeId)+")");
						}
					}
				}
				
				// how to deal with dead ends (i.e. only one way at a junction node)?
				// current implementation: allow crossing the street at the end of an dead end
				else if (count == 1) {
					
					WayWithSidewalk wayWithSidewalk = waysWithSidewalk.get(0);
					int junctionNode = getNodeMap().get(junctionOsmNodeId);
					
					// insert 4 new edges into graph
					if (wayWithSidewalk.rightJunctionNodeId != 0 && wayWithSidewalk.rightPreJunctionNodeId != 0 && wayWithSidewalk.leftJunctionNodeId != 0 && wayWithSidewalk.leftPreJunctionNodeId != 0) {

						// add edge from pre junction node to junction sidewalk node (right side of way)
						PointList pillarNodes = new PointList(2, nodeAccess.is3D());
				        pillarNodes.add(getTmpLatitude(wayWithSidewalk.rightPreJunctionNodeId), getTmpLongitude(wayWithSidewalk.rightPreJunctionNodeId), getTmpElevation(wayWithSidewalk.rightPreJunctionNodeId));
				        pillarNodes.add(getTmpLatitude(wayWithSidewalk.rightJunctionNodeId), getTmpLongitude(wayWithSidewalk.rightJunctionNodeId), getTmpElevation(wayWithSidewalk.rightJunctionNodeId));
						EdgeIteratorState newEdge = addEdge(-wayWithSidewalk.rightPreJunctionNodeId-3, -wayWithSidewalk.rightJunctionNodeId-3, pillarNodes, wayWithSidewalk.wayFlags, 0);
						double[] sidewalkWheelchairAttributes = wayWithSidewalk.wheelchairRightSidewalkAttibutes != null ? wayWithSidewalk.wheelchairRightSidewalkAttibutes : wayWithSidewalk.wheelchairAttributes;
						gsWheelchair.setEdgeValue(newEdge.getEdge(), wayWithSidewalk.wayFeatureType, sidewalkWheelchairAttributes);

						// add edge from pre junction node to junction sidewalk node (left side of way)
						pillarNodes = new PointList(2, nodeAccess.is3D());
				        pillarNodes.add(getTmpLatitude(wayWithSidewalk.leftPreJunctionNodeId), getTmpLongitude(wayWithSidewalk.leftPreJunctionNodeId), getTmpElevation(wayWithSidewalk.leftPreJunctionNodeId));
				        pillarNodes.add(getTmpLatitude(wayWithSidewalk.leftJunctionNodeId), getTmpLongitude(wayWithSidewalk.leftJunctionNodeId), getTmpElevation(wayWithSidewalk.leftJunctionNodeId));
				        newEdge = addEdge(-wayWithSidewalk.leftPreJunctionNodeId-3, -wayWithSidewalk.leftJunctionNodeId-3, pillarNodes, wayWithSidewalk.wayFlags, 0);
				        sidewalkWheelchairAttributes = wayWithSidewalk.wheelchairLeftSidewalkAttibutes != null ? wayWithSidewalk.wheelchairLeftSidewalkAttibutes : wayWithSidewalk.wheelchairAttributes;
						gsWheelchair.setEdgeValue(newEdge.getEdge(), wayWithSidewalk.wayFeatureType, sidewalkWheelchairAttributes);
						
						// add edge from junction sidewalk node (right side of way) to junction node to allow crossing the way
						pillarNodes = new PointList(2, nodeAccess.is3D());
				        pillarNodes.add(getTmpLatitude(wayWithSidewalk.rightJunctionNodeId), getTmpLongitude(wayWithSidewalk.rightJunctionNodeId), getTmpElevation(wayWithSidewalk.rightJunctionNodeId));
				        pillarNodes.add(getTmpLatitude(junctionNode), getTmpLongitude(junctionNode), getTmpElevation(junctionNode));
				        newEdge = addEdge(-wayWithSidewalk.rightJunctionNodeId-3, -junctionNode-3, pillarNodes, wayWithSidewalk.wayFlags, 0);
				        sidewalkWheelchairAttributes = wayWithSidewalk.wheelchairRightSidewalkAttibutes != null ? wayWithSidewalk.wheelchairRightSidewalkAttibutes : wayWithSidewalk.wheelchairAttributes;
						gsWheelchair.setEdgeValue(newEdge.getEdge(), wayWithSidewalk.wayFeatureType, sidewalkWheelchairAttributes);
						

						// add edge from junction sidewalk node (left side of way) to junction node to allow crossing the way
						pillarNodes = new PointList(2, nodeAccess.is3D());
				        pillarNodes.add(getTmpLatitude(wayWithSidewalk.leftJunctionNodeId), getTmpLongitude(wayWithSidewalk.leftJunctionNodeId), getTmpElevation(wayWithSidewalk.leftJunctionNodeId));
				        pillarNodes.add(getTmpLatitude(junctionNode), getTmpLongitude(junctionNode), getTmpElevation(junctionNode));
				        newEdge = addEdge(-wayWithSidewalk.leftJunctionNodeId-3, -junctionNode-3, pillarNodes, wayWithSidewalk.wayFlags, 0);
				        sidewalkWheelchairAttributes = wayWithSidewalk.wheelchairLeftSidewalkAttibutes != null ? wayWithSidewalk.wheelchairLeftSidewalkAttibutes : wayWithSidewalk.wheelchairAttributes;
						gsWheelchair.setEdgeValue(newEdge.getEdge(), wayWithSidewalk.wayFeatureType, sidewalkWheelchairAttributes);
						
					}
				}
			}
		}
		
		private PointList createPillarNodes(int id1, int id2)
		{
			PointList nodes = new PointList(2, nodeAccess.is3D());
			if (nodeAccess.is3D())
			{
				nodes.add(getTmpLatitude(id1), getTmpLongitude(id1), getTmpElevation(id1));
				nodes.add(getTmpLatitude(id2), getTmpLongitude(id2), getTmpElevation(id2));
			}
			else
			{
				nodes.add(getTmpLatitude(id1), getTmpLongitude(id1));
				nodes.add(getTmpLatitude(id2), getTmpLongitude(id2));
			}

			return nodes;
		}

		/**
		 * determine the counter clockwise neighbouring way for a way at a junction
		 * e.g. way 2 is the counter clockwise neighbour of way 1
		 * computation is based on the orienation of the ways.
		 *
		 *               2
		 *			     |
		 *			     |
		 *			3 --- --- 1
		 *			     |
		 *			     |
		 *			     4
		 * 
		 * @param wayWithSidewalk
		 * @param waysWithSidewalk
		 * @return the count clockwiserwise neighbouring way of <code>wayWithSidewalk</code>
		 */
		private WayWithSidewalk getCounterClockwiseNeighbourWay(WayWithSidewalk wayWithSidewalk, List<WayWithSidewalk> waysWithSidewalk) {
			float counterClockwiseNeighbourAngle = Float.MAX_VALUE;
			WayWithSidewalk wayWithSidewalkCounterClockwiseNeighbour = null;
			// System.out.println("ORSOSMReader.getCounterClockwiseNeighbourWay(), waysWithSidewalk.size()="+waysWithSidewalk.size());
			for (int j = 0; j < waysWithSidewalk.size(); j++) {
				WayWithSidewalk wayWithSidewalkJ = waysWithSidewalk.get(j);
				float deltaBearing = wayWithSidewalk.bearing - wayWithSidewalkJ.bearing;
				// System.out.println("ORSOSMReader.finishedReading(), wayWithSidewalkJ.wayName="+wayWithSidewalkJ.wayName+" ("+wayWithSidewalkJ.osmWayId+"), wayWithSidewalkI.wayName="+wayWithSidewalkI.wayName+" ("+wayWithSidewalkI.osmWayId+"), bearingI="+wayWithSidewalkI.bearing+", bearingJ="+wayWithSidewalkJ.bearing);
				// System.out.println("ORSOSMReader.finishedReading(), osmWayIdJ=("+wayWithSidewalkJ.osmWayId+"), osmWayId=("+wayWithSidewalk.osmWayId+"), bearingI="+wayWithSidewalk.bearing+", bearingJ="+wayWithSidewalkJ.bearing);
				if (wayWithSidewalkJ.bearing > wayWithSidewalk.bearing) deltaBearing += 360; 
				if (deltaBearing < counterClockwiseNeighbourAngle && deltaBearing != 0) {
					counterClockwiseNeighbourAngle = deltaBearing;
					wayWithSidewalkCounterClockwiseNeighbour = wayWithSidewalkJ;
				}
			}
			// System.out.println("ORSOSMReader.getCounterClockwiseNeighbourWay(), wayName="+wayWithSidewalkCounterClockwiseNeighbour.wayName+", incline="+wayWithSidewalkCounterClockwiseNeighbour.wheelchairAttributes[WheelchairRestrictionCodes.INCLINE]+", wayWithSidewalkCounterClockwiseNeighbour.wheelchairAttributes="+wayWithSidewalkCounterClockwiseNeighbour.wheelchairAttributes);
			return wayWithSidewalkCounterClockwiseNeighbour;
		}
	    

		/**
		 * Returns a new OSMNode (with new Id) that is at the intersection of two 2 lines.<p>
		 * First line is defined by <code>p1Lat</code>, <code>p1Lon</code> and <code>p2Lat</code>, <code>p2Lon</code><p>
		 * Second line is defined by <code>p3Lat</code>, <code>p3Lon</code> and <code>p4Lat</code>, <code>p4Lon</code><p>
		 * 
		 * @param p1Lat
		 * @param p1Lon
		 * @param p2Lat
		 * @param p2Lon
		 * @param p3Lat
		 * @param p3Lon
		 * @param p4Lat
		 * @param p4Lon
		 * @return Returns an OSMNode object if the lines intersect, otherwise null.
		 */
		private OSMNode getLineIntersection(double p1Lat, double p1Lon, double p2Lat, double p2Lon, double p3Lat, double p3Lon, double p4Lat, double p4Lon) {
			double lat;
			double lon;
			
			double deltaLat1 = p2Lat - p1Lat;
			double deltaLon1 = p2Lon - p1Lon;
			double deltaLat2 = p4Lat - p3Lat;
			double deltaLon2 = p4Lon - p3Lon;

			double s;
			double t;
			
			
			// s = (-deltaLon1 * (p1Lat - p3Lat) + deltaLat1 * (p1Lon - p3Lon)) / (-deltaLat2 * deltaLon1 + deltaLat1 * deltaLon2);
			t = (deltaLat2 * (p1Lon - p3Lon) - deltaLon2 * (p1Lat - p3Lat)) / (-deltaLat2 * deltaLon1 + deltaLat1 * deltaLon2);
			
			double denom = (p4Lat - p3Lat) * (p2Lon - p1Lon) - (p4Lon - p3Lon) * (p2Lat - p1Lat);
			
			// additionally check whether line segments (not lines) intersect 
			// if (s >= 0 && s <= 1 && t >= 0 && t <= 1) {
			if (denom != 0) {
				// Intersection point
				lat = p1Lat + (t * deltaLat1);
				lon = p1Lon + (t * deltaLon1);
				if (lon < -180 || lon > 180 || lat < -90 || lat > 90) {
					// This may happen in case of an intersection of 2 lines at a very obtuse angle
					lat = (p1Lat + p3Lat)/2d;
					lon = (p1Lon + p3Lon)/2d;
				}
				return new OSMNode(createNewNodeId(), lat, lon);
			}
			else {
				
				if (p1Lat == p3Lat && p1Lon == p3Lon) {
					return new OSMNode(createNewNodeId(), p1Lat, p1Lon);
				}
				
				// System.out.println("ORSOSMReader.getLineIntersection(), null, p1Lat="+p1Lat+", p1Lon="+p1Lon+", p2Lat="+p2Lat+", p2Lon="+p2Lon+", p3Lat="+p3Lat+", p3Lon="+p3Lon+", p4Lat="+p4Lat+", p4Lon="+p4Lon);
				// Lines are parallel
				// No intersection
				return null;
			}
		}
		
		/**
		 * Computes the midpoint on a line segment and return a new <code>OSMNode</code> with new node id with the coordinates of the midpoint.<p>
		 * The line segment is defined by <code>p1Lat</code>, <code>p1Lon</code>, <code>p2Lat</code>, <code>p2Lon</code>.<p>
		 * 
		 * @param p1Lat
		 * @param p1Lon
		 * @param p2Lat
		 * @param p2Lon
		 * @return the midpoint as a new OSMNode
		 */
		private OSMNode getMidPoint(double p1Lat, double p1Lon, double p2Lat, double p2Lon) {
			return new OSMNode(createNewNodeId(), (p1Lat + p2Lat)/2d, (p1Lon + p2Lon)/2d);
		}
	    
		
	    /**
	     * Helper class to store sidewalk information between 2nd parsing step and finished reading.<p>
	     * In handleSidewalkJunctions() this class is used to interpolate sidewalks at junctions.<p>
	     *
	     */
	    private class WayWithSidewalk {
	    	
	    	long osmWayId = 0;
	        long wayFlags = 0;
	        float bearing = 0;
	        double[] wheelchairAttributes = null;
			double[] wheelchairLeftSidewalkAttibutes = null;
			double[] wheelchairRightSidewalkAttibutes = null;
			int wayFeatureType = 0;
	        // String wayName = null;
			
			/**
	         * newly created sidewalk node (with parallel offset to way) on the left side of the junction (looking towards the junction)
	         */
	        int leftJunctionNodeId = 0;
	        /**
	         * newly created sidewalk node (with parallel offset to way) on the right side of the junction (looking towards the junction)
	         */
	        int rightJunctionNodeId = 0;
	        /**
	         * newly created sidewalk node (with parallel offset to way) on the point before the junction on the left side of the way (looking towards the junction)
	         */
	        int leftPreJunctionNodeId = 0;
	        /**
	         * newly created sidewalk node (with parallel offset to way) on the point before the junction on the right side of the way (looking towards the junction)
	         */
	        int rightPreJunctionNodeId = 0;
	        
	        boolean hasCloseCrossing = false;
	        
	        public WayWithSidewalk(long osmWayId, long wayFlags, float bearing, int wayFeatureType, double[] wheelchairAttributes) {
				this.osmWayId = osmWayId;
				this.wayFlags = wayFlags;
				this.bearing = bearing;
				this.wheelchairAttributes = wheelchairAttributes;
				this.wayFeatureType = wayFeatureType;
				// this.wayName = wayName;
			}
			
			private void setWheelchairLeftSidewalkAttibutes(double[] wheelchairLeftSidewalkAttibutes) {
				if (wheelchairLeftSidewalkAttibutes != null) {
					this.wheelchairLeftSidewalkAttibutes = new double[5];
					System.arraycopy(wheelchairLeftSidewalkAttibutes, 0, this.wheelchairLeftSidewalkAttibutes, 0, 5);
					if(this.wheelchairLeftSidewalkAttibutes[WheelchairRestrictionCodes.SURFACE] == 0) {
						this.wheelchairLeftSidewalkAttibutes[WheelchairRestrictionCodes.SURFACE] = wheelchairAttributes[WheelchairRestrictionCodes.SURFACE];
					}
					if(wheelchairLeftSidewalkAttibutes[WheelchairRestrictionCodes.SMOOTHNESS] == 0) {
						this.wheelchairLeftSidewalkAttibutes[WheelchairRestrictionCodes.SMOOTHNESS] = wheelchairAttributes[WheelchairRestrictionCodes.SMOOTHNESS];
					}
					this.wheelchairLeftSidewalkAttibutes[WheelchairRestrictionCodes.SLOPED_CURB] = wheelchairAttributes[WheelchairRestrictionCodes.SLOPED_CURB];
					this.wheelchairLeftSidewalkAttibutes[WheelchairRestrictionCodes.TRACKTYPE] = wheelchairAttributes[WheelchairRestrictionCodes.TRACKTYPE];
					this.wheelchairLeftSidewalkAttibutes[WheelchairRestrictionCodes.INCLINE] = wheelchairAttributes[WheelchairRestrictionCodes.INCLINE];
					
					// this.wheelchairLeftSidewalkAttibutes = wheelchairLeftSidewalkAttibutes;
					// System.out.println("ORSOSMReader.WayWithSidewalk.setWheelchairLeftSidewalkAttibutes(), wayName="+wayName+", smoothness="+wheelchairLeftSidewalkAttibutes[WheelchairRestrictionCodes.SMOOTHNESS]);
				}
			}

			private void setWheelchairRightSidewalkAttibutes(double[] wheelchairRightSidewalkAttibutes) {
				if (wheelchairRightSidewalkAttibutes != null) {
					this.wheelchairRightSidewalkAttibutes = new double[5];
					System.arraycopy(wheelchairRightSidewalkAttibutes, 0, this.wheelchairRightSidewalkAttibutes, 0, 5);
					if(this.wheelchairRightSidewalkAttibutes[WheelchairRestrictionCodes.SURFACE] == 0) {
						this.wheelchairRightSidewalkAttibutes[WheelchairRestrictionCodes.SURFACE] = wheelchairAttributes[WheelchairRestrictionCodes.SURFACE];
					}
					if(this.wheelchairRightSidewalkAttibutes[WheelchairRestrictionCodes.SMOOTHNESS] == 0) {
						this.wheelchairRightSidewalkAttibutes[WheelchairRestrictionCodes.SMOOTHNESS] = wheelchairAttributes[WheelchairRestrictionCodes.SMOOTHNESS];
					}
					this.wheelchairRightSidewalkAttibutes[WheelchairRestrictionCodes.SLOPED_CURB] = wheelchairAttributes[WheelchairRestrictionCodes.SLOPED_CURB];
					this.wheelchairRightSidewalkAttibutes[WheelchairRestrictionCodes.TRACKTYPE] = wheelchairAttributes[WheelchairRestrictionCodes.TRACKTYPE];
					this.wheelchairRightSidewalkAttibutes[WheelchairRestrictionCodes.INCLINE] = wheelchairAttributes[WheelchairRestrictionCodes.INCLINE];
					// this.wheelchairRightSidewalkAttibutes = wheelchairRightSidewalkAttibutes;
					// System.out.println("ORSOSMReader.WayWithSidewalk.setWheelchairRightSidewalkAttibutes(), wayName="+wayName+", smoothness="+wheelchairRightSidewalkAttibutes[WheelchairRestrictionCodes.SMOOTHNESS]);
				}
			}
	    }
	    
	    private double getIncline(String inclineValue) {
			double v = 0d;
			boolean isDegree = false;
			try {
				inclineValue = inclineValue.replace("%", "");
				inclineValue = inclineValue.replace(",", ".");
				if (inclineValue.contains("°")) {
					inclineValue = inclineValue.replace("°", "");
					isDegree = true;
				}
				// TODO: the following lines are assumptions - can they be validated?
				inclineValue = inclineValue.replace("up", "10");
				inclineValue = inclineValue.replace("down", "10");
				inclineValue = inclineValue.replace("yes", "10");
				inclineValue = inclineValue.replace("steep", "15");
				inclineValue = inclineValue.replace("no", "0");
				inclineValue = inclineValue.replace("+/-0", "0");
				v = Double.parseDouble(inclineValue);
				if (isDegree) {
					v = Math.tan(v) * 100;
				}
			}
			catch (Exception ex) {
				Logger logger = Logger.getLogger(RouteProfileManager.class.getName());
				logger.warning("Error parsing value for Tag incline from this String: " + inclineValue);
			}
			// Fist check if the value makes sense
			// http://wiki.openstreetmap.org/wiki/DE:Key:incline
			// TODO: deal with negative incline (indicates the direction of the incline => might not be important for use wheelchair user as too much incline is an exclusion criterion in both directions?)
			if (-50 < v && v < 50) {
				// value seems to be okay
			}
			else {
				v = 15;
			}
			if (Math.abs(v) > 15) {
				v = 15;
			}
			v = Math.abs(v);
			return v;
		}
	    
	    
	    private double[] getSidewalkAttributes(OSMWay way, String direction) {
	    	
	    	double[] sidewalkAttributes = null;
	    	
	    	boolean hasSidewalkSurface = way.hasTag("sidewalk:surface") || way.hasTag("sidewalk:both:surface") || way.hasTag("sidewalk:"+direction+":surface");
			boolean hasSidewalkSmoothness = way.hasTag("sidewalk:smoothness") || way.hasTag("sidewalk:both:smoothness") || way.hasTag("sidewalk:"+direction+":smoothness");

			if(hasSidewalkSurface || hasSidewalkSmoothness) {
				
				sidewalkAttributes = new double[5];
				sidewalkAttributes[WheelchairRestrictionCodes.SURFACE] = wheelchairAttributes[WheelchairRestrictionCodes.SURFACE];
				sidewalkAttributes[WheelchairRestrictionCodes.SMOOTHNESS] = wheelchairAttributes[WheelchairRestrictionCodes.SMOOTHNESS];
				sidewalkAttributes[WheelchairRestrictionCodes.SLOPED_CURB] = wheelchairAttributes[WheelchairRestrictionCodes.SLOPED_CURB];
				sidewalkAttributes[WheelchairRestrictionCodes.TRACKTYPE] = wheelchairAttributes[WheelchairRestrictionCodes.TRACKTYPE];
				sidewalkAttributes[WheelchairRestrictionCodes.INCLINE] = wheelchairAttributes[WheelchairRestrictionCodes.INCLINE];
				
				if (hasSidewalkSurface) {
					String tagValue = null;
					if (way.hasTag("sidewalk:surface")) {
						tagValue = way.getTag("sidewalk:surface");
					}
					else if (way.hasTag("sidewalk:both:surface")) {
						tagValue = way.getTag("sidewalk:both:surface");
					}
					else if (way.hasTag("sidewalk:"+direction+":surface")) {
						tagValue = way.getTag("sidewalk:"+direction+":surface");
					}
					if (WheelchairRestrictionCodes.SURFACE_MAP.containsKey(tagValue)) {
						// set value
						sidewalkAttributes[WheelchairRestrictionCodes.SURFACE]=WheelchairRestrictionCodes.SURFACE_MAP.get(tagValue);
					}
				}
				if (hasSidewalkSmoothness) {
					String tagValue = null;
					if (way.hasTag("sidewalk:surface")) {
						tagValue = way.getTag("sidewalk:smoothness");
					}
					else if (way.hasTag("sidewalk:both:smoothness")) {
						tagValue = way.getTag("sidewalk:both:smoothness");
					}
					else if (way.hasTag("sidewalk:"+direction+":smoothness")) {
						tagValue = way.getTag("sidewalk:"+direction+":smoothness");
					}
					if (WheelchairRestrictionCodes.SMOOTHNESS_MAP.containsKey(tagValue)) {
						// set value
						sidewalkAttributes[WheelchairRestrictionCodes.SMOOTHNESS]=WheelchairRestrictionCodes.SMOOTHNESS_MAP.get(tagValue);
					}
					// System.out.println("ORSOSMReader.getSidewalkAttributes(), tagValue="+tagValue+", "+"sidewalkAttributes[WheelchairRestrictionCodes.SMOOTHNESS]="+sidewalkAttributes[WheelchairRestrictionCodes.SMOOTHNESS]);
				}
			}
			return sidewalkAttributes;
	    }
	    
	    private double[] mergeWheelchairAttributes(double[] wheelchairAttributes1, double[] wheelchairAttributes2) {
	    	// always decide for the worst case
	    	double[] mergedWheelchairAttributes = new double[5];
	    	
	    	mergedWheelchairAttributes[WheelchairRestrictionCodes.SURFACE] = wheelchairAttributes1[WheelchairRestrictionCodes.SURFACE] < wheelchairAttributes2[WheelchairRestrictionCodes.SURFACE] ? wheelchairAttributes2[WheelchairRestrictionCodes.SURFACE] : wheelchairAttributes1[WheelchairRestrictionCodes.SURFACE];
	    	mergedWheelchairAttributes[WheelchairRestrictionCodes.SMOOTHNESS] = wheelchairAttributes1[WheelchairRestrictionCodes.SMOOTHNESS] < wheelchairAttributes2[WheelchairRestrictionCodes.SMOOTHNESS] ? wheelchairAttributes2[WheelchairRestrictionCodes.SMOOTHNESS] : wheelchairAttributes1[WheelchairRestrictionCodes.SMOOTHNESS];
	    	mergedWheelchairAttributes[WheelchairRestrictionCodes.SLOPED_CURB] = wheelchairAttributes1[WheelchairRestrictionCodes.SLOPED_CURB] < wheelchairAttributes2[WheelchairRestrictionCodes.SLOPED_CURB] ? wheelchairAttributes2[WheelchairRestrictionCodes.SLOPED_CURB] : wheelchairAttributes1[WheelchairRestrictionCodes.SLOPED_CURB];
	    	mergedWheelchairAttributes[WheelchairRestrictionCodes.TRACKTYPE] = wheelchairAttributes1[WheelchairRestrictionCodes.TRACKTYPE] < wheelchairAttributes2[WheelchairRestrictionCodes.TRACKTYPE] ? wheelchairAttributes2[WheelchairRestrictionCodes.TRACKTYPE] : wheelchairAttributes1[WheelchairRestrictionCodes.TRACKTYPE];
	    	mergedWheelchairAttributes[WheelchairRestrictionCodes.INCLINE] = wheelchairAttributes1[WheelchairRestrictionCodes.INCLINE] < wheelchairAttributes2[WheelchairRestrictionCodes.INCLINE] ? wheelchairAttributes2[WheelchairRestrictionCodes.INCLINE] : wheelchairAttributes1[WheelchairRestrictionCodes.INCLINE];
	    	
	    	return mergedWheelchairAttributes;
	    }
}
