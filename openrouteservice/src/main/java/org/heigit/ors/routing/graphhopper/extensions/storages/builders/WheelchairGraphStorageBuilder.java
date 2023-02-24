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
package org.heigit.ors.routing.graphhopper.extensions.storages.builders;

import com.graphhopper.GraphHopper;
import com.graphhopper.reader.ReaderWay;
import com.graphhopper.storage.GraphExtension;
import com.graphhopper.util.EdgeIteratorState;
import org.locationtech.jts.geom.Coordinate;
import org.heigit.ors.routing.graphhopper.extensions.WheelchairAttributes;
import org.heigit.ors.routing.graphhopper.extensions.WheelchairTypesEncoder;
import org.heigit.ors.routing.graphhopper.extensions.storages.WheelchairAttributesGraphStorage;
import org.heigit.ors.util.UnitsConverter;

import java.util.*;

public class WheelchairGraphStorageBuilder extends AbstractGraphStorageBuilder {
	public static final String KEY_SLOPED_CURB = "sloped_curb";
	public static final String KEY_SLOPED_KERB = "sloped_kerb";
	public static final String KEY_KERB_HEIGHT = "kerb:height";
	public static final String KEY_FOOTWAY = "footway";
	public static final String SW_VAL_RIGHT = "right";
	public static final String SW_VAL_LEFT = "left";
	public static final String KEY_BOTH = "both";
	public static final String KEY_SIDEWALK_BOTH = "sidewalk:both:";
	public static final String KEY_FOOTWAY_BOTH = "footway:both:";
	public static final String KEY_CURB_HEIGHT = "curb:height";

	public enum Side {
		LEFT,
		RIGHT,
		NONE
	}

	private WheelchairAttributesGraphStorage storage;
	private final WheelchairAttributes wheelchairAttributes;
	private final WheelchairAttributes wheelchairAttributesLeftSide;
	private final WheelchairAttributes wheelchairAttributesRightSide;

	private Map<Integer, Map<String,String>> nodeTagsOnWay;
	private Map<String, Object> cleanedTags;

	private boolean hasLeftSidewalk = false;
	private boolean hasRightSidewalk = false;
	private boolean kerbHeightOnlyOnCrossing = false;

	public WheelchairGraphStorageBuilder() {
		wheelchairAttributes = new WheelchairAttributes();
		wheelchairAttributesLeftSide = new WheelchairAttributes();
		wheelchairAttributesRightSide = new WheelchairAttributes();
		nodeTagsOnWay = new HashMap<>();
		cleanedTags = new HashMap<>();
	}

	/**
	 * Constructor - Used for testing
	 * @param onlyAttachKerbsToCrossings	Only attach kerb heights to crossings?
	 */
	public WheelchairGraphStorageBuilder(boolean onlyAttachKerbsToCrossings) {
		this();
		kerbHeightOnlyOnCrossing = onlyAttachKerbsToCrossings;
	}

	/**
	 * Initiate the wheelchair storage builder
	 *
	 * @param graphhopper    The graphhopper instance to run against
	 * @return				The storage that is created from the builder
	 * @throws Exception	Thrown when the storage has already been initialized
	 */
	@Override
	public GraphExtension init(GraphHopper graphhopper) throws Exception {
		if (storage != null)
			throw new Exception("GraphStorageBuilder has been already initialized.");

		if(parameters.containsKey("KerbsOnCrossings")) {
			kerbHeightOnlyOnCrossing = Boolean.parseBoolean(parameters.get("KerbsOnCrossings"));
		}
		storage = new WheelchairAttributesGraphStorage();
		return storage;
	}

	/**
	 * Call the processWay method with empty coordinates and tags
	 * @param way	The way to process
	 */
	@Override
	public void processWay(ReaderWay way) {
		this.processWay(way, new Coordinate[0], new HashMap<>());
	}

	/**
	 * Process the way
	 *
	 * @param way		The way to be processed
	 * @param coords	Coordinates of the way
	 * @param nodeTags	Tags that have been stored on nodes of the way that should be used during processing
	 */
	@Override
	public void processWay(ReaderWay way, Coordinate[] coords, Map<Integer, Map<String,String>> nodeTags)
	{
		// Start by resetting storage variables after the previous way
		wheelchairAttributes.reset();
		wheelchairAttributesLeftSide.reset();
		wheelchairAttributesRightSide.reset();
		hasRightSidewalk = false;
		hasLeftSidewalk = false;

		this.nodeTagsOnWay = nodeTags;

		// Annoyingly, it seems often to be the case that rather than using ":" to seperate tag parts, "." is used, so
		// we need to take this into account
		cleanedTags = cleanTags(way.getTags());

		// Now we need to process the way specific to whether it is a separate feature (i.e. footway) or is attached
		// to a road feature (i.e. with the tag sidewalk=left)
		processWayCheckForSeparateFeature(way);

		// We still need to always process the way itself even if it separate so that we can get sidewalk info (a
		// separate footway can still have sidewalk tags...)
		processSidewalksAttachedToWay(way);

		// the way has known suitability if it can be classified as seperate footway
		wheelchairAttributes.setSuitable(isSeparateFootway(way) || way.hasTag("wheelchair_accessible", true));

		// the sidewalks always imply known suitability
		wheelchairAttributesLeftSide.setSuitable(true);
		wheelchairAttributesRightSide.setSuitable(true);

		// Process the kerb tags.
		processKerbTags();
	}

	/**
	 * Return the attributes for the sidewalk on the specified side of the road, or the general attributes when NONE is provided
	 *
	 * @param side	The side of the road you want the data for
	 * @return		The WheelchairAttributes object containing the inofrmation for the specified side
	 */
	public WheelchairAttributes getStoredAttributes(Side side) {
		switch (side) {
			case LEFT:
				return wheelchairAttributesLeftSide;
			case RIGHT:
				return wheelchairAttributesRightSide;
			case NONE:
				return wheelchairAttributes;
			default:
				return null;
		}
	}

	/**
	 * Go through tags and attempt to remove any invalid keys (i.e. when compound keys have been entered using a '.' rather than ':'
	 *
	 * @param dirtyTags		The OSM tag collection that needs to be cleaned
	 *
	 * @return				A cleaned version of the tags on the way (. replaced with : in tag names)
	 */
	private HashMap<String, Object> cleanTags(Map<String, Object> dirtyTags) {
		HashMap<String, Object> cleanedTagsMap = new HashMap<>();
		for(Map.Entry<String, Object> entry : dirtyTags.entrySet()) {
			String cleanKey = entry.getKey().replace(".",":");
			cleanedTagsMap.put(cleanKey, entry.getValue());
		}
		return cleanedTagsMap;
	}

	/**
	 * Process footways that are attached to an OSM way via the sidewalk tags. It looks for parameters important for
	 * wheelchair routing such as width, smoothness and kerb height and then stores these in the attributes object
	 * ready for use when the edge(s) are processed. It also detects which side of the base way that the sidewalks
	 * have been created for and stores the information appropriately.
	 *
	 * @param way		The way to be processed
	 */
	private void processSidewalksAttachedToWay(ReaderWay way) {

		detectAndRecordSidewalkSide(way);

		// get surface type (asphalt, sand etc.)
		setSidewalkAttribute(WheelchairAttributes.Attribute.SURFACE);

		// get smoothness value (good, terrible etc.)
		setSidewalkAttribute(WheelchairAttributes.Attribute.SMOOTHNESS);

		// Get the track type (grade1, grade4 etc.)
		setSidewalkAttribute(WheelchairAttributes.Attribute.TRACK);

		// Get the width of the way (2, 0.1 etc.)
		setSidewalkAttribute(WheelchairAttributes.Attribute.WIDTH);

		// Get the incline of the way (10%, 6% etc.)
		setSidewalkAttribute(WheelchairAttributes.Attribute.INCLINE);

	}

	/**
	 * Process a footway that has been stored in OSM as a separate feature, such as a crossing, footpath or pedestrian
	 * way. The same as the attached processing, it looks for the different attributes as tags that are important for
	 * wheelchair routing and stores them against the generic wheelchair storage object
	 */
	private void processWayCheckForSeparateFeature(ReaderWay way) {
		boolean markSurfaceQualityKnown = isSeparateFootway(way);
		setWayAttribute(WheelchairAttributes.Attribute.SURFACE, markSurfaceQualityKnown);
		setWayAttribute(WheelchairAttributes.Attribute.SMOOTHNESS, markSurfaceQualityKnown);
		setWayAttribute(WheelchairAttributes.Attribute.TRACK, markSurfaceQualityKnown);
		setWayAttribute(WheelchairAttributes.Attribute.WIDTH, markSurfaceQualityKnown);
		setWayAttribute(WheelchairAttributes.Attribute.INCLINE, markSurfaceQualityKnown);
	}

	/**
	 * Set the specified attribute in the attribute storage object based on the information gathered from the way. This
	 * method ony sets the attribute in the attribute storage object for the standalone way and not sidewalks.
	 * @param attribute	The attribute to process
	 * @param markSurfaceQualityKnown Whether or not to also set the surfaceQualityKnown flag in the WheelchairAttributes object
	 */
	private void setWayAttribute(WheelchairAttributes.Attribute attribute, boolean markSurfaceQualityKnown) {
		if (cleanedTags.containsKey(attributeToTagName(attribute))) {
			setWheelchairAttribute((String) cleanedTags.get(attributeToTagName(attribute)), attribute, markSurfaceQualityKnown);
		}
	}

	/**
	 * Set teh specified attribute in the attribute storage objects for the left and right sidewalks,
	 *
	 * @param attribute	The attribute to process
	 */
	private void setSidewalkAttribute(WheelchairAttributes.Attribute attribute) {
		String[] tagValues;

		tagValues = getSidedTagValue(attributeToTagName(attribute));

		if(tagValues[0] != null && !tagValues[0].isEmpty()) {
			setSidewalkAttributeForSide(tagValues[0], attribute, Side.LEFT);
		}
		if(tagValues[1] != null && !tagValues[1].isEmpty()) {
			setSidewalkAttributeForSide(tagValues[1], attribute, Side.RIGHT);
		}
	}

	/**
	 * Detect if there are sidewalks stored on the way and if so, mark that these are present
	 *
	 * @param way	The way to look for sidewalks on
	 */
	private void detectAndRecordSidewalkSide(ReaderWay way) {
		if (way.hasTag("sidewalk")) {
			String sw = way.getTag("sidewalk");
			switch (sw) {
				case SW_VAL_LEFT:
					hasLeftSidewalk = true;
					break;
				case SW_VAL_RIGHT:
					hasRightSidewalk = true;
					break;
				case KEY_BOTH:
					hasLeftSidewalk = true;
					hasRightSidewalk = true;
					break;
				default:
			}
		}
	}

	/**
	 * Convert an attribute from the wheelchair attribute storage to a corresponding osm tag key
	 *
	 * @param attribute	The attribute that the tag key is required for
	 * @return			The OSM tag key that corresponds to the attribute
	 */
	private String attributeToTagName(WheelchairAttributes.Attribute attribute) {
		switch(attribute) {
			case SURFACE: return "surface";
			case SMOOTHNESS: return "smoothness";
			case TRACK: return "tracktype";
			case WIDTH: return "width";
			case INCLINE: return "incline";
			case KERB: return "kerb";
			default: return "";
		}
	}

	/**
	 * Set the specified attribute of the specified sidewalk to be the value passed
	 *
	 * @param value			The value to store
	 * @param attribute		The attribute to store the value against
	 * @param side			The sidewalk the attribute is for
	 */
	private void setSidewalkAttributeForSide(String value, WheelchairAttributes.Attribute attribute, Side side) {
		switch(side) {
			case LEFT:
				hasLeftSidewalk = true;
				wheelchairAttributesLeftSide.setAttribute(attribute, convertTagValueToEncodedValue(attribute, value), true);
				break;
			case RIGHT:
				hasRightSidewalk = true;
				wheelchairAttributesRightSide.setAttribute(attribute, convertTagValueToEncodedValue(attribute, value), true);
				break;
			default:
		}
	}

	/**
	 * Set the specified attribute on the standalone way to be the value passed
	 *
	 * @param value			The value to store
	 * @param attribute		The attribute to store the value against
	 * @param markSurfaceQualityKnown Whether or not to also set the surfaceQualityKnown flag in the WheelchairAttributes object
	 */
	private void setWheelchairAttribute(String value, WheelchairAttributes.Attribute attribute, boolean markSurfaceQualityKnown) {
		wheelchairAttributes.setAttribute(attribute, convertTagValueToEncodedValue(attribute, value), markSurfaceQualityKnown);
	}

	/**
	 * Transform (if needed) a value into an encoded value using the correct encoder.
	 *
	 * @param attribute		The attribute the value is for
	 * @param tagValue			The string value stored in the tag
	 * @return				The correctly encoded value
	 */
	private int convertTagValueToEncodedValue(WheelchairAttributes.Attribute attribute, String tagValue) {
		switch(attribute) {
			case SMOOTHNESS:
			case TRACK:
			case SURFACE: try {
				return WheelchairTypesEncoder.getEncodedType(attribute, tagValue.toLowerCase());
			} catch (Exception notRecognisedEncodedTypeError) {
				return -1;
			}
			case WIDTH:
				return (int)(UnitsConverter.convertOSMDistanceTagToMeters(tagValue.toLowerCase())*100);
			case INCLINE:
				return getInclineFromTagValue(tagValue.toLowerCase());
			case KERB:
				return convertKerbTagValueToCentimetres(tagValue.toLowerCase());
			default:
				return -1;
		}
	}

	/**
	 * Get the kerb height value from a set of tags. The method takes into account different ways of spelling and representing the kerb height and then adds the kerb information
	 * to the wheelchair attribute objects
	 */
	private void processKerbTags() {
		String[] assumedKerbTags = new String[] {
				"curb",
				"kerb",
				KEY_SLOPED_CURB,
				KEY_SLOPED_KERB
		};
		String[] explicitKerbTags = new String[] {
				KEY_KERB_HEIGHT,
				KEY_CURB_HEIGHT
		};

		int height = calcSingleKerbHeightFromTagList(assumedKerbTags, -1);
		// Explicit heights overwrite assumed
		height = calcSingleKerbHeightFromTagList(explicitKerbTags, height);

		if (height > -1) {
			wheelchairAttributes.setSlopedKerbHeight(height);
		}

		// Now for if the values are attached to sides of the way
		int[] heights = calcSingleKerbHeightFromSidedTagList(assumedKerbTags, new int[] { -1, -1});
		heights = calcSingleKerbHeightFromSidedTagList(explicitKerbTags, heights);

		if (heights[0] > -1) {
			hasLeftSidewalk = true;
			wheelchairAttributesLeftSide.setSlopedKerbHeight(heights[0]);
		}

		if (heights[1] > -1) {
			hasLeftSidewalk = true;
			wheelchairAttributesRightSide.setSlopedKerbHeight(heights[1]);
		}
	}

	/**
	 * Calculate the kerb height from the way that should be stored on the graph bsaed on the tag keys specified
	 *
	 * @param kerbTags			The tag keys that should be evaluated
	 * @param initialValue		The initial value for the return. If no kerb height info is found, this value is returned
	 * @return					The value to use as the kerb height derived from the specified tag keys.
	 */
	private int calcSingleKerbHeightFromTagList(String[] kerbTags, int initialValue) {
		int height = initialValue;
		for (String kerbTag : kerbTags) {
			int kerbHeightValue = convertKerbTagValueToCentimetres((String) cleanedTags.get(kerbTag));
			if (kerbHeightValue != -1) {
				height = kerbHeightValue;
			}
		}
		return height;
	}

	/**
	 * Calculate the kerb heights from the way that should be stored on the graph bsaed on the tag keys specified.
	 * This method looks at the tags which specify a side to the road)
	 *
	 * @param kerbTags			The tag keys that should be evaluated
	 * @param initialValues		The initial value for the return. If no kerb height info is found, this value is returned
	 * @return					The values to use as the kerb height derived from the specified tag keys. The first item
	 * 							in the array is for the left side, and the second is the right side.
	 */
	private int[] calcSingleKerbHeightFromSidedTagList(String[] kerbTags, int[] initialValues) {
		int[] heights = initialValues;
		int height = -1;
		for (String kerbTag : kerbTags) {
			String[] tagValues = getSidedKerbTagValuesToApply(kerbTag);
			if(tagValues[0] != null && !tagValues[0].isEmpty()) {
				height = convertKerbTagValueToCentimetres(tagValues[0].toLowerCase());
				if (height > -1) {
					heights[0] = height;
				}
			}
			if(tagValues[1] != null && !tagValues[1].isEmpty()) {
				height = convertKerbTagValueToCentimetres(tagValues[1].toLowerCase());
				if (height > -1) {
					heights[1] = height;
				}
			}
		}

		return heights;
	}

	/**
	 * Look at way and try to find the correct kerb heights for it. In some cases when the kerbs are attached directly to a way they are
	 * marked as start and end and so we need to look through the various tags to try and find these.
	 *
	 * @param key	The base key that we are investigating (e.g. "kerb", "sloped_kerb" etc.)
	 * @return	The textual tag that should be used as the kerb height
	 */
	private String[] getSidedKerbTagValuesToApply(String key) {
		// If we are looking at the kerbs, sometimes the start and end of a way is marked as having different kerb
		// heights using the ...:start and ...:end tags. For now, we just want to get the worse of these values (the
		// highest)
		double leftStart = -1;
		double leftEnd = -1;
		double rightStart = -1;
		double rightEnd = -1;

		String[] endValues = getSidedTagValue(key + ":end");
		// Convert
		if(endValues[0] != null && !endValues[0].isEmpty()) {
			leftEnd = convertKerbTagValueToCentimetres(endValues[0]);
		}
		if(endValues[1] != null && !endValues[1].isEmpty()) {
			rightEnd = convertKerbTagValueToCentimetres(endValues[1]);
		}
		String[] startValues = getSidedTagValue(key + ":start");
		// Convert
		if(startValues[0] != null && !startValues[0].isEmpty()) {
			leftStart = convertKerbTagValueToCentimetres(startValues[0]);
		}
		if(startValues[1] != null && !startValues[1].isEmpty()) {
			rightStart = convertKerbTagValueToCentimetres(startValues[1]);
		}

		// Now compare to find the worst
		String[] values = new String[2];
		if(leftEnd > leftStart )
			values[0] = endValues[0];
		else if(leftStart > leftEnd)
			values[0] = startValues[0];

		if(rightEnd > rightStart)
			values[1] = endValues[1];
		else if(rightStart > rightEnd)
			values[1] = startValues[1];

		return values;
	}

	/**
	 * Compare the attributes gained for the given property between the sidewalks on the left and the right hand side
	 * of the feature and identify which is worse. This is useful if for some reason the sidewalks can not be created
	 * as separate edges from the feature, in which case you would avoid the whole way if an attribute was seen as
	 * impassible.
	 *
	 * @param attr		The attribute to be assessed (surface, smoothness etc.)
	 *
	 * @return			The value that is seen as being the worst
	 */
	private int getWorseAttributeValueFromSeparateItems(WheelchairAttributes.Attribute attr) {
		switch(attr) {
			case SURFACE:
				return Math.max(Math.max(wheelchairAttributesLeftSide.getSurfaceType(), wheelchairAttributesRightSide.getSurfaceType()),
						wheelchairAttributes.getSurfaceType());
			case SMOOTHNESS:
				return Math.max(Math.max(wheelchairAttributesLeftSide.getSmoothnessType(), wheelchairAttributesRightSide.getSmoothnessType()),
						wheelchairAttributes.getSmoothnessType());
			case KERB:
				return Math.max(Math.max(wheelchairAttributesLeftSide.getSlopedKerbHeight(), wheelchairAttributesRightSide.getSlopedKerbHeight()),
						wheelchairAttributes.getSlopedKerbHeight());
			case WIDTH:
				// default value is 0, but this will always be returned so we need to do a check
				int l = wheelchairAttributesLeftSide.getWidth();
				int r = wheelchairAttributesRightSide.getWidth();
				int w = wheelchairAttributes.getWidth();
				if (l <= 0) l = Integer.MAX_VALUE;
				if (r <= 0) r = Integer.MAX_VALUE;
				if (w <= 0) w = Integer.MAX_VALUE;

				int ret = Math.min(Math.min(l,r),w);
				if (ret == Integer.MAX_VALUE) ret = 0;

				return ret;
			case TRACK:
				return Math.max(Math.max(wheelchairAttributesLeftSide.getTrackType(), wheelchairAttributesRightSide.getTrackType()),
						wheelchairAttributes.getTrackType());
			case INCLINE:
				return Math.max(Math.max(wheelchairAttributesLeftSide.getIncline(), wheelchairAttributesRightSide.getIncline()),
						wheelchairAttributes.getIncline());

			default:
				return 0;
		}
	}

	/**
	 * Process an individual edge which has been derived from the way and then store it in the storage.
	 *
	 * @param way		The parent way feature
	 * @param edge		The specific edge to be processed
	 */
	@Override
	public void processEdge(ReaderWay way, EdgeIteratorState edge) {
		// We want to copy so that we don't overwrite original values as this edge is only part of the way
		WheelchairAttributes at = wheelchairAttributes.copy();

		// Get the kerb heights for the individual edge as this may overwrite the original
		int kerbHeight = getKerbHeightForEdge(way);
		if (kerbHeight > -1) {
			at.setSlopedKerbHeight(kerbHeight);
		}

		// Check for if we have specified which side the processing is for
        if(way.hasTag("ors-sidewalk-side")) {
		    String side = way.getTag("ors-sidewalk-side");
		    if(side.equals(SW_VAL_LEFT)) {
				// Only get the attributes for the left side
				at = getAttributes(SW_VAL_LEFT);
				at.setSide(WheelchairAttributes.Side.LEFT);
            }
            if(side.equals(SW_VAL_RIGHT)) {
		    	at = getAttributes(SW_VAL_RIGHT);
		    	at.setSide(WheelchairAttributes.Side.RIGHT);
			}
        } else {
			// if we have sidewalks attached, then we should also look at those. We should only hit this point if
			// the preprocessing hasn't detected that there are sidewalks even though there are...
			if (hasRightSidewalk || hasLeftSidewalk) {
				at = combineAttributesOfWayWhenBothSidesPresent(at);
			}
		}

		storage.setEdgeValues(edge.getEdge(), at);

	}

	/**
	 * Get an overriding kerb height if needed from the nodes that are on the way rather than the data stored on the way itself.
	 * This should be the case if we are specifying to only store kerb heights on crossings as these features do not normally
	 * have kerb heights attached to them
	 *
	 * @param way	The way that is being investigated
	 * @return		A kerb height from the tags of the nodes on the way, or -1 if no kerb heights are found/required
	 */
	int getKerbHeightForEdge(ReaderWay way) {
		int kerbHeight = -1;

		if(!kerbHeightOnlyOnCrossing || (way.hasTag(KEY_FOOTWAY) && way.getTag(KEY_FOOTWAY).equals("crossing"))) {
			// Look for kerb information
			kerbHeight = getKerbHeightFromNodeTags();
		}

		return kerbHeight;
	}

	/**
	 * Look at the information stored against the nodes of the way and extract the kerb height to use for the whole way
	 * from those data.
	 *
	 * @return	The derived kerb height in centimetres from teh nodes that are on the way
	 */
	int getKerbHeightFromNodeTags() {
		// Assumed kerb heights are those obtained from a tag without the explicit :height attribute
		List<Integer> assumedKerbHeights = new ArrayList<>();
		// Explicit heights are those provided by the :height tag - these should take precidence
		List<Integer> explicitKerbHeights = new ArrayList<>();

		for(Map.Entry<Integer, Map<String, String>> entry: nodeTagsOnWay.entrySet()) {
			Map<String, String> tags = entry.getValue();
			for (Map.Entry<String,String> tag : tags.entrySet()) {
				switch (tag.getKey()) {
					case KEY_SLOPED_CURB:
					case "curb":
					case "kerb":
					case KEY_SLOPED_KERB:
						assumedKerbHeights.add(convertKerbTagValueToCentimetres(tag.getValue()));
						break;
					case KEY_KERB_HEIGHT:
						explicitKerbHeights.add(convertKerbTagValueToCentimetres(tag.getValue()));
						break;
					default:
				}
			}
		}
		if (!explicitKerbHeights.isEmpty()) {
			return Collections.max(explicitKerbHeights);
		} else if(!assumedKerbHeights.isEmpty()) {
			// If we have multiple kerb heights, we need to apply the largest to the edge as this is the worst
			return Collections.max(assumedKerbHeights);
		} else {
			return -1;
		}
	}

	/**
	 * When sidewalks are tagged on both sides for a way and we do not want to process them as separate items then we need
	 * to get the "worst" for each attribute and use that.
	 *
	 * @param attributes	The attributes storage object that needs to be merged
	 * @return				A resultant combined object
	 */
	public WheelchairAttributes combineAttributesOfWayWhenBothSidesPresent(WheelchairAttributes attributes) {
		WheelchairAttributes at = attributes;

		int tr = getWorseAttributeValueFromSeparateItems(WheelchairAttributes.Attribute.TRACK);
		if (tr > 0) at.setTrackType(tr);

		int su = getWorseAttributeValueFromSeparateItems(WheelchairAttributes.Attribute.SURFACE);
		if (su > 0) at.setSurfaceType(su);

		int sm = getWorseAttributeValueFromSeparateItems(WheelchairAttributes.Attribute.SMOOTHNESS);
		if (sm > 0) at.setSmoothnessType(sm);

		int sl = getWorseAttributeValueFromSeparateItems(WheelchairAttributes.Attribute.KERB);
		if (sl > 0) at.setSlopedKerbHeight(sl);

		int wi = getWorseAttributeValueFromSeparateItems(WheelchairAttributes.Attribute.WIDTH);
		if (wi > 0) at.setWidth(wi);

		int in = getWorseAttributeValueFromSeparateItems(WheelchairAttributes.Attribute.INCLINE);
		if (in > 0) at.setIncline(in);

		at.setSurfaceQualityKnown(
				wheelchairAttributesLeftSide.isSurfaceQualityKnown()
				&& wheelchairAttributesRightSide.isSurfaceQualityKnown()
				&& attributes.isSurfaceQualityKnown()
		);

		at.setSuitable(
				wheelchairAttributesLeftSide.isSuitable()
						&& wheelchairAttributesRightSide.isSuitable()
						&& attributes.isSuitable()
		);

		return at;
	}

	/**
	 * Get the attributes of a sidewalk on the specified side of the road
	 *
	 * @param side	The side you want the attributes for
	 *
	 * @return		A WheelchairAttributes object for the side requested. If there are no attributes for the specified
	 * 				side, then the overall attributes for the way are returned
	 */
	private WheelchairAttributes getAttributes(String side) {
		WheelchairAttributes at = wheelchairAttributes.copy();

		// Now get the specific items
		switch(side) {
			case SW_VAL_LEFT:
				at = at.merge(wheelchairAttributesLeftSide);
				break;
			case SW_VAL_RIGHT:
				at = at.merge(wheelchairAttributesRightSide);
				break;
			default:
		}
		return at;
	}

	/**
	 * Converts a kerb height value to a numerical height (in centimetres). A kerb could be stored as an explicit height or
	 * as an indicator as to whether the kerb is lowered or not.
	 *
	 * @param value		The value of the tag
	 * @return			The presumed height of the kerb in metres
	 */
	private int convertKerbTagValueToCentimetres(String value) {
		int centimetreHeight = -1;

		if (value == null) {
			return -1;
		}
		switch(value) {
			case "yes":
			case KEY_BOTH:
			case "low":
			case "lowered":
			case "dropped":
			case "sloped":
				centimetreHeight = 3;
				break;
			case "no":
			case "none":
			case "one":
			case "rolled":
			case "regular":
				centimetreHeight = 15;
				break;
			case "at_grade":
			case "flush":
				centimetreHeight = 0;
				break;
			default:
				double metresHeight = UnitsConverter.convertOSMDistanceTagToMeters(value);
				// If no unit was given in the tag, the value might be in meters or centimeters; we can only guess
				// depending on the value
				if (metresHeight < 0.15) {
					centimetreHeight = (int)(metresHeight*100);
				} else {
					centimetreHeight = (int)metresHeight;
				}
				break;
		}

		return centimetreHeight;
	}

	/**
	 * Get the values obtained from a way for a specific sidewalk property. For example, providing the property
	 * "surface" would check the way for the surface tag stored against attached sidewalks using the keys
	 * sidewalk:left:surface, sidewalk:right:surface, and sidewalk:both:surface. The obtained values are then returned
	 * in an array.
	 *
	 * @param property		The property to be extracted
	 *
	 * @return				A String array containing two values - the first is the property for the left sidewalk and
	 * 						the second is the property value for the right sidewalk.
	 */
	private String[] getSidedTagValue(String property) {
		String[] values = new String[2];
		// Left side
		if(cleanedTags.containsKey("sidewalk:left:" + property))
			values[0] = (String) cleanedTags.get("sidewalk:left:" + property);
		else if(cleanedTags.containsKey("footway:left:" +property))
			values[0] = (String) cleanedTags.get("footway:left:" + property);
		// Right side
		if(cleanedTags.containsKey("sidewalk:right:" + property))
			values[1] = (String) cleanedTags.get("sidewalk:right:" + property);
		else if(cleanedTags.containsKey("footway:right:" +property))
			values[1] = (String) cleanedTags.get("footway:right:" + property);

		// Both
		if(cleanedTags.containsKey(KEY_SIDEWALK_BOTH + property)) {
			values[0] = (String) cleanedTags.get(KEY_SIDEWALK_BOTH + property);
			values[1] = (String) cleanedTags.get(KEY_SIDEWALK_BOTH + property);
		}
		else if(cleanedTags.containsKey(KEY_FOOTWAY_BOTH +property)) {
			values[0] = (String) cleanedTags.get(KEY_FOOTWAY_BOTH + property);
			values[1] = (String) cleanedTags.get(KEY_FOOTWAY_BOTH + property);
		}
		return values;
	}

	private int getInclineFromTagValue(String inclineValue) {
		double decimalIncline = UnitsConverter.convertOSMInclineValueToPercentage(inclineValue, true);
		decimalIncline = Math.min(decimalIncline, 15.0);
		return (int) Math.round(decimalIncline);
	}


	/**
	 * Determine if the way is a separate footway object or a road feature.
	 *
	 * @param way		The OSM way object to be assessed
	 * @return			Whether the way is seen as a separately drawn footway (true) or a road (false)
	 */
	private boolean isSeparateFootway(ReaderWay way) {
		String type = way.getTag("highway", "");

		String[] pedestrianWayTypes = {
				"living_street",
				"pedestrian",
				KEY_FOOTWAY,
				"path",
				"crossing",
				"track"
		};

		// Check if it is a footpath or pedestrian
		if(!type.isEmpty()) {
			// We are looking at a separate footpath
			// we are looking at a road feature so any footway would be attached to it as a tag
			return Arrays.asList(pedestrianWayTypes).contains(type);
		}

		return true;
	}

	@Override
	public String getName() {
		return "Wheelchair";
	}

}
