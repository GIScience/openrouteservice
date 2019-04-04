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
package heigit.ors.routing.graphhopper.extensions.storages.builders;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.graphhopper.GraphHopper;
import com.graphhopper.reader.ReaderWay;
import com.graphhopper.storage.GraphExtension;
import com.graphhopper.util.EdgeIteratorState;
import com.graphhopper.util.Helper;

import heigit.ors.routing.graphhopper.extensions.HeavyVehicleAttributes;
import heigit.ors.routing.graphhopper.extensions.VehicleDimensionRestrictions;
import heigit.ors.routing.graphhopper.extensions.storages.HeavyVehicleAttributesGraphStorage;

public class HeavyVehicleGraphStorageBuilder extends AbstractGraphStorageBuilder
{
	private boolean _includeRestrictions = true;
	private HeavyVehicleAttributesGraphStorage _storage;
	private int _hgvType = 0;
	private int _hgvDestination = 0;
	private boolean _hasRestrictionValues;
	private double[] _restrictionValues = new double[VehicleDimensionRestrictions.Count];
	private List<String> _motorVehicleRestrictions = new ArrayList<String>(5);
	private Set<String> _motorVehicleRestrictedValues = new HashSet<String>(5);
	private Set<String> _motorVehicleHgvValues = new HashSet<String>(6);

	private Set<String> _noValues = new HashSet<String>(5);
	private Set<String> _yesValues = new HashSet<String>(5);
	private Pattern _patternDimension;

	public HeavyVehicleGraphStorageBuilder()
	{
		_motorVehicleRestrictions.addAll(Arrays.asList("motorcar", "motor_vehicle", "vehicle", "access"));

		_motorVehicleRestrictedValues.add("private");
		_motorVehicleRestrictedValues.add("no");
		_motorVehicleRestrictedValues.add("restricted");
		_motorVehicleRestrictedValues.add("military");

		_motorVehicleHgvValues.addAll(Arrays.asList("hgv", "goods", "bus", "agricultural", "forestry", "delivery"));

		_noValues.addAll(Arrays.asList("no", "private"));
		_yesValues.addAll(Arrays.asList("yes", "designated"));

		_patternDimension = Pattern.compile("(?:\\s*(\\d+)\\s*(?:feet|ft\\.|ft|'))?(?:(\\d+)\\s*(?:inches|in\\.|in|''|\"))?");
	}
	
	public GraphExtension init(GraphHopper graphhopper) throws Exception {
		if (_storage != null)
			throw new Exception("GraphStorageBuilder has been already initialized.");
		
		if (_parameters != null)
		{
			String value = _parameters.get("restrictions");
			if (!Helper.isEmpty(value))
				_includeRestrictions = Boolean.parseBoolean(value);
		}
		
		_storage = new HeavyVehicleAttributesGraphStorage(_includeRestrictions);
		
		return _storage;
	}

	public void processWay(ReaderWay way) {
		// reset values
		_hgvType = 0;
		_hgvDestination = 0;

		if (_hasRestrictionValues) {
			_restrictionValues[0] = 0.0;
			_restrictionValues[1] = 0.0;
			_restrictionValues[2] = 0.0;
			_restrictionValues[3] = 0.0;
			_restrictionValues[4] = 0.0;

			_hasRestrictionValues = false;
		}

		boolean hasHighway = way.hasTag("highway");

		if (hasHighway) {
			// process motor vehicle restrictions before any more specific vehicle type tags which override the former

			// if there are any generic motor vehicle restrictions restrict all types...
			if (way.hasTag(_motorVehicleRestrictions, _motorVehicleRestrictedValues))
				_hgvType = HeavyVehicleAttributes.ANY;

			//...or all but the explicitly listed ones
			if (way.hasTag(_motorVehicleRestrictions, _motorVehicleHgvValues)) {
				int flag = 0;
				for (String key : _motorVehicleRestrictions) {
					String val = way.getTag(key);
					if (_motorVehicleHgvValues.contains(val))
						flag |= HeavyVehicleAttributes.getFromString(val);
				}
				_hgvType = HeavyVehicleAttributes.ANY & ~flag;
			}

			Iterator<Entry<String, Object>> it = way.getProperties();

			while (it.hasNext()) {
				Map.Entry<String, Object> pairs = it.next();
				String key = pairs.getKey();
				String value = pairs.getValue().toString();

				/*
				 * https://wiki.openstreetmap.org/wiki/Restrictions
				 */

				int valueIndex = -1;

				switch(key) {
					case "maxheight":
						valueIndex = VehicleDimensionRestrictions.MaxHeight;
						break;
					case "maxweight":
					case "maxweight:hgv":
						valueIndex = VehicleDimensionRestrictions.MaxWeight;
						break;
					case "maxwidth":
						valueIndex = VehicleDimensionRestrictions.MaxWidth;
						break;
					case "maxlength":
					case "maxlength:hgv":
						valueIndex = VehicleDimensionRestrictions.MaxLength;
						break;
					case "maxaxleload":
						valueIndex = VehicleDimensionRestrictions.MaxAxleLoad;
						break;
				}

				// given tag is a weight/dimension restriction
				if (valueIndex >= 0) {
					if (_includeRestrictions && !("none".equals(value) || "default".equals(value))) {
						double parsedValue = -1;

						// sanitize decimal separators
						if (value.contains(","))
							value = value.replace(',', '.');

						// weight restrictions
						if (valueIndex == VehicleDimensionRestrictions.MaxWeight || valueIndex == VehicleDimensionRestrictions.MaxAxleLoad) {
							if (value.contains("t")) {
								value = value.replace('t', ' ');
							} else if (value.contains("lbs")) {
								value = value.replace("lbs", " ");
								parsedValue = parseDouble(value) / 2204.622;
							}
						}

						// dimension restrictions
						else {
							if (value.contains("m")) {
								value = value.replace('m', ' ');
							} else {
								Matcher m = _patternDimension.matcher(value);
								if (m.matches() && m.lookingAt()) {
									double feet = parseDouble(m.group(1));
									double inches = 0;
									if (m.groupCount() > 1 && m.group(2) != null) {
										inches = parseDouble(m.group(2));
									}
									parsedValue = feet * 0.3048 + inches * 0.0254;
								}
							}
						}

						if (parsedValue == -1)
							parsedValue = parseDouble(value);

						// it was possible to extract a reasonable value
						if (parsedValue > 0) {
							_restrictionValues[valueIndex] = parsedValue;
							_hasRestrictionValues = true;
						}
					}
				}

				if (_motorVehicleHgvValues.contains(key)) {

					// TODO: the following implementation does not pick up access:destination
					String hgvTag = getHeavyVehicleValue(key, "hgv", value);
					String goodsTag = getHeavyVehicleValue(key, "goods", value);
					String busTag = getHeavyVehicleValue(key, "bus", value);
					String agriculturalTag = getHeavyVehicleValue(key, "agricultural", value);
					String forestryTag = getHeavyVehicleValue(key, "forestry", value);
					String deliveryTag = getHeavyVehicleValue(key, "delivery", value);

					setFlagsFromTag(goodsTag, HeavyVehicleAttributes.GOODS);
					setFlagsFromTag(hgvTag, HeavyVehicleAttributes.HGV);
					setFlagsFromTag(busTag, HeavyVehicleAttributes.BUS);
					setFlagsFromTag(agriculturalTag, HeavyVehicleAttributes.AGRICULTURE);
					setFlagsFromTag(forestryTag, HeavyVehicleAttributes.FORESTRY);
					setFlagsFromTag(deliveryTag, HeavyVehicleAttributes.DELIVERY);

				}
				else if (key.equals("hazmat") && "no".equals(value)) {
					_hgvType |= HeavyVehicleAttributes.HAZMAT;
				}
			}
		}
	}

	public void processEdge(ReaderWay way, EdgeIteratorState edge)
	{
		if (_hgvType > HeavyVehicleAttributes.UNKNOWN || _hgvDestination > 0 || _hasRestrictionValues) 
		{
			if (_hasRestrictionValues)
				_storage.setEdgeValue(edge.getEdge(), _hgvType, _hgvDestination, _restrictionValues);
			else
				_storage.setEdgeValue(edge.getEdge(), _hgvType, _hgvDestination, null);
	    }
	}

	private String getHeavyVehicleValue(String key, String hv, String value) {
		if (value.equals(hv))
			return value;
		else if (key.equals(hv)) {
			if (_yesValues.contains(value))
				return "yes";
			else if (_noValues.contains(value))
				return "no";
		}
		else if (key.equals(hv+":forward") || key.equals(hv+":backward"))
			return value;
		
		return null;
	}

	/**
	 * Toggle the bit corresponding to a given hgv type defined by {@code flag} inside binary restriction masks based on
	 * the value of {@code tag}. "no" sets the bit in {@code _hgvType}, while "yes" unsets it.
	 *
	 * When the value is "destination" (or "delivery" for hgv delivery) values in both {@code _hgvType} and
	 * {@code  _hgvDestination} are set.
	 *
	 * @param tag
	 *          a String describing the access restriction
	 * @param flag
	 *          hgv type as defined in {@code HeavyVehicleAttributes}
	 */
	private void setFlagsFromTag (String tag, int flag) {
		if (tag != null) {
			if ("no".equals(tag))
				_hgvType |= flag;
			else if ("yes".equals(tag))
				_hgvType &= ~flag;
			else if ("destination".equals(tag) || (flag==HeavyVehicleAttributes.DELIVERY && "delivery".equals(tag))) {
				_hgvType |= flag;
				_hgvDestination |= flag;
			}
		}
	}

	private double parseDouble(String str) {
		double d;
		try {
			d = Double.parseDouble(str);
		} catch(NumberFormatException e) {
			d = 0.0;
		}
		return d;
	}

	@Override
	public String getName() {
		return "HeavyVehicle";
	}
}
