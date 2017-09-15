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

import heigit.ors.routing.graphhopper.extensions.WheelchairAttributes;
import heigit.ors.routing.graphhopper.extensions.WheelchairTypesEncoder;
import heigit.ors.routing.graphhopper.extensions.storages.WheelchairAttributesGraphStorage;

public class WheelchairGraphStorageBuilder extends AbstractGraphStorageBuilder 
{
	private WheelchairAttributesGraphStorage _storage;
	private WheelchairAttributes _wheelchairAttributes;

	public WheelchairGraphStorageBuilder()
	{
		_wheelchairAttributes = new WheelchairAttributes();
	}

	public GraphExtension init(GraphHopper graphhopper) throws Exception 
	{
		if (!graphhopper.getEncodingManager().supports("wheelchair"))
			return null;

		if (_storage != null)
			throw new Exception("GraphStorageBuilder has been already initialized.");

		_storage = new WheelchairAttributesGraphStorage();
		return _storage;
	}

	public void processWay(ReaderWay way) 
	{
		_wheelchairAttributes.reset();

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

		// sloped_curb
		// ===========
		// http://wiki.openstreetmap.org/wiki/Wheelchair_routing#Curb_heights
		// http://wiki.openstreetmap.org/wiki/DE:Wheelchair_routing#B.C3.BCrgersteige
		// http://wiki.openstreetmap.org/wiki/DE:Wheelchair_routing#B.C3.BCrgersteige_und_Eigenschaften
		// http://wiki.openstreetmap.org/wiki/Key:sloped_curb

		// only use sloped_curb|kerb|curb values on ways that are crossing. there are cases (e.g. platform) where these tags are also used but in fact indicate wheelchair accessibility (e.g. platform=yes, kerb=raised)
		if ((way.hasTag("sloped_curb") || way.hasTag("kerb") || way.hasTag("curb")) && (way.hasTag("footway", "crossing") || way.hasTag("cycleway", "crossing") || way.hasTag("highway", "crossing") || way.hasTag("crossing"))) {
			
			double curbHeight = getCurbHeight(way);
			if (curbHeight != 0.0)
				_wheelchairAttributes.setSlopedCurbHeight((float)curbHeight);
		}

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

	public void processEdge(ReaderWay way, EdgeIteratorState edge) 
	{
		_storage.setEdgeValues(edge.getEdge(), _wheelchairAttributes);
	}

	private double getCurbHeight(ReaderWay way) {
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
				str = str.replace("no", "0.15");
				str = str.replace("dropped", "0.03");
				str = str.replace("rolled", "0.03");
				str = str.replace("none", "0.15");
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

	private double getIncline(ReaderWay way)
	{
		String inclineValue = way.getTag("incline");
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

	@Override
	public String getName() {
		return "Wheelchair";
	}

	@Override
	public void finish() {

	}
}
