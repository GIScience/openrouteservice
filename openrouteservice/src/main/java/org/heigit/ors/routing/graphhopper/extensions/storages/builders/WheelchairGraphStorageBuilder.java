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
import com.vividsolutions.jts.geom.Coordinate;
import org.heigit.ors.routing.graphhopper.extensions.WheelchairAttributes;
import org.heigit.ors.routing.graphhopper.extensions.WheelchairTypesEncoder;
import org.heigit.ors.routing.graphhopper.extensions.storages.WheelchairAttributesGraphStorage;

import java.util.*;

public class WheelchairGraphStorageBuilder extends AbstractGraphStorageBuilder {
	public static final String KEY_SLOPED_CURB = "sloped_curb";
	public static final String KEY_SLOPED_KERB = "sloped_kerb";
	public static final String KEY_KERB_HEIGHT = "kerb:height";
	public static final String KEY_FOOTWAY = "footway";
	public static final String KEY_INCLINE = "incline";
	public static final String KEY_RIGHT = "right";
	public static final String KEY_SURFACE = "surface";
	public static final String KEY_SMOOTHNESS = "smoothness";
	public static final String KEY_TRACKTYPE = "tracktype";
	public static final String KEY_WIDTH = "width";
	public static final String KEY_SIDEWALK_BOTH = "sidewalk:both:";
	public static final String KEY_FOOTWAY_BOTH = "footway:both:";

	public enum Side {
		LEFT,
		RIGHT
	}

	private WheelchairAttributesGraphStorage storage;
	private WheelchairAttributes wheelchairAttributes;
	private WheelchairAttributes wheelchairAttributesLeftSide;
	private WheelchairAttributes wheelchairAttributesRightSide;

	private HashMap<Integer, HashMap<String,String>> nodeTags;
	private HashMap<String, Object> cleanedTags;

	private boolean hasLeftSidewalk = false;
	private boolean hasRightSidewalk = false;
	private boolean kerbOnCrossing = false;
	private boolean wayEastToWest = false;

	public WheelchairGraphStorageBuilder() {
		wheelchairAttributes = new WheelchairAttributes();
		wheelchairAttributesLeftSide = new WheelchairAttributes();
		wheelchairAttributesRightSide = new WheelchairAttributes();
		nodeTags = new HashMap<>();
		cleanedTags = new HashMap<>();
	}

	/**
	 * Constructor
	 * @param onlyAttachKerbsToCrossings	Only attach kerb heights to crossings?
	 */
	public WheelchairGraphStorageBuilder(boolean onlyAttachKerbsToCrossings) {
		this();
		kerbOnCrossing = onlyAttachKerbsToCrossings;
	}

	private final String[] pedestrianTypes = {
			"living_street",
			"pedestrian",
			KEY_FOOTWAY,
			"path",
			"crossing"
	};

	@Override
	public GraphExtension init(GraphHopper graphhopper) throws Exception {
		if (storage != null)
			throw new Exception("GraphStorageBuilder has been already initialized.");

		if(parameters.containsKey("KerbsOnCrossings")) {
			kerbOnCrossing = Boolean.parseBoolean(parameters.get("KerbsOnCrossings"));
		}
		storage = new WheelchairAttributesGraphStorage();
		return storage;
	}

	@Override
	public void processWay(ReaderWay way) {
		this.processWay(way, new Coordinate[0], new HashMap<>());
	}

	@Override
	public void processWay(ReaderWay way, Coordinate[] coords, HashMap<Integer, HashMap<String,String>> nodeTags)
	{
		// Start by resetting storage variables
		resetAttributes();
		resetSideIndicators();

		this.nodeTags = nodeTags;

		// Annoyingly, it seems often to be the case that rather than using ":" to seperate tag parts, "." is used, so
		// we need to take this into account
		cleanedTags = cleanTags(way.getTags());

		// Now we need to process the way specific to whether it is a separate feature (i.e. footway) or is attached
		// to a road feature (i.e. with the tag sidewalk=left)
		if(isSeparateFootway(way)) {
			// We have a separate footway feature
			processSeparate(way);
		}

		// We still need to always process the way itself even if it separate so that we can get sidewalk info (a
		// separate footway can still have sidewalk tags...)
		processSidewalksAttachedToWay(way);

		// We need to know which direction the way was drawn in so we can determine which side to attach sidewalks to
		// on edges
		wayEastToWest = isWayEastToWest(coords);
	}

	private void resetAttributes() {
		wheelchairAttributes.reset();
		wheelchairAttributesLeftSide.reset();
		wheelchairAttributesRightSide.reset();
	}

	private void resetSideIndicators() {
		hasRightSidewalk = false;
		hasLeftSidewalk = false;

	}

	public WheelchairAttributes getStoredAttributes(Side side) {
		if(side == null) {
			return wheelchairAttributes;
		} else {
			switch (side) {
				case LEFT:
					return wheelchairAttributesLeftSide;
				case RIGHT:
					return wheelchairAttributesRightSide;
				default:
					return null;
			}
		}
	}

	/**
	 * Go through tags and attempt to remove any invalid keys (i.e. when compound keys have been entered using a '.' rather than ':'
	 *
	 * @param dirtyTags		The OSM tag collection that needs to be cleaned
	 *
	 * @return
	 */
	private HashMap<String, Object> cleanTags(Map<String, Object> dirtyTags) {
		HashMap<String, Object> cleanedTagsMap = new HashMap<>();
		for(Map.Entry<String, Object> entry : dirtyTags.entrySet()) {
			String cleanKey = replacePointWithColon(entry.getKey());
			cleanedTagsMap.put(cleanKey, entry.getValue());
		}
		return cleanedTagsMap;
	}

	private String replacePointWithColon(String textToReplace) {
		return textToReplace.replace(".",":");
	}

	/**
	 * Determine if the way has been drawn as going from east to west.
	 *
	 * @param coordinatesOfWay
	 * @return
	 */
	private boolean isWayEastToWest(Coordinate[] coordinatesOfWay) {
		return coordinatesOfWay.length > 1 && coordinatesOfWay[0].x < coordinatesOfWay[1].x;
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
		setTagValuesForAttribute(WheelchairAttributes.Attribute.SURFACE);

		// get smoothness value (good, terrible etc.)
		setTagValuesForAttribute(WheelchairAttributes.Attribute.SMOOTHNESS);

		// Get the track type (grade1, grade4 etc.)
		setTagValuesForAttribute(WheelchairAttributes.Attribute.TRACK);

		// Get the width of the way (2, 0.1 etc.)
		setTagValuesForAttribute(WheelchairAttributes.Attribute.WIDTH);

		// Get the incline of the way (10%, 6% etc.)
		if (way.hasTag(KEY_INCLINE)) {
			int incline = getInclineValueFromWay(way);
			if (incline != -1) {
				wheelchairAttributesLeftSide.setIncline(incline);
				wheelchairAttributesRightSide.setIncline(incline);
			}
		}

		setTagValuesForAttribute(WheelchairAttributes.Attribute.INCLINE);

		// Assess any kerb height attached directly to the way
		processKerbTags(way);
	}

	private void detectAndRecordSidewalkSide(ReaderWay way) {
		if (way.hasTag("sidewalk")) {
			String sw = way.getTag("sidewalk");
			switch (sw) {
				case "left":
					hasLeftSidewalk = true;
					break;
				case KEY_RIGHT:
					hasRightSidewalk = true;
					break;
				case "both":
					hasLeftSidewalk = true;
					hasRightSidewalk = true;
					break;
				default:
			}
		}
	}

	private void setTagValuesForAttribute(WheelchairAttributes.Attribute attribute) {
		String[] tagValues;

		tagValues = getConcatenatedTagValue(attributeToTagName(attribute));

		if(tagValues[0] != null && !tagValues[0].isEmpty()) {
			setSidewalkAttributeForSide(tagValues[0], attribute, Side.LEFT);
		}
		if(tagValues[1] != null && !tagValues[1].isEmpty()) {
			setSidewalkAttributeForSide(tagValues[1], attribute, Side.RIGHT);
		}
	}

	private String attributeToTagName(WheelchairAttributes.Attribute attribute) {
		switch(attribute) {
			case SURFACE: return KEY_SURFACE;
			case SMOOTHNESS: return KEY_SMOOTHNESS;
			case TRACK: return KEY_TRACKTYPE;
			case WIDTH: return KEY_WIDTH;
			case INCLINE: return KEY_INCLINE;
			case KERB: return "kerb";
			default: return "";
		}
	}

	private void setSidewalkAttributeForSide(String value, WheelchairAttributes.Attribute attribute, Side side) {
		switch(side) {
			case LEFT:
				hasLeftSidewalk = true;
				wheelchairAttributesLeftSide.setAttribute(attribute, getEncodedAttributeValueAsString(attribute, value));
				break;
			case RIGHT:
				hasRightSidewalk = true;
				wheelchairAttributesRightSide.setAttribute(attribute, getEncodedAttributeValueAsString(attribute, value));
				break;
			default:
		}
	}

	/**
	 * Transform (if needed) a value into an encoded value using the correct encoder.
	 *
	 * @param attribute		The attribute the value is for
	 * @param value			The string value stored in the tag
	 * @return				The correctly encoded value
	 */
	private String getEncodedAttributeValueAsString(WheelchairAttributes.Attribute attribute, String value) {
		switch(attribute) {
			case SMOOTHNESS:
			case TRACK:
			case SURFACE: try {
				return Integer.toString(WheelchairTypesEncoder.getEncodedType(attribute, value.toLowerCase()));
			} catch (Exception notRecognisedEncodedTypeError) {
				return "";
			}
			case WIDTH:
				return Integer.toString((int)(convertLinearValueToMetres(value.toLowerCase())*100));
			case INCLINE:
				return Integer.toString(convertInclineValueToPercentage(value.toLowerCase()));
			case KERB:
				return value;
			default:
				return "";
		}
	}

	/**
	 * Get the kerb height value from a set of tags. The method takes into account different ways of spelling and representing the kerb height and then adds the kerb information
	 * to the wheelchair attribute objects
	 *
	 * @param way
	 */
	private void processKerbTags(ReaderWay way) {
		int height = -1;

		if(way.hasTag("curb")) {
			height = convertKerbValueToHeight("curb", way.getTag("curb"));
		}
		if(way.hasTag("kerb")) {
			height = convertKerbValueToHeight("kerb", way.getTag("kerb"));
		}
		if(way.hasTag(KEY_SLOPED_CURB)) {
			height = convertKerbValueToHeight(KEY_SLOPED_CURB, way.getTag(KEY_SLOPED_CURB));
		}
		if(way.hasTag(KEY_SLOPED_KERB)) {
			height = convertKerbValueToHeight(KEY_SLOPED_KERB, way.getTag(KEY_SLOPED_KERB));
		}
		if(way.hasTag(KEY_KERB_HEIGHT)) {
			height = convertKerbValueToHeight(KEY_KERB_HEIGHT, way.getTag(KEY_KERB_HEIGHT));
		}

		if(height > -1) {
			setSidewalkAttributeForSide(Integer.toString(height), WheelchairAttributes.Attribute.KERB, Side.LEFT);
			setSidewalkAttributeForSide(Integer.toString(height), WheelchairAttributes.Attribute.KERB, Side.RIGHT);
		}

		// Also check if they have been marked for specific sides
		String[] tagValues = getCompoundKerb("curb");
		if(tagValues[0] != null && !tagValues[0].isEmpty()) {
			hasLeftSidewalk = true;
			wheelchairAttributesLeftSide.setSlopedKerbHeight(convertKerbValueToHeight("curb", tagValues[0].toLowerCase()));
		}
		if(tagValues[1] != null && !tagValues[1].isEmpty()) {
			hasRightSidewalk = true;
			wheelchairAttributesRightSide.setSlopedKerbHeight(convertKerbValueToHeight("curb", tagValues[1].toLowerCase()));
		}
		tagValues = getCompoundKerb("kerb");
		if(tagValues[0] != null && !tagValues[0].isEmpty()) {
			hasLeftSidewalk = true;
			wheelchairAttributesLeftSide.setSlopedKerbHeight(convertKerbValueToHeight("kerb", tagValues[0].toLowerCase()));
		}
		if(tagValues[1] != null && !tagValues[1].isEmpty()) {
			hasRightSidewalk = true;
			wheelchairAttributesRightSide.setSlopedKerbHeight(convertKerbValueToHeight("kerb", tagValues[1].toLowerCase()));
		}
		tagValues = getCompoundKerb(KEY_SLOPED_CURB);
		if(tagValues[0] != null && !tagValues[0].isEmpty()) {
			hasLeftSidewalk = true;
			wheelchairAttributesLeftSide.setSlopedKerbHeight(convertKerbValueToHeight(KEY_SLOPED_CURB, tagValues[0].toLowerCase()));
		}
		if(tagValues[1] != null && !tagValues[1].isEmpty()) {
			hasRightSidewalk = true;
			wheelchairAttributesRightSide.setSlopedKerbHeight(convertKerbValueToHeight(KEY_SLOPED_CURB, tagValues[1].toLowerCase()));
		}
		tagValues = getCompoundKerb(KEY_KERB_HEIGHT);
		if(tagValues[0] != null && !tagValues[0].isEmpty()) {
			hasLeftSidewalk = true;
			wheelchairAttributesLeftSide.setSlopedKerbHeight(convertKerbValueToHeight(KEY_KERB_HEIGHT, tagValues[0].toLowerCase()));
		}
		if(tagValues[1] != null && !tagValues[1].isEmpty()) {
			hasRightSidewalk = true;
			wheelchairAttributesRightSide.setSlopedKerbHeight(convertKerbValueToHeight(KEY_KERB_HEIGHT, tagValues[1].toLowerCase()));
		}
	}

	/**
	 * Look at way and try to find the correct kerb heights for it. In some cases when the kerbs are attached directly to a way they are
	 * marked as start and end and so we need to look through the various tags to try and find these.
	 *
	 * @param key
	 * @return
	 */
	private String[] getCompoundKerb(String key) {
		// If we are looking at the kerbs, sometimes the start and end of a way is marked as having different kerb
		// heights using the ...:start and ...:end tags. For now, we just want to get the worse of these values (the
		// highest)
		double leftStart = -1;
		double leftEnd = -1;
		double rightStart = -1;
		double rightEnd = -1;
		double leftNorm = -1;
		double rightNorm = -1;

		String[] endValues = getConcatenatedTagValue(key + ":end");
		// Convert
		if(endValues[0] != null && !endValues[0].isEmpty()) {
			leftEnd = convertKerbValueToHeight(key, endValues[0]);
		}
		if(endValues[1] != null && !endValues[1].isEmpty()) {
			rightEnd = convertKerbValueToHeight(key, endValues[1]);
		}
		String[] startValues = getConcatenatedTagValue(key + ":start");
		// Convert
		if(startValues[0] != null && !startValues[0].isEmpty()) {
			leftStart = convertKerbValueToHeight(key, startValues[0]);
		}
		if(startValues[1] != null && !startValues[1].isEmpty()) {
			rightStart = convertKerbValueToHeight(key, startValues[1]);
		}

		String[] normValues = getConcatenatedTagValue(key);
		// Convert
		if(normValues[0] != null && !normValues[0].isEmpty()) {
			leftNorm = convertKerbValueToHeight(key, normValues[0]);
		}
		if(normValues[1] != null && !normValues[1].isEmpty()) {
			rightNorm = convertKerbValueToHeight(key, normValues[1]);
		}

		// Now compare to find the worst
		String[] values = new String[2];
		if(leftEnd > leftStart && leftEnd > leftNorm)
			values[0] = endValues[0];
		else if(leftStart > leftEnd && leftStart > leftNorm)
			values[0] = startValues[0];
		else
			values[0] = normValues[0];

		if(rightEnd > rightStart && rightEnd > rightNorm)
			values[1] = endValues[1];
		else if(rightStart > rightEnd && rightStart > rightNorm)
			values[1] = startValues[1];
		else
			values[1] = normValues[1];

		return values;
	}

	/**
	 * Process a footway that has been stored in OSM as a separate feature, such as a crossing, footpath or pedestrian
	 * way. The same as the attached processing, it looks for the different attributes as tags that are important for
	 * wheelchair routing and stores them against the generic wheechair storage object
	 * @param way
	 */
	// TODO: This is ugly as hell, processSidewalksAttachedToWay() does basically the same thing but in a completely 
	//  different way. Desperately needs refactoring!
	private void processSeparate(ReaderWay way) {

		if (way.hasTag(KEY_SURFACE))
		{
			int value = WheelchairTypesEncoder.getSurfaceType(way.getTag(KEY_SURFACE).toLowerCase());
			if (value > 0)
				wheelchairAttributes.setSurfaceType(value);
		}

		if (way.hasTag(KEY_SMOOTHNESS))
		{
			int value = WheelchairTypesEncoder.getSmoothnessType(way.getTag(KEY_SMOOTHNESS).toLowerCase());
			if (value > 0)
				wheelchairAttributes.setSmoothnessType(value);
		}

		if (way.hasTag(KEY_TRACKTYPE)) {
			int value = WheelchairTypesEncoder.getTrackType(way.getTag(KEY_TRACKTYPE).toLowerCase());
			if (value > 0)
				wheelchairAttributes.setTrackType(value);
		}

		if(way.hasTag(KEY_WIDTH)) {
			double width = convertLinearValueToMetres(way.getTag(KEY_WIDTH));
			if (width >= 0)
				wheelchairAttributes.setWidth((int) (width*100));
		}

		// kerb height is only valid on separated ways
		if(way.hasTag("curb"))
			wheelchairAttributes.setSlopedKerbHeight(convertKerbValueToHeight("curb", way.getTag("curb")));
		if(way.hasTag("kerb"))
			wheelchairAttributes.setSlopedKerbHeight(convertKerbValueToHeight("kerb", way.getTag("kerb")));
		if(way.hasTag(KEY_SLOPED_CURB))
			wheelchairAttributes.setSlopedKerbHeight(convertKerbValueToHeight(KEY_SLOPED_CURB, way.getTag(KEY_SLOPED_CURB)));
		if(way.hasTag(KEY_SLOPED_KERB))
			wheelchairAttributes.setSlopedKerbHeight(convertKerbValueToHeight(KEY_SLOPED_KERB, way.getTag(KEY_SLOPED_KERB)));
		if(way.hasTag(KEY_KERB_HEIGHT))
			wheelchairAttributes.setSlopedKerbHeight(convertKerbValueToHeight(KEY_KERB_HEIGHT, way.getTag(KEY_KERB_HEIGHT)));

		// incline
		// =======
		// http://wiki.openstreetmap.org/wiki/Key:incline
		// http://wiki.openstreetmap.org/wiki/Wheelchair_routing#Path_properties.2C_in_general
		// http://wiki.openstreetmap.org/wiki/DE:Wheelchair_routing#Weg_Eigenschaften_allgemein
		if (way.hasTag(KEY_INCLINE))
		{
			int incline = getInclineValueFromWay(way);
			if (incline != 0.0)
			{
				wheelchairAttributes.setIncline(incline);
			}
		}
	}

	/**
	 * Compare the attributes gained for the given property between the sidewalks on the left and the right hand side
	 * of the feature and identify which is worse. This is useful if for some reason the sidewalks can not be created
	 * as separate edges from the feature, in which case you would avoid the whole way if an attribute was seen as
	 * impassible.
	 *
	 * @param attr		The attribute to be assessed (surface, smoothness etc.)
	 * @param type		The object type (i.e. Integer, Float)
	 * @param <T>		The return type
	 *
	 * @return			The value that is seen as being the worst
	 */
	private <T> T getWorse(WheelchairAttributes.Attribute attr, Class<T> type) {
		switch(attr) {
			case SURFACE:
				return type.cast(Math.max(Math.max(wheelchairAttributesLeftSide.getSurfaceType(), wheelchairAttributesRightSide.getSurfaceType()),
						wheelchairAttributes.getSurfaceType()));
			case SMOOTHNESS:
				return type.cast(Math.max(Math.max(wheelchairAttributesLeftSide.getSmoothnessType(), wheelchairAttributesRightSide.getSmoothnessType()),
						wheelchairAttributes.getSmoothnessType()));
			case KERB:
				return type.cast(Math.max(Math.max(wheelchairAttributesLeftSide.getSlopedKerbHeight(), wheelchairAttributesRightSide.getSlopedKerbHeight()),
						wheelchairAttributes.getSlopedKerbHeight()));
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

				return type.cast(ret);
			case TRACK:
				return type.cast(Math.max(Math.max(wheelchairAttributesLeftSide.getTrackType(), wheelchairAttributesRightSide.getTrackType()),
						wheelchairAttributes.getTrackType()));
			case INCLINE:
				return type.cast(Math.max(Math.max(wheelchairAttributesLeftSide.getIncline(), wheelchairAttributesRightSide.getIncline()),
						wheelchairAttributes.getIncline()));

			default:
				return type.cast(0);
		}
	}

	@Override
	public void processEdge(ReaderWay way, EdgeIteratorState edge) {
		// do nothing
	}

	@Override
	public void processEdge(ReaderWay way, EdgeIteratorState edge, Coordinate[] coords) {
		WheelchairAttributes at = wheelchairAttributes.copy();

		// If we are only dealing with kerbs on crossings, then we need ot check that the way is a crossing, else work
		// with all ways
        // This is only applicable if the kerb height is stored on a node rather than on the way itself. If that is the
        // case, then the kerb height has already been stored in the attributes.

		int kerbHeight = getKerbHeightForWay(way, edge);
		if(kerbHeight > -1) {
			at.setSlopedKerbHeight(kerbHeight);
		}

		// Look at which way the edge goes
		boolean eastToWest = false;
		if(coords.length > 1) {
			eastToWest = coords[0].x <= coords[coords.length-1].x;
		}

		// Check for if we have specified which side the processing is for
        if(way.hasTag("ors-sidewalk-side")) {


		    String side = way.getTag("ors-sidewalk-side");
		    if(side.equals("left")) {
				// Only get the attributes for the left side
				at = getAttributes("left");
				// Check which direction we are travelling in
				// if we are travelling the same direction as the way, then use the same as marked, else the opposite
				if(eastToWest == wayEastToWest)
					at.setSide(WheelchairAttributes.Side.LEFT);
				else
					at.setSide(WheelchairAttributes.Side.RIGHT);
            }
            if(side.equals(KEY_RIGHT)) {
		    	at = getAttributes(KEY_RIGHT);
				// Check which direction we are travelling in
				// if we are travelling the same direction as the way, then use the same as marked, else the opposite
				if(eastToWest == wayEastToWest)
					at.setSide(WheelchairAttributes.Side.RIGHT);
				else
					at.setSide(WheelchairAttributes.Side.LEFT);
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

	int getKerbHeightForWay(ReaderWay way, EdgeIteratorState edge) {
		int kerbHeight = -1;

		if((kerbOnCrossing && way.hasTag(KEY_FOOTWAY) && way.getTag(KEY_FOOTWAY).equals("crossing"))
				|| !kerbOnCrossing) {
			// Look for kerb information
			kerbHeight = getKerbHeightForWayFromNodeTags(edge.getBaseNode(), edge.getAdjNode());
		}

		return kerbHeight;
	}

	/**
	 * Look through the way tags stored for this instance and find the corresponding kerb heights for the start and end
	 * points.
	 *
	 * @param startNodeId
	 * @param endNodeId
	 * @return
	 */
	int getKerbHeightForWayFromNodeTags(int startNodeId, int endNodeId) {
		List<Integer> kerbHeights = new ArrayList<>();
		for(Map.Entry<Integer, HashMap<String, String>> entry: nodeTags.entrySet()) {
			// We only want to add the kerb height to the edge that is actually connected to it
			if(entry.getKey() == endNodeId || entry.getKey() == startNodeId) {
				HashMap<String, String> tags = entry.getValue();
				for (Map.Entry<String,String> tag : tags.entrySet()) {
					switch (tag.getKey()) {
						case KEY_SLOPED_CURB:
						case "curb":
						case "kerb":
						case KEY_SLOPED_KERB:
						case KEY_KERB_HEIGHT:
							kerbHeights.add(convertKerbValueToHeight(tag.getKey(), tag.getValue()));
							break;
						default:
					}
				}
			}
		}
		if(!kerbHeights.isEmpty()) {
			// If we have multiple kerb heights, we need to apply the largest to the edge as this is the worst
			if(kerbHeights.size() > 1) {
				// TODO: performance -- why is this sorted instead of computing max?
				java.util.Collections.sort(kerbHeights, (v1, v2) -> (v1 < v2) ? 1 : -1);
			}
			return kerbHeights.get(0);
		} else {
			return -1;
		}
	}

	/**
	 * When sidewalks are tagged on both sides for a way and we do not want to process them as separate items then we need
	 * to get the "worst" for each attribute and use that.
	 *
	 * @param attributes
	 * @return
	 */
	public WheelchairAttributes combineAttributesOfWayWhenBothSidesPresent(WheelchairAttributes attributes) {
		WheelchairAttributes at = attributes;

		int tr = getWorse(WheelchairAttributes.Attribute.TRACK, Integer.class);
		if (tr > 0) at.setTrackType(tr);

		int su = getWorse(WheelchairAttributes.Attribute.SURFACE, Integer.class);
		if (su > 0) at.setSurfaceType(su);

		int sm = getWorse(WheelchairAttributes.Attribute.SMOOTHNESS, Integer.class);
		if (sm > 0) at.setSmoothnessType(sm);

		int sl = getWorse(WheelchairAttributes.Attribute.KERB, Integer.class);
		if (sl > 0) at.setSlopedKerbHeight(sl);

		int wi = getWorse(WheelchairAttributes.Attribute.WIDTH, Integer.class);
		if (wi > 0) at.setWidth(wi);

		int in = getWorse(WheelchairAttributes.Attribute.INCLINE, Integer.class);
		if (in > 0) at.setIncline(in);

		return at;
	}

	/**
	 * Get the attibutes of a sidewalk on the specified side of the road
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
			case "left":
				at = at.merge(wheelchairAttributesLeftSide);
				break;
			case KEY_RIGHT:
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
	 * @param tag		The key (tag) that was obtained describing the kerb information
	 * @param value		The value of the tag
	 * @return			The presumed height of the kerb in metres
	 */
	private int convertKerbValueToHeight(String tag, String value) {
		int height = -1;

		if(tag.equals(KEY_SLOPED_CURB) || tag.equals("sloped_kurb") || tag.equals("curb") || tag.equals("kerb")) {
			// THE TAGS sloped_curb AND curb SHOULD NOT BE USED
			// also, many of the values are not recognised as proper OSM tags, but they still exist
			switch(value) {
				case "yes":
				case "both":
				case "low":
				case "lowered":
				case "dropped":
				case "sloped":
					height = 3;
					break;
				case "no":
				case "none":
				case "one":
				case "rolled":
				case "regular":
					height = 15;
					break;
				case "at_grade":
				case "flush":
					height = 0;
					break;
				default:
					// May be that it is already numeric (though it shouldn't be)
					double metresHeight = convertLinearValueToMetres(value);
					if (metresHeight >= 0) {
						height = (int) (metresHeight*100);
					}
					break;
			}
		}
		if(tag.equals(KEY_KERB_HEIGHT)) {
			// we need to also check for the measurement unit
			// we can use the same unit conversion as width
			double metresHeight = convertLinearValueToMetres(value);
			if (metresHeight >= 0) {
				height = (int) (metresHeight*100);
			}
		}

		return height;
	}

	/**
	 * Get the values obtained from a way for a specific sidewalk property. Fotr example, providing the property
	 * "surface" would check the way for the surface tag stored against attached sidewalks using the keys
	 * sidewalk:left:surface, sidewalk:right:surface, and sidewalk:both:surface. The obtained values are then returned
	 * in an array.
	 *
	 * @param property		The property to be extracted
	 *
	 * @return				A String array containing two values - the first is the property for the left sidewalk and
	 * 						the second is the property value for the right sidewalk.
	 */
	private String[] getConcatenatedTagValue(String property) {
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

	private int getInclineValueFromWay(ReaderWay way)
	{
		String inclineValue = way.getTag(KEY_INCLINE);
		return convertInclineValueToPercentage(inclineValue);
	}

	/**
	 * Convert the String representation of an incline into a %age incline value. in OSM the tag value could already
	 * be a %age value, or it could be written as "up", "down", "steep" etc. in which case an incline value is assumed
	 *
	 * @param unprocessedInclineValue		The value obtained from the incline tag
	 * @return					a percentage incline value
	 */
	private int convertInclineValueToPercentage(String unprocessedInclineValue) {

		if (unprocessedInclineValue != null)
		{
			double v = 0d;
			boolean isDegree = false;
			try {
				unprocessedInclineValue = unprocessedInclineValue.replace("%", "");
				unprocessedInclineValue = unprocessedInclineValue.replace(",", ".");
				if (unprocessedInclineValue.contains("°")) {
					unprocessedInclineValue = unprocessedInclineValue.replace("°", "");
					isDegree = true;
				}

				// Replace textual descriptions with assumed values
				unprocessedInclineValue = unprocessedInclineValue.replace("up", "10");
				unprocessedInclineValue = unprocessedInclineValue.replace("down", "10");
				unprocessedInclineValue = unprocessedInclineValue.replace("yes", "10");
				unprocessedInclineValue = unprocessedInclineValue.replace("steep", "15");
				unprocessedInclineValue = unprocessedInclineValue.replace("no", "0");
				unprocessedInclineValue = unprocessedInclineValue.replace("+/-0", "0");
				v = Double.parseDouble(unprocessedInclineValue);
				if (isDegree) {
					v = Math.tan(v) * 100;
				}
			} catch (Exception ex) {
				// do nothing
			}

			// If the value seems too extreme, then we should limit
			if (Math.abs(v) > 15) {
				v = 15;
			}

			if(v < 0) {
			    v = v * -1.0;
            }

			return (int) Math.round(v);
		}

		return -1;
	}

	/**
	 * Convert a OSM width value to a decimal value in metres. In osm the width could be stored in many different units
	 * and so this method attempts to convert them all to metres.
	 *
	 * @param unprocessedLinearValue		The obtained width tag value
	 * @return				The width value converted to metres
	 */
	private double convertLinearValueToMetres(String unprocessedLinearValue) {
		double processedLinearValue = -1d;

		// Valid values are:
		/*
		processedLinearValue=x (default metres)
		processedLinearValue=x m		(metre)
		processedLinearValue=x km		(kilometre)
		processedLinearValue=x mi		(mile)
		processedLinearValue=x nmi		(nautical mile)
		processedLinearValue=x'y"		(feet and inches)

		However, many people omit the space, even though they shouldn't
		 */

		if (unprocessedLinearValue.contains(" ")) {
			// we are working with a specified unit
			String[] split = unprocessedLinearValue.split(" ");
			if(split.length == 2) {
				try {
					processedLinearValue = Double.parseDouble(split[0]);

					switch(split[1]) {
						case "m":
							// do nothing as already in metres
							break;
						case "km":
							processedLinearValue = processedLinearValue / 0.001;
							break;
						case "cm":
							processedLinearValue = processedLinearValue / 100.0;
							break;
						case "mi":
							processedLinearValue = processedLinearValue / 0.000621371;
							break;
						case "nmi":
							processedLinearValue = processedLinearValue / 0.000539957;
							break;
						default:
							// Invalid unit
							processedLinearValue = -1d;
					}
				} catch (Exception e) {
					processedLinearValue = -1d;
				}
			}
		} else if (unprocessedLinearValue.contains("'") && unprocessedLinearValue.contains("\"")) {
			// Working with feet and inches
			String[] split = unprocessedLinearValue.split("'");
			if(split.length == 2) {
				split[1] = split[1].replace("\"", "");
				try {
					processedLinearValue = Double.parseDouble(split[0]) * 12d; // 12 inches to a foot
					processedLinearValue += Double.parseDouble(split[1]);

					// convert to metres
					processedLinearValue = processedLinearValue * 0.0254;

				} catch (Exception e) {
					processedLinearValue = -1d;
				}
			}
		} else {
			// Try and read a number and assume it is in metres
			try {
				processedLinearValue = Double.parseDouble(unprocessedLinearValue);
			} catch (Exception e) {
				processedLinearValue = -1d;
			}
		}

		// If the processedLinearValue is still -1, then it could be that they have used an invalid tag, so just try and parse the most common mistakes
		if(processedLinearValue == -1d) {
			// Be careful of the order as 3cm ends in both cm and m, so we should check for cm first
			try {
				if (unprocessedLinearValue.endsWith("cm")) {
					String[] split = unprocessedLinearValue.split("cm");
					if (split.length == 2) {
						processedLinearValue = Double.parseDouble(split[0]) / 100f;
					}
				} else if (unprocessedLinearValue.endsWith("km")) {
					String[] split = unprocessedLinearValue.split("km");
					if (split.length == 2) {
						processedLinearValue = Double.parseDouble(split[0]) / 0.001f;
					}
				}else if (unprocessedLinearValue.endsWith("nmi")) {
					String[] split = unprocessedLinearValue.split("nmi");
					if (split.length == 2) {
						processedLinearValue = Double.parseDouble(split[0]) / 0.000539957;
					}
				} else if (unprocessedLinearValue.endsWith("mi")) {
					String[] split = unprocessedLinearValue.split("mi");
					if (split.length == 2) {
						processedLinearValue = Double.parseDouble(split[0]) / 0.000621371;
					}
				} else if (unprocessedLinearValue.endsWith("m")) {
					String[] split = unprocessedLinearValue.split("m");
					if (split.length == 2) {
						processedLinearValue = Double.parseDouble(split[0]);
					}
				}
			} catch (NumberFormatException nfe) {
				// There was an invalid number, so just set it to be the "invalid" value
                processedLinearValue = -1d;
			}
		}

		// If the value is more than three, we need more bits in the encoder to store it, so we can just cap to 3
		if(processedLinearValue > 3)
			processedLinearValue = 3;

		return processedLinearValue;
	}

	/**
	 * Determine if the way is a separate footway object or a road feature.
	 *
	 * @param way		The OSM way object to be assessed
	 * @return			Whether the way is seen as a separately drawn footway (true) or a road (false)
	 */
	private boolean isSeparateFootway(ReaderWay way) {
		String type = way.getTag("highway", "");

		// Check if it is a footpath or pedestrian
		if(!type.isEmpty()) {
			// We are looking at a separate footpath
			// we are looking at a road feature so any footway would be attached to it as a tag
			return Arrays.asList(pedestrianTypes).contains(type);
		}

		return true;
	}

	@Override
	public String getName() {
		return "Wheelchair";
	}

	@Override
	public void finish() {
		// do nothing
	}
}
