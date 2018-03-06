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

import java.util.Arrays;
import java.util.HashMap;

public class WheelchairGraphStorageBuilder extends AbstractGraphStorageBuilder 
{
	private static Logger LOGGER = Logger.getLogger(WheelchairGraphStorageBuilder.class.getName());
	private enum Side {LEFT, RIGHT, BOTH};

	private WheelchairAttributesGraphStorage _storage;
	private WheelchairAttributes _wheelchairAttributes;
	private WheelchairAttributes _wheelchairAttributesLeft;
	private WheelchairAttributes _wheelchairAttributesRight;

	private WheelchairFlagEncoder wheelchairFlagEncoder;

	private HashMap<Integer, HashMap<String,String>> nodeTags;

	private boolean _isSeparate = false;
	private boolean _hasLeft = false;
	private boolean _hasRight = false;

	public WheelchairGraphStorageBuilder()
	{
		_wheelchairAttributes = new WheelchairAttributes();
		_wheelchairAttributesLeft = new WheelchairAttributes();
		_wheelchairAttributesRight = new WheelchairAttributes();
		nodeTags = new HashMap<>();
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

		_storage = new WheelchairAttributesGraphStorage();
		return _storage;
	}

	@Override
	public void processWay(ReaderWay way) {

	}

	@Override
	public void processWay(ReaderWay way, Coordinate[] coords, HashMap<Integer, HashMap<String,String>> nodeTags)
	{

		_wheelchairAttributes.reset();

		_wheelchairAttributesLeft.reset();

		_wheelchairAttributesRight.reset();

		this.nodeTags = nodeTags;

		_isSeparate = false;
		_hasRight = false;
		_hasLeft = false;

		// When we read a way there are four possibilities:
		/*
			1. We are looking at a separated pedestrian way (i.e. footpath) - use way properties
			2. We are looking at a road with sidewalks tagged to it	- use sidewalk properties
			3. We are looking at a pedestrian way with sidewalks tagged to it - use sidewalk and way properties
			5. We are just looking at a road - use no properties
		 */


		if(isSeparateFootway(way)) {
			// We have a separate footway feature
			// work with _wheelchairAttributes
			processSeparate(way);
		}

		// always process the way as attached to get any sidewalks
		processAttached(way);

		// sloped_curb
		// ===========
		// http://wiki.openstreetmap.org/wiki/Wheelchair_routing#Curb_heights
		// http://wiki.openstreetmap.org/wiki/DE:Wheelchair_routing#B.C3.BCrgersteige
		// http://wiki.openstreetmap.org/wiki/DE:Wheelchair_routing#B.C3.BCrgersteige_und_Eigenschaften
		// http://wiki.openstreetmap.org/wiki/Key:sloped_curb

		// only use sloped_curb|kerb|curb values on ways that are crossing. there are cases (e.g. platform) where these tags are also used but in fact indicate wheelchair accessibility (e.g. platform=yes, kerb=raised)
		/*if ((way.hasTag("sloped_curb") || way.hasTag("kerb") || way.hasTag("curb")) && (way.hasTag("footway", "crossing") || way.hasTag("cycleway", "crossing") || way.hasTag("highway", "crossing") || way.hasTag("crossing"))) {
			
			double curbHeight = getCurbHeight(way);
			if (curbHeight != 0.0)
				_wheelchairAttributes.setSlopedCurbHeight((float)curbHeight);
		}*/


	}

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

		values = getCompoundValue(way, "surface");
		if(values[0] != null && !values[0].isEmpty()) {
			_hasLeft = true;
			_wheelchairAttributesLeft.setSurfaceType(WheelchairTypesEncoder.getSurfaceType(values[0].toLowerCase()));
		}
		if(values[1] != null && !values[1].isEmpty()) {
			_hasRight = true;
			_wheelchairAttributesRight.setSurfaceType(WheelchairTypesEncoder.getSurfaceType(values[1].toLowerCase()));
		}

		values = getCompoundValue(way, "smoothness");
		if(values[0] != null && !values[0].isEmpty()) {
			_hasLeft = true;
			_wheelchairAttributesLeft.setSmoothnessType(WheelchairTypesEncoder.getSmoothnessType(values[0].toLowerCase()));

		}
		if(values[1] != null && !values[1].isEmpty()) {
			_hasRight = true;
			_wheelchairAttributesRight.setSmoothnessType(WheelchairTypesEncoder.getSmoothnessType(values[1].toLowerCase()));
		}

		values = getCompoundValue(way, "tracktype");
		if(values[0] != null && !values[0].isEmpty()) {
			_hasLeft = true;
			_wheelchairAttributesLeft.setTrackType(WheelchairTypesEncoder.getTrackType(values[0].toLowerCase()));
		}
		if(values[1] != null && values[0] != null && !values[1].isEmpty()) {
			_hasRight = true;
			_wheelchairAttributesRight.setTrackType(WheelchairTypesEncoder.getTrackType(values[1].toLowerCase()));
		}

		values = getCompoundValue(way, "width");
		if(values[0] != null && !values[0].isEmpty()) {
			_hasLeft = true;
			_wheelchairAttributesLeft.setWidth((float)convertWidth(values[0].toLowerCase()));
		}
		if(values[1] != null && !values[1].isEmpty()) {
			_hasRight = true;
			_wheelchairAttributesRight.setWidth((float)convertWidth(values[1].toLowerCase()));
		}

		if (way.hasTag("incline"))
		{
			double incline = getIncline(way);
			if (incline != 0.0)
			{
				_wheelchairAttributesLeft.setIncline((float)incline);
				_wheelchairAttributesRight.setIncline((float)incline);
			}
		}
		values = getCompoundValue(way, "incline");
		if(values[0] != null && !values[0].isEmpty()) {
			_hasLeft = true;
			_wheelchairAttributesLeft.setWidth((float)convertIncline(values[0].toLowerCase()));
		}
		if(values[1] != null && !values[1].isEmpty()) {
			_hasRight = true;
			_wheelchairAttributesRight.setWidth((float)convertIncline(values[1].toLowerCase()));
		}
	}

	private void processSeparate(ReaderWay way) {
		_isSeparate = true;

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
			double width = convertWidth(way.getTag("width"));
			_wheelchairAttributes.setWidth((float)width);
		}

		// kerb height is only valid on separated ways
		if(way.hasTag("curb"))
			_wheelchairAttributes.setSlopedKerbHeight((float)convertKerb("curb", way.getTag("curb")));
		if(way.hasTag("kerb"))
			_wheelchairAttributes.setSlopedKerbHeight((float)convertKerb("kerb", way.getTag("kerb")));
		if(way.hasTag("sloped_curb"))
			_wheelchairAttributes.setSlopedKerbHeight((float)convertKerb("sloped_curb", way.getTag("sloped_curb")));
		if(way.hasTag("kerb:height"))
			_wheelchairAttributes.setSlopedKerbHeight((float)convertKerb("kerb:height", way.getTag("kerb:height")));

		// incline
		// =======
		// http://wiki.openstreetmap.org/wiki/Key:incline
		// http://wiki.openstreetmap.org/wiki/Wheelchair_routing#Path_properties.2C_in_general
		// http://wiki.openstreetmap.org/wiki/DE:Wheelchair_routing#Weg_Eigenschaften_allgemein
		if (way.hasTag("incline"))
		{
			double incline = getIncline(way);
			if (incline != 0.0)
			{
				_wheelchairAttributes.setIncline((float)incline);
			}
		}
	}

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
	public void processEdge(ReaderWay way, EdgeIteratorState edge) 
	{
		WheelchairAttributes at = _wheelchairAttributes.copy();
		/*at.reset();

		if(_isSeparate) {
			// we have a seperate way so we also need to look at the attributes of the way itself
			at = _wheelchairAttributes.copy();
		}*/

		// Look to see if it is a barrier edge (i.e. a node that has signified a potential barrier
		if(edge.getDistance() == 0) {
			// We need to get the tags from the node
			boolean hasKerb = false;

			HashMap<String, String> tags;
			tags = nodeTags.get(edge.getBaseNode());
			if(tags == null)
				tags = nodeTags.get(edge.getAdjNode());

			for(String tag : tags.keySet()) {
				switch(tag) {
					case "sloped_curb":
					case "curb":
					case "kerb":
					case "sloped_kerb":
					case "kerb:height":
						at.setSlopedKerbHeight((float)convertKerb(tag, tags.get(tag)));
						hasKerb = true;
						break;
				}
			}
			// Say that we can traverse the edge normally... As this was created as a barrier, it is automatically
			// marked as not traversible in both directions, so we need to undo that in the case of kerbs
			if(hasKerb) {
				edge.setFlags(wheelchairFlagEncoder.setBool(edge.getFlags(), 0, true));
				edge.setFlags(wheelchairFlagEncoder.setBool(edge.getFlags(), 1, true));
			}
		}

		// Check for if we have specified which side the processing is for
        if(way.hasTag("ors-sidewalk-side")) {
		    String side = way.getTag("ors-sidewalk-side");
		    if(side.equals("left")) {
				// Only get the attributes for the left side
				at = getAttributes("left");
            }
            if(side.equals("right")) {
		    	at = getAttributes("right");
			}
        } else {
			// if we have sidewalks attached, then we should also look at those
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

	private double convertKerb(String tag, String value) {
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
			}
		}
		if(tag.equals("kerb:height")) {
			// we need to also check for the measurement unit
			// we can use the same unit conversion as width
			height = convertWidth(value);
		}

		return height;
	}

	private double getKerbHeight(ReaderWay way) {
		// http://taginfo.openstreetmap.org/keys/kerb#overview: 80% nodes, 20% ways
		// http://taginfo.openstreetmap.org/keys/kerb#values
		double res = 0d;
		String str = null;
		// http://taginfo.openstreetmap.org/keys/sloped_curb#overview: 90% nodes, 10% ways
		// http://taginfo.openstreetmap.org/keys/sloped_curb#values
		if (way.hasTag("sloped_curb")) {
			str = way.getTag("sloped_curb").toLowerCase();
			str = str.replace("yes", "0.03");
			str = str.replace("both", "0.03");
			str = str.replace("no", "0.15");
			str = str.replace("one", "0.15");
			str = str.replace("at_grade", "0.0");
			str = str.replace("flush", "0.0");
			str = str.replace("low", "0.03");
		}
		else if (way.hasTag("kerb")) {
			if (way.hasTag("kerb:height")) {
				str = way.getTag("kerb:height").toLowerCase();
			}
			else {
				str = way.getTag("kerb").toLowerCase();
				str = str.replace("lowered", "0.03");
				str = str.replace("raised", "0.15");
				str = str.replace("yes", "0.03");
				str = str.replace("flush", "0.0");
				str = str.replace("unknown", "0.03");
				str = str.replace("none", "0.15");
				str = str.replace("no", "0.15");
				str = str.replace("dropped", "0.03");
				str = str.replace("rolled", "0.03");
			}
		}
       
		// http://taginfo.openstreetmap.org/keys/curb#overview: 70% nodes, 30% ways
		// http://taginfo.openstreetmap.org/keys/curb#values
		else if (way.hasTag("curb")) {
			str = way.getTag("curb").toLowerCase();
			str = str.replace("lowered", "0.03");
			str = str.replace("regular", "0.15");
			str = str.replace("flush;lowered", "0.0");
			str = str.replace("sloped", "0.03");
			str = str.replace("lowered_and_sloped", "0.03");
			str = str.replace("flush", "0.0");
			str = str.replace("none", "0.15");
			str = str.replace("flush_and_lowered", "0.0");
		}

		if (str != null) {
			boolean isCm = false;
			try {
				if (str.contains("c")) {
					isCm = true;
				}
				res = Double.parseDouble(str.replace("%", "").replace(",", ".").replace("m", "").replace("c", ""));
				if (isCm) {
					res /= 100d;
				}
			}
			catch (Exception ex) {
				//	logger.warning("Error parsing value for Tag kerb from this String: " + stringValue + ". Exception:" + ex.getMessage());
			}
		}

		// check if the value makes sense (i.e. maximum 0.3m/30cm)
		if (-0.15 < res && res < 0.15) {
			res = Math.abs(res);
		}
		else {
			// doubleValue = Double.NaN;
			res = 0.15;
		}
		
		return res;
	}

	private String[] getCompoundValue(ReaderWay way, String property) {
		String[] values = new String[2];

		// Left side
		if(way.hasTag("sidewalk:left:" + property))
			values[0] = way.getTag("sidewalk:left:" + property);
		// Right side
		if(way.hasTag("sidewalk:right:" + property))
			values[1] = way.getTag("sidewalk:right:" + property);

		// Both
		if(way.hasTag("sidewalk:both:" + property)) {
			values[0] = way.getTag("sidewalk:both:" + property);
			values[1] = way.getTag("sidewalk:both:" + property);
		}

		return values;
	}

	private double getIncline(ReaderWay way)
	{
		String inclineValue = way.getTag("incline");
		return convertIncline(inclineValue);
	}

	private double convertIncline(String inclineValue) {

		if (inclineValue != null)
		{
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

			}
			// Fist check if the value makes sense
			// http://wiki.openstreetmap.org/wiki/DE:Key:incline
			// TODO: deal with negative incline (indicates the direction of the incline => might not be important for use wheelchair user as too much incline is an exclusion criterion in both directions?)
			if (-50 < v && v < 50) {
				// value seems to be okay
			}
			else {
				// v = Double.NaN;
				v = 15;
			}
			if (Math.abs(v) > 15) {
				v = 15;
			}

			return v;
		}

		return 0;
	}

	private double convertWidth(String widthStr) {
		double width = -1d;

		// Valid values are:
		/*
		width=x (default metres)
		width=x m		(metre)
		width=x km		(kilometre)
		width=x mi		(mile)
		width=x nmi		(nautical mile)
		width=x'y"		(feet and inches)

		However, many people omit the space, even though they shouldn't
		 */

		if (widthStr.contains(" ")) {
			// we are working with a specified unit
			String split[] = widthStr.split(" ");
			if(split.length == 2) {
				try {
					width = Double.parseDouble(split[0]);

					switch(split[1]) {
						case "m":
							// do nothing as already in metres
							break;
						case "km":
							width = width / 0.001;
							break;
						case "cm":
							width = width / 100.0;
							break;
						case "mi":
							width = width / 0.000621371;
							break;
						case "nmi":
							width = width / 0.000539957;
							break;
						default:
							// Invalid unit
							width = -1d;
					}
				} catch (Exception e) {
					width = -1d;
				}
			}
		} else if (widthStr.contains("'") && widthStr.contains("\"")) {
			// Working with feet and inches
			String[] split = widthStr.split("'");
			if(split.length == 2) {
				split[1] = split[1].replace("\"", "");
				try {
					width = Double.parseDouble(split[0]) * 12d; // 12 inches to a foot
					width += Double.parseDouble(split[1]);

					// convert to metres
					width = width * 0.0254;

				} catch (Exception e) {
					width = -1d;
				}
			}
		} else {
			// Try and read a number and assume it is in metres
			try {
				width = Double.parseDouble(widthStr);
			} catch (Exception e) {
				width = -1d;
			}
		}

		// If the width is still -1, then it could be that they have used an invalid tag, so just try and parse the most common
		if(width == -1d) {
			// Be careful of the order as 3cm ends in both cm and m, so we should check for cm first
			try {
				if (widthStr.endsWith("cm")) {
					String[] split = widthStr.split("cm");
					if (split.length == 2) {
						width = Double.parseDouble(split[0]) / 100f;
					}
				} else if (widthStr.endsWith("km")) {
					String[] split = widthStr.split("km");
					if (split.length == 2) {
						width = Double.parseDouble(split[0]) / 0.001f;
					}
				}else if (widthStr.endsWith("nmi")) {
					String[] split = widthStr.split("nmi");
					if (split.length == 2) {
						width = Double.parseDouble(split[0]) / 0.000539957;
					}
				} else if (widthStr.endsWith("mi")) {
					String[] split = widthStr.split("mi");
					if (split.length == 2) {
						width = Double.parseDouble(split[0]) / 0.000621371;
					}
				} else if (widthStr.endsWith("m")) {
					String[] split = widthStr.split("m");
					if (split.length == 2) {
						width = Double.parseDouble(split[0]);
					}
				}
			} catch (NumberFormatException nfe) {
				// There was an invalid number, so just ignore it
			}
		}

		// If it is more than three we don't really care, and any more means more bits needed to store
		if(width > 3)
			width = 3;

		return width;
	}

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
