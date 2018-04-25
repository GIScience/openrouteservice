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
package heigit.ors.routing.graphhopper.extensions.storages.builders;

import com.graphhopper.GraphHopper;
import com.graphhopper.reader.ReaderWay;
import com.graphhopper.storage.GraphExtension;
import com.graphhopper.util.EdgeIteratorState;

import com.vividsolutions.jts.geom.Coordinate;
import heigit.ors.routing.graphhopper.extensions.WheelchairAttributes;
import heigit.ors.routing.graphhopper.extensions.WheelchairTypesEncoder;
import heigit.ors.routing.graphhopper.extensions.flagencoders.WheelchairFlagEncoder;
import heigit.ors.routing.graphhopper.extensions.storages.WheelchairAttributesGraphStorage;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Arrays;

public class WheelchairGraphStorageBuilder extends AbstractGraphStorageBuilder 
{
	private static Logger LOGGER = Logger.getLogger(WheelchairGraphStorageBuilder.class.getName());

	private WheelchairAttributesGraphStorage _storage;
	private WheelchairAttributes _wheelchairAttributes;
	private WheelchairAttributes _wheelchairAttributesLeft;
	private WheelchairAttributes _wheelchairAttributesRight;

	private WheelchairFlagEncoder wheelchairFlagEncoder;

	private HashMap<Integer, HashMap<String,String>> nodeTags;
	private HashMap<String, Object> cleanedTags;

	private boolean _hasLeft = false;
	private boolean _hasRight = false;
	private boolean kerbOnCrossing = false;
	private boolean wayEastToWest = false;

	public WheelchairGraphStorageBuilder()
	{
		_wheelchairAttributes = new WheelchairAttributes();
		_wheelchairAttributesLeft = new WheelchairAttributes();
		_wheelchairAttributesRight = new WheelchairAttributes();
		nodeTags = new HashMap<>();
		cleanedTags = new HashMap<>();
	}

	private final String[] _pedestrianTypes = {
			"living_street",
			"pedestrian",
			"footway",
			"path",
			"crossing"
	};

	@Override
	public GraphExtension init(GraphHopper graphhopper) throws Exception 
	{
		if (!graphhopper.getEncodingManager().supports("wheelchair"))
			return null;

		if (_storage != null)
			throw new Exception("GraphStorageBuilder has been already initialized.");


		wheelchairFlagEncoder = new WheelchairFlagEncoder();
		wheelchairFlagEncoder.defineWayBits(0,0);

		if(_parameters.containsKey("KerbsOnCrossings")) {
			kerbOnCrossing = Boolean.parseBoolean(_parameters.get("KerbsOnCrossings"));
		}
		_storage = new WheelchairAttributesGraphStorage();
		return _storage;
	}

	@Override
	public void processWay(ReaderWay way) {
		this.processWay(way, new Coordinate[0], new HashMap<>());
	}

	@Override
	public void processWay(ReaderWay way, Coordinate[] coords, HashMap<Integer, HashMap<String,String>> nodeTags)
	{
		// Start by resetting storage variables
		_wheelchairAttributes.reset();

		_wheelchairAttributesLeft.reset();

		_wheelchairAttributesRight.reset();

		this.nodeTags = nodeTags;

		_hasRight = false;
		_hasLeft = false;



		// Annoyingly, it seems often to be the case that rather than using ":" to seperate tag parts, "." is used, so
		// we need to take this into account
		cleanedTags.clear();
		Map<String, Object> dirtyTags = way.getTags();
		for(String key : dirtyTags.keySet()) {
			String cleanKey = key.replace(".", ":");
			cleanedTags.put(cleanKey, dirtyTags.get(key));
		}

		// Now we need to process the way specific to whether it is a separate feature (i.e. footway) or is attached
		// to a road feature (i.e. with the tag sidewalk=left)
		if(isSeparateFootway(way)) {
			// We have a separate footway feature
			processSeparate(way);
		}

		// We still need to always process the way itself even if it separate so that we can get sidewalk info (a
		// separate footway can still ahve sidewalk tags...)
		processAttached(way);

		// We need to know which direction the way was drawn in so we can determine which side to attach sidewalks to
		// on edges
		// To do that, we need to look at the points to get a direction - but really we just want to know if it goes
		// from east to west as that is enough
		if(coords.length > 1) {
			wayEastToWest = coords[0].x < coords[1].x;
		}
	}

	/**
	 * Process footways that are attached to an OSM way via the sidewalk tags. It looks for parameters important for
	 * wheelchair routing such as width, smoothness and kerb height and then stores these in the attributes object
	 * ready for use when the edge(s) are processed. It also detects which side of the base way that the sidewalks
	 * have been created for and stores the information appropriately.
	 *
	 * @param way		The way to be processed
	 */
	private void processAttached(ReaderWay way) {
		String[] values;

		// check if there is an explicit tag
		if(way.hasTag("sidewalk")) {
			String sw = way.getTag("sidewalk");
			switch(sw) {
				case "left":
					_hasLeft = true;
				case "right":
					_hasRight = true;
				case "both":
					_hasLeft = true;
					_hasRight = true;
			}
		}

		// get surface type (asphalt, sand etc.)
		values = getCompoundValue(way, "surface");
		if(values[0] != null && !values[0].isEmpty()) {
			_hasLeft = true;
			_wheelchairAttributesLeft.setSurfaceType(WheelchairTypesEncoder.getSurfaceType(values[0].toLowerCase()));
		}
		if(values[1] != null && !values[1].isEmpty()) {
			_hasRight = true;
			_wheelchairAttributesRight.setSurfaceType(WheelchairTypesEncoder.getSurfaceType(values[1].toLowerCase()));
		}

		// get smoothness value (good, terrible etc.)
		values = getCompoundValue(way, "smoothness");
		if(values[0] != null && !values[0].isEmpty()) {
			_hasLeft = true;
			_wheelchairAttributesLeft.setSmoothnessType(WheelchairTypesEncoder.getSmoothnessType(values[0].toLowerCase()));

		}
		if(values[1] != null && !values[1].isEmpty()) {
			_hasRight = true;
			_wheelchairAttributesRight.setSmoothnessType(WheelchairTypesEncoder.getSmoothnessType(values[1].toLowerCase()));
		}

		// Get the track type (grade1, grade4 etc.)
		values = getCompoundValue(way, "tracktype");
		if(values[0] != null && !values[0].isEmpty()) {
			_hasLeft = true;
			_wheelchairAttributesLeft.setTrackType(WheelchairTypesEncoder.getTrackType(values[0].toLowerCase()));
		}
		if(values[1] != null && values[0] != null && !values[1].isEmpty()) {
			_hasRight = true;
			_wheelchairAttributesRight.setTrackType(WheelchairTypesEncoder.getTrackType(values[1].toLowerCase()));
		}

		// Get the width of the way (2, 0.1 etc.)
		values = getCompoundValue(way, "width");
		if(values[0] != null && !values[0].isEmpty()) {
			_hasLeft = true;
			_wheelchairAttributesLeft.setWidth((float) convertLinearValueToMetres(values[0].toLowerCase()));
		}
		if(values[1] != null && !values[1].isEmpty()) {
			_hasRight = true;
			_wheelchairAttributesRight.setWidth((float) convertLinearValueToMetres(values[1].toLowerCase()));
		}

		// Get the incline of the way (10%, 6% etc.)
		if (way.hasTag("incline"))
		{
			double incline = getInclineValueFromWay(way);
			if (incline != 0.0)
			{
				_wheelchairAttributesLeft.setIncline((float)incline);
				_wheelchairAttributesRight.setIncline((float)incline);
			}
		}
		values = getCompoundValue(way, "incline");
		if(values[0] != null && !values[0].isEmpty()) {
			_hasLeft = true;
			_wheelchairAttributesLeft.setWidth((float) convertInclineValueToPercentage(values[0].toLowerCase()));
		}
		if(values[1] != null && !values[1].isEmpty()) {
			_hasRight = true;
			_wheelchairAttributesRight.setWidth((float) convertInclineValueToPercentage(values[1].toLowerCase()));
		}

		// Assess any kerb height attached directly to the way
		if(way.hasTag("curb")) {
			double height = convertKerbTagToHeight("curb", way.getTag("curb"));
			_wheelchairAttributesLeft.setSlopedKerbHeight((float) height);
			_wheelchairAttributesRight.setSlopedKerbHeight((float) height);
		}
		if(way.hasTag("kerb")) {
			double height = convertKerbTagToHeight("kerb", way.getTag("kerb"));
			_wheelchairAttributesLeft.setSlopedKerbHeight((float) height);
			_wheelchairAttributesRight.setSlopedKerbHeight((float) height);
		}
		if(way.hasTag("sloped_curb")) {
			double height = convertKerbTagToHeight("sloped_curb", way.getTag("sloped_curb"));
			_wheelchairAttributesLeft.setSlopedKerbHeight((float) height);
			_wheelchairAttributesRight.setSlopedKerbHeight((float) height);
		}
		if(way.hasTag("kerb:height")) {
			double height = convertKerbTagToHeight("kerb:height", way.getTag("kerb:height"));
			_wheelchairAttributesLeft.setSlopedKerbHeight((float) height);
			_wheelchairAttributesRight.setSlopedKerbHeight((float) height);
		}

		// Also check if they have been marked for specific sides
		values = getCompoundKerb(way, "curb");
		if(values[0] != null && !values[0].isEmpty()) {
			_hasLeft = true;
			_wheelchairAttributesLeft.setSlopedKerbHeight((float) convertKerbTagToHeight("curb", values[0].toLowerCase()));
		}
		if(values[1] != null && !values[1].isEmpty()) {
			_hasRight = true;
			_wheelchairAttributesRight.setSlopedKerbHeight((float) convertKerbTagToHeight("curb", values[0].toLowerCase()));
		}
		values = getCompoundKerb(way, "kerb");
		if(values[0] != null && !values[0].isEmpty()) {
			_hasLeft = true;
			_wheelchairAttributesLeft.setSlopedKerbHeight((float) convertKerbTagToHeight("kerb", values[0].toLowerCase()));
		}
		if(values[1] != null && !values[1].isEmpty()) {
			_hasRight = true;
			_wheelchairAttributesRight.setSlopedKerbHeight((float) convertKerbTagToHeight("kerb", values[0].toLowerCase()));
		}
		values = getCompoundKerb(way, "sloped_curb");
		if(values[0] != null && !values[0].isEmpty()) {
			_hasLeft = true;
			_wheelchairAttributesLeft.setSlopedKerbHeight((float) convertKerbTagToHeight("sloped_curb", values[0].toLowerCase()));
		}
		if(values[1] != null && !values[1].isEmpty()) {
			_hasRight = true;
			_wheelchairAttributesRight.setSlopedKerbHeight((float) convertKerbTagToHeight("sloped_curb", values[0].toLowerCase()));
		}
		values = getCompoundKerb(way, "kerb:height");
		if(values[0] != null && !values[0].isEmpty()) {
			_hasLeft = true;
			_wheelchairAttributesLeft.setSlopedKerbHeight((float) convertKerbTagToHeight("kerb:height", values[0].toLowerCase()));
		}
		if(values[1] != null && !values[1].isEmpty()) {
			_hasRight = true;
			_wheelchairAttributesRight.setSlopedKerbHeight((float) convertKerbTagToHeight("kerb:height", values[0].toLowerCase()));
		}
	}

	private String[] getCompoundKerb(ReaderWay way, String key) {
		// If we are looking at the kerbs, sometimes the start and end of a way is marked as having different kerb
		// heights using the ...:start and ...:end tags. For now, we just want to get the worse of these values (the
		// highest)
		double leftStart = -1, leftEnd = -1, rightStart = -1, rightEnd = -1, leftNorm = -1, rightNorm = -1;

		String[] endValues = getCompoundValue(way, key + ":end");
		// Convert
		if(endValues[0] != null && !endValues[0].isEmpty()) {
			leftEnd = convertKerbTagToHeight(key, endValues[0]);
		}
		if(endValues[1] != null && !endValues[1].isEmpty()) {
			rightEnd = convertKerbTagToHeight(key, endValues[1]);
		}
		String[] startValues = getCompoundValue(way, key + ":start");
		// Convert
		if(startValues[0] != null && !startValues[0].isEmpty()) {
			leftStart = convertKerbTagToHeight(key, startValues[0]);
		}
		if(startValues[1] != null && !startValues[1].isEmpty()) {
			rightStart = convertKerbTagToHeight(key, startValues[1]);
		}

		String[] normValues = getCompoundValue(way, key);
		// Convert
		if(normValues[0] != null && !normValues[0].isEmpty()) {
			leftNorm = convertKerbTagToHeight(key, normValues[0]);
		}
		if(normValues[1] != null && !normValues[1].isEmpty()) {
			rightNorm = convertKerbTagToHeight(key, normValues[1]);
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
	private void processSeparate(ReaderWay way) {

		if (way.hasTag("surface"))
		{
			int value = WheelchairTypesEncoder.getSurfaceType(way.getTag("surface").toLowerCase());
			if (value > 0)
				_wheelchairAttributes.setSurfaceType(value);
		}

		if (way.hasTag("smoothness"))
		{
			int value = WheelchairTypesEncoder.getSmoothnessType(way.getTag("smoothness").toLowerCase());
			if (value > 0)
				_wheelchairAttributes.setSmoothnessType(value);
		}

		if (way.hasTag("tracktype")) {
			int value = WheelchairTypesEncoder.getTrackType(way.getTag("tracktype").toLowerCase());
			if (value > 0)
				_wheelchairAttributes.setTrackType(value);
		}

		if(way.hasTag("width")) {
			double width = convertLinearValueToMetres(way.getTag("width"));
			_wheelchairAttributes.setWidth((float)width);
		}

		// kerb height is only valid on separated ways
		if(way.hasTag("curb"))
			_wheelchairAttributes.setSlopedKerbHeight((float) convertKerbTagToHeight("curb", way.getTag("curb")));
		if(way.hasTag("kerb"))
			_wheelchairAttributes.setSlopedKerbHeight((float) convertKerbTagToHeight("kerb", way.getTag("kerb")));
		if(way.hasTag("sloped_curb"))
			_wheelchairAttributes.setSlopedKerbHeight((float) convertKerbTagToHeight("sloped_curb", way.getTag("sloped_curb")));
		if(way.hasTag("kerb:height"))
			_wheelchairAttributes.setSlopedKerbHeight((float) convertKerbTagToHeight("kerb:height", way.getTag("kerb:height")));

		// incline
		// =======
		// http://wiki.openstreetmap.org/wiki/Key:incline
		// http://wiki.openstreetmap.org/wiki/Wheelchair_routing#Path_properties.2C_in_general
		// http://wiki.openstreetmap.org/wiki/DE:Wheelchair_routing#Weg_Eigenschaften_allgemein
		if (way.hasTag("incline"))
		{
			double incline = getInclineValueFromWay(way);
			if (incline != 0.0)
			{
				_wheelchairAttributes.setIncline((float)incline);
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
	private <T> T getWorse(String attr, Class<T> type) {
		switch(attr) {
			case "surface":
				return type.cast(Math.max(Math.max(_wheelchairAttributesLeft.getSurfaceType(), _wheelchairAttributesRight.getSurfaceType()),
						_wheelchairAttributes.getSurfaceType()));
			case "smoothness":
				return type.cast(Math.max(Math.max(_wheelchairAttributesLeft.getSmoothnessType(), _wheelchairAttributesRight.getSmoothnessType()),
						_wheelchairAttributes.getSmoothnessType()));
			case "slopedKerb":
				return type.cast(Math.max(Math.max(_wheelchairAttributesLeft.getSlopedKerbHeight(), _wheelchairAttributesRight.getSlopedKerbHeight()),
						_wheelchairAttributes.getSlopedKerbHeight()));
			case "width":
				// default value is 0, but this will always be returned so we need to do a check
				float l = _wheelchairAttributesLeft.getWidth(),
						r = _wheelchairAttributesRight.getWidth(),
						w = _wheelchairAttributes.getWidth();
				if (l == 0) l = Float.MAX_VALUE;
				if (r == 0) r = Float.MAX_VALUE;
				if (w == 0) w = Float.MAX_VALUE;

				float ret = Math.min(Math.min(l,r),w);
				if (ret == Float.MAX_VALUE) ret = 0;

				return type.cast(ret);
			case "track":
				return type.cast(Math.max(Math.max(_wheelchairAttributesLeft.getTrackType(), _wheelchairAttributesRight.getTrackType()),
						_wheelchairAttributes.getTrackType()));
			case "incline":
				return type.cast(Math.max(Math.max(_wheelchairAttributesLeft.getIncline(), _wheelchairAttributesRight.getIncline()),
						_wheelchairAttributes.getIncline()));

				default:
					return null;
		}
	}

	@Override
	public void processEdge(ReaderWay way, EdgeIteratorState edge) {

	}

	@Override
	public void processEdge(ReaderWay way, EdgeIteratorState edge, Coordinate[] coords)
	{
		WheelchairAttributes at = _wheelchairAttributes.copy();

		// If we are only dealing with kerbs on crossings, then we need ot check that the way is a crossing, else work
		// with all ways
        // This is only applicable if the kerb height is stored on a node rather than on the way itself. If that is the
        // case, then the kerb height has already been stored in the attributes.

		if((kerbOnCrossing && way.hasTag("footway") && way.getTag("footway").equals("crossing"))
				|| !kerbOnCrossing) {
			// Look for kerb information
			List<Float> kerbHeights = new ArrayList<>();
			for(int id : nodeTags.keySet()) {
				// We only want to add the kerb height to the edge that is actually connected to it
				if(id == edge.getAdjNode() || id == edge.getBaseNode()) {
					HashMap<String, String> tags = nodeTags.get(id);
					for (String key : tags.keySet()) {
						switch (key) {
							case "sloped_curb":
							case "curb":
							case "kerb":
							case "sloped_kerb":
							case "kerb:height":
								kerbHeights.add((float) convertKerbTagToHeight(key, tags.get(key)));
								break;
						}
					}
				}
			}
			if(kerbHeights.size() > 0) {
				// If we have multiple kerb heights, we need to apply the largest to the edge as this is the worst
				if(kerbHeights.size() > 1) {
					java.util.Collections.sort(kerbHeights, new Comparator<Float>() {
						@Override
						public int compare(Float v1, Float v2) {
								return (v1 < v2) ? 1 : -1;
							}
					});
				}
				at.setSlopedKerbHeight(kerbHeights.get(0));
			}
		}

		// Check for if we have specified which side the processing is for
        if(way.hasTag("ors-sidewalk-side")) {
			// Look at which way the edge goes
			boolean eastToWest = false;
			if(coords.length > 1) {
				eastToWest = coords[0].x <= coords[coords.length-1].x;
			}

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
            if(side.equals("right")) {
		    	at = getAttributes("right");
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
			if (_hasRight || _hasLeft) {
				int tr = getWorse("track", Integer.class);
				if (tr > 0) at.setTrackType(tr);

				int su = getWorse("surface", Integer.class);
				if (su > 0) at.setSurfaceType(su);

				int sm = getWorse("smoothness", Integer.class);
				if (sm > 0) at.setSmoothnessType(sm);

				float sl = getWorse("slopedKerb", Float.class);
				if (sl > 0) at.setSlopedKerbHeight(sl);

				float wi = getWorse("width", Float.class);
				if (wi > 0) at.setWidth(wi);

				float in = getWorse("incline", Float.class);
				if (in > 0) at.setIncline(in);
			}
		}

		_storage.setEdgeValues(edge.getEdge(), at);

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
		WheelchairAttributes at = _wheelchairAttributes.copy();

		// Now get the specific items
		switch(side) {
			case "left":
				at = at.merge(_wheelchairAttributesLeft);
				break;
			case "right":
				at = at.merge(_wheelchairAttributesRight);
				break;
		}

		return at;
	}

	/**
	 * Converts a kerb height value to a numerical height (in metres). A kerb could be stored as an explicit height or
	 * as an indicator as to whether the kerb is lowered or not.
	 *
	 * @param tag		The key (tag) that was obtained describing the kerb information
	 * @param value		The value of the tag
	 * @return			The presumed height of the kerb in metres
	 */
	private double convertKerbTagToHeight(String tag, String value) {
		double height = -1d;

		if(tag.equals("sloped_curb") || tag.equals("curb") || tag.equals("kerb")) {
			// THE TAGS sloped_curb AND curb SHOULD NOT BE USED
			// also, many of the values are not recognised as proper OSM tags, but they still exist
			switch(value) {
				case "yes":
				case "both":
				case "low":
				case "lowered":
				case "dropped":
				case "sloped":
					height = 0.03d;
					break;
				case "no":
				case "none":
				case "one":
				case "rolled":
				case "regular":
					height = 0.15d;
					break;
				case "at_grade":
				case "flush":
					height = 0d;
					break;
				default:
					// May be that it is already numeric (though it shouldn't be)
					height = convertLinearValueToMetres(value);
					break;
			}
		}
		if(tag.equals("kerb:height")) {
			// we need to also check for the measurement unit
			// we can use the same unit conversion as width
			height = convertLinearValueToMetres(value);
		}

		return height;
	}

	/**
	 * Get the values obtained from a way for a specific sidewalk property. Fotr example, providing the property
	 * "surface" would check the way for the surface tag stored against attached sidewalks using the keys
	 * sidewalk:left:surface, sidewalk:right:surface, and sidewalk:both:surface. The obtained values are then returned
	 * in an array.
	 *
	 * @param way			The way object to extract the properties from
	 * @param property		The property to be extracted
	 *
	 * @return				A String array containing two values - the first is the property for the left sidewalk and
	 * 						the second is the property value for the right sidewalk.
	 */
	private String[] getCompoundValue(ReaderWay way, String property) {
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
		if(cleanedTags.containsKey("sidewalk:both:" + property)) {
			values[0] = (String) cleanedTags.get("sidewalk:both:" + property);
			values[1] = (String) cleanedTags.get("sidewalk:both:" + property);
		}
		else if(cleanedTags.containsKey("footway:both:" +property)) {
			values[0] = (String) cleanedTags.get("footway:both:" + property);
			values[1] = (String) cleanedTags.get("footway:both:" + property);
		}

		return values;
	}

	private double getInclineValueFromWay(ReaderWay way)
	{
		String inclineValue = way.getTag("incline");
		return convertInclineValueToPercentage(inclineValue);
	}

	/**
	 * Convert the String representation of an incline into a %age incline value. in OSM the tag value could already
	 * be a %age value, or it could be written as "up", "down", "steep" etc. in which case an incline value is assumed
	 *
	 * @param unprocessedInclineValue		The value obtained from the incline tag
	 * @return					a percentage incline value
	 */
	private double convertInclineValueToPercentage(String unprocessedInclineValue) {

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
			}
			catch (Exception ex) {

			}

			// If the value seems too extreme, then we should limit
			if (Math.abs(v) > 15) {
				v = 15;
			}

			return v;
		}

		return 0;
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
			String split[] = unprocessedLinearValue.split(" ");
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
			if (Arrays.asList(_pedestrianTypes).contains(type)) {
				// We are looking at a separate footpath
				return true;
			} else {
				// we are looking at a road feature so any footway would be attached to it as a tag
				return false;
			}
		}

		return true;
	}

	@Override
	public String getName() {
		return "Wheelchair";
	}

	@Override
	public void finish() {

	}
}
