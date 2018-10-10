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
	private Pattern _patternHeight;

	public HeavyVehicleGraphStorageBuilder()
	{
		_motorVehicleRestrictions.addAll(Arrays.asList("motorcar", "motor_vehicle", "vehicle", "access"));

		_motorVehicleRestrictedValues.add("private");
		_motorVehicleRestrictedValues.add("no");
		_motorVehicleRestrictedValues.add("restricted");
		_motorVehicleRestrictedValues.add("military");
		
		_patternHeight = Pattern.compile("(?:\\s*(\\d+)\\s*(?:feet|ft\\.|ft|'))?(?:(\\d+)\\s*(?:inches|in\\.|in|''|\"))?");
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
			// restrict all types if there are any generic motor vehicle restrictions
			if (way.hasTag(_motorVehicleRestrictions, _motorVehicleRestrictedValues)) {
				_hgvType |= HeavyVehicleAttributes.BUS;
				_hgvType |= HeavyVehicleAttributes.AGRICULTURE;
				_hgvType |= HeavyVehicleAttributes.FORESTRY;
				_hgvType |= HeavyVehicleAttributes.DELIVERY;
				_hgvType |= HeavyVehicleAttributes.GOODS;
				_hgvType |= HeavyVehicleAttributes.HGV;
			}
			
			Iterator<Entry<String, Object>> it = way.getProperties();

			while (it.hasNext()) {
				Map.Entry<String, Object> pairs = it.next();
				String key = pairs.getKey();
				String value = pairs.getValue().toString();

				/*
				 * https://wiki.openstreetmap.org/wiki/Restrictions
				 */
				if (_includeRestrictions) {
					int valueIndex = -1;

					if (key.equals("maxheight")) {
						valueIndex = VehicleDimensionRestrictions.MaxHeight;
					} else if (key.equals("maxweight")) {
						valueIndex = VehicleDimensionRestrictions.MaxWeight;
					} else if (key.equals("maxweight:hgv")) {
						valueIndex = VehicleDimensionRestrictions.MaxWeight;
					}	else if (key.equals("maxwidth")) {
						valueIndex = VehicleDimensionRestrictions.MaxWidth;
					} else if (key.equals("maxlength")) {
						valueIndex = VehicleDimensionRestrictions.MaxLength;
					} else if (key.equals("maxlength:hgv")) {
						valueIndex = VehicleDimensionRestrictions.MaxLength;
					}
					else if (key.equals("maxaxleload")) {
						valueIndex = VehicleDimensionRestrictions.MaxAxleLoad;
					}

					if (valueIndex >= 0 && !("none".equals(value) || "default".equals(value))) {
						if (valueIndex == VehicleDimensionRestrictions.MaxWeight || valueIndex == VehicleDimensionRestrictions.MaxAxleLoad) {
							if (value.contains("t")) {
								value = value.replace('t', ' ');
							} else if (value.contains("lbs")) {
								value = value.replace("lbs", " ");
								value = Double.toString(Double.parseDouble(value) / 2204.622);
							}
						} else {
							if (value.contains("m")) {
								value = value.replace('m', ' ');
							} else /*if (value.contains("'")) */ {
                            // MARQ24: why the heck we only make use of our fancy RegEx for height
                            // parsing - IF the string contains a ' ?!
                            Matcher m = _patternHeight.matcher(value);
                            if (m.matches() && m.lookingAt()) {
                                double feet = Double.parseDouble(m.group(1));
                                double inches = 0;
                                if (m.groupCount() > 1 && m.group(2) != null) {
                                    inches = Double.parseDouble(m.group(2));
                                }
                                // MARQ24: n feet * 0.3048 = feet 2 meter - ok fine... BUT
                                // x inches * 0.0254 * y feet - this does not make much sense to me...
                                // double newValue = feet * 0.3048 + inches * 0.0254 * feet;
                                double newValue = feet * 0.3048 + inches * 0.0254;
                                value = Double.toString(newValue);
                            }
                        }
                    }

                    // MARQ24: FIXME: Here we can get a "NumberFormatException" and then the complete
                    // iterator will stop (no additional propertis of the way will be processed!
                    _restrictionValues[valueIndex] = Double.parseDouble(value);
                    _hasRestrictionValues = true;
					}
				}

				// TODO: the following implementation does not pick up access:destination
				String hgvTag = getHeavyVehicleValue(key, "hgv", value);
				String goodsTag = getHeavyVehicleValue(key, "goods", value);
				String busTag = getHeavyVehicleValue(key, "bus", value);
				String agriculturalTag = getHeavyVehicleValue(key, "agricultural", value);
				String forestryTag = getHeavyVehicleValue(key, "forestry", value);
				String deliveryTag = getHeavyVehicleValue(key, "delivery", value);

				String accessTag = key.equals("access") ? value : null;

				if (!Helper.isEmpty(accessTag)) {
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
				}

				setFlagsFromTag(goodsTag, HeavyVehicleAttributes.GOODS);
				setFlagsFromTag(hgvTag, HeavyVehicleAttributes.HGV);
				setFlagsFromTag(busTag, HeavyVehicleAttributes.BUS);
				setFlagsFromTag(agriculturalTag, HeavyVehicleAttributes.AGRICULTURE);
				setFlagsFromTag(forestryTag, HeavyVehicleAttributes.FORESTRY);
				setFlagsFromTag(deliveryTag, HeavyVehicleAttributes.DELIVERY);

				String hazmatTag = key.equals("hazmat") ? value : null;
				if ("no".equals(hazmatTag)) {
					_hgvType |= HeavyVehicleAttributes.HAZMAT;
				}

				// (access=no) + access:conditional=delivery @
				// (07:00-11:00); customer @ (07:00-17:00)
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
	
	@Override
	public String getName() {
		return "HeavyVehicle";
	}
}
