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
import java.util.HashSet;
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
import heigit.ors.routing.graphhopper.extensions.storages.EmergencyVehicleAttributesGraphStorage;

public class EmergencyVehicleGraphStorageBuilder extends AbstractGraphStorageBuilder {
    private boolean _includeRestrictions = true;
    private EmergencyVehicleAttributesGraphStorage _storage;
    private int _hgvType = 0;
    private boolean _hasRestrictionValues;
    private double[] _restrictionValues = new double[VehicleDimensionRestrictions.Count];
    private List<String> _motorVehicleRestrictions = new ArrayList<String>(5);
    private Set<String> _motorVehicleRestrictedValues = new HashSet<String>(5);
    private Pattern _patternHeight;

    public EmergencyVehicleGraphStorageBuilder() {
        // _motorVehicleRestrictions.addAll(Arrays.asList("motorcar", "motor_vehicle", "vehicle", "access"));

        // _motorVehicleRestrictedValues.add("private");
        // _motorVehicleRestrictedValues.add("no");
        // _motorVehicleRestrictedValues.add("restricted");
        // _motorVehicleRestrictedValues.add("military");

        //_patternHeight = Pattern.compile("(?:\\s*(\\d+)\\s*(?:feet|ft\\.|ft|'))?(?:(\\d+)\\s*(?:inches|in\\.|in|''|\"))?");
        _patternHeight = Pattern.compile("(?:\\s*(.*?)\\s*(?:feet|ft\\.|ft|'))?(?:\\s*(.*?)\\s*(?:inches|in\\.|in|''|\"))?");
    }

    public GraphExtension init(GraphHopper graphhopper) throws Exception {
        if (_storage != null)
            throw new Exception("GraphStorageBuilder has been already initialized.");

        if (_parameters != null) {
            String value = _parameters.get("restrictions");
            if (!Helper.isEmpty(value))
                _includeRestrictions = Boolean.parseBoolean(value);
        }

        _storage = new EmergencyVehicleAttributesGraphStorage(_includeRestrictions);

        return _storage;
    }

    @Override
    public void processWay(ReaderWay way) {

        _hgvType = 0;

        if (_hasRestrictionValues) {
            _restrictionValues[0] = 0.0;
            _restrictionValues[1] = 0.0;
            _restrictionValues[2] = 0.0;
            _restrictionValues[3] = 0.0;
            _restrictionValues[4] = 0.0;

            _hasRestrictionValues = false;
        }

        boolean hasHighway = way.hasTag("highway");
        if (hasHighway && way.hasTag(_motorVehicleRestrictions, _motorVehicleRestrictedValues)) {// set to 0
            _hgvType |= 0;
            // _hgvType |= HeavyVehicleAttributes.BUS;
            // _hgvType |= HeavyVehicleAttributes.AGRICULTURE;
            // _hgvType |= HeavyVehicleAttributes.FORESTRY;
            // _hgvType |= HeavyVehicleAttributes.DELIVERY;
            // _hgvType |= HeavyVehicleAttributes.GOODS;
            // _hgvType |= HeavyVehicleAttributes.HGV;
        }

        java.util.Iterator<Entry<String, Object>> it = way.getProperties();

        while (it.hasNext()) {
            Map.Entry<String, Object> pairs = it.next();
            String key = pairs.getKey();
            String value = pairs.getValue().toString();

            if (hasHighway) {
                if (key.equals("highway")) {
                    if (value.equals("motorway") || value.equals("motorway_link")) {
                    } else if (value.equals("steps")) {
                    } else if ("track".equals(value)) {
                        String tracktype = way.getTag("tracktype");
                        if (tracktype != null && (
                                tracktype.equals("grade1") || tracktype.equals("grade2") ||
                                tracktype.equals("grade3") || tracktype.equals("grade4") ||
                                tracktype.equals("grade5"))
                        ) {
                            _hgvType |= HeavyVehicleAttributes.AGRICULTURE;
                            _hgvType |= HeavyVehicleAttributes.FORESTRY;
                        }
                    }

                }
                /*
                 * todo borders
                 */
                else {
                    if (_includeRestrictions) {
                        int valueIndex = -1;

                        if (key.equals("maxheight")) {
                            valueIndex = VehicleDimensionRestrictions.MaxHeight;
                        } else if (key.equals("maxweight")) {
                            valueIndex = VehicleDimensionRestrictions.MaxWeight;
                        } else if (key.equals("maxweight:hgv")) {
                            valueIndex = VehicleDimensionRestrictions.MaxWeight;
                        } else if (key.equals("maxwidth")) {
                            valueIndex = VehicleDimensionRestrictions.MaxWidth;
                        } else if (key.equals("maxlength")) {
                            valueIndex = VehicleDimensionRestrictions.MaxLength;
                        } else if (key.equals("maxlength:hgv")) {
                            valueIndex = VehicleDimensionRestrictions.MaxLength;
                        } else if (key.equals("maxaxleload")) {
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
                                } else /*if (value.contains("'"))*/{
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


                    // String hgvTag = getHeavyVehicleValue(key, "hgv", value); //key.equals("hgv") ? value : null;
                    // String goodsTag = getHeavyVehicleValue(key, "goods", value); // key.equals("goods") ? value : null;
                    // String busTag = getHeavyVehicleValue(key, "bus", value); //key.equals("bus") ? value : null;
                    // String agriculturalTag = getHeavyVehicleValue(key, "agricultural", value); //key.equals("agricultural") ? value : null;
                    // String forestryTag = getHeavyVehicleValue(key, "forestry", value); // key.equals("forestry") ? value : null;
                    // String deliveryTag = getHeavyVehicleValue(key, "delivery", value); //key.equals("delivery") ? value : null;

                    // String accessTag = key.equals("access") ? value : null;

                    // if (Helper.isEmpty(accessTag)) {
                    // 	if ("agricultural".equals(accessTag))
                    // 		agriculturalTag = "yes";
                    // 	else if ("forestry".equals(accessTag))
                    // 		forestryTag = "yes";
                    // 	else if ("bus".equals(accessTag))
                    // 		busTag = "yes";
                    // }

                    // String motorVehicle = key.equals("motor_vehicle") ? value : null;
                    // if (motorVehicle == null)
                    // 	motorVehicle = key.equals("motorcar") ? value : null;

                    // if (motorVehicle != null) {
                    // 	if ("agricultural".equals(motorVehicle))
                    // 		agriculturalTag = "yes";
                    // 	else if ("forestry".equals(motorVehicle))
                    // 		forestryTag = "yes";
                    // 	else if ("delivery".equals(motorVehicle))
                    // 		deliveryTag = "yes";

                    // 	//if ("destination".equals(motorVehicle))
                    // 	//	heavyVehicleFlag |= HeavyVehicleAttributes.Destination;
                    // }

                    // if (goodsTag != null) {
                    // 	if ("no".equals(goodsTag))
                    // 		_hgvType |= HeavyVehicleAttributes.GOODS;
                    // 	else if ("yes".equals(busTag))
                    // 		_hgvType &= ~HeavyVehicleAttributes.GOODS;
                    // 	else if ("destination".equals(goodsTag))
                    // 	{
                    // 		_hgvType |= HeavyVehicleAttributes.GOODS;
                    // 		_hgvDestination |= HeavyVehicleAttributes.GOODS;//(1 << (HeavyVehicleAttributes.Goods >> 1));
                    // 	}
                    // }

                    // if (hgvTag != null) {
                    // 	if ("no".equals(hgvTag))
                    // 		_hgvType |= HeavyVehicleAttributes.HGV;
                    // 	else if ("yes".equals(busTag))
                    // 		_hgvType &= ~HeavyVehicleAttributes.HGV;
                    // 	else if ("destination".equals(hgvTag))
                    // 	{
                    // 		_hgvType |= HeavyVehicleAttributes.HGV;
                    // 		_hgvDestination |= HeavyVehicleAttributes.HGV;// (1 << (HeavyVehicleAttributes.Hgv >> 1));
                    // 	}
                    // }

                    // if (busTag != null) {
                    // 	if ("no".equals(busTag))
                    // 		_hgvType |= HeavyVehicleAttributes.BUS;
                    // 	else if ("yes".equals(busTag))
                    // 		_hgvType &= ~HeavyVehicleAttributes.BUS;
                    // 	else if ("destination".equals(busTag))
                    // 	{
                    // 		_hgvType |= HeavyVehicleAttributes.BUS;
                    // 		_hgvDestination |= HeavyVehicleAttributes.BUS; //(1 << (HeavyVehicleAttributes.Bus >> 1));
                    // 	}
                    // }

                    // if (agriculturalTag != null) {
                    // 	if ("no".equals(agriculturalTag))
                    // 		_hgvType |= HeavyVehicleAttributes.AGRICULTURE;
                    // 	else if ("yes".equals(busTag))
                    // 		_hgvType &= ~HeavyVehicleAttributes.AGRICULTURE;
                    // 	else if ("destination".equals(agriculturalTag))
                    // 	{
                    // 		_hgvType |= HeavyVehicleAttributes.AGRICULTURE;
                    // 		_hgvDestination |= HeavyVehicleAttributes.AGRICULTURE;// (1 << (HeavyVehicleAttributes.Agricultural >> 1));
                    // 	}
                    // } else

                    // if (forestryTag != null) {
                    // 	if ("no".equals(forestryTag))
                    // 		_hgvType |= HeavyVehicleAttributes.FORESTRY;
                    // 	else if ("yes".equals(busTag))
                    // 		_hgvType &= ~HeavyVehicleAttributes.FORESTRY;
                    // 	else if ("destination".equals(forestryTag))
                    // 	{
                    // 		_hgvType |= HeavyVehicleAttributes.FORESTRY;
                    // 		_hgvDestination |= HeavyVehicleAttributes.FORESTRY;//(1 << (HeavyVehicleAttributes.Forestry >> 1));
                    // 	}
                    // }

                    // if (deliveryTag != null) {
                    // 	if ("no".equals(deliveryTag))
                    // 		_hgvType |= HeavyVehicleAttributes.DELIVERY;
                    // 	else if ("yes".equals(busTag))
                    // 		_hgvType &= ~HeavyVehicleAttributes.DELIVERY;
                    // 	else if ("destination".equals(deliveryTag) || "delivery".equals(deliveryTag) )
                    // 	{
                    // 		_hgvType |= HeavyVehicleAttributes.DELIVERY;
                    // 		_hgvDestination |= HeavyVehicleAttributes.DELIVERY; //(1 << (HeavyVehicleAttributes.Delivery >> 1));
                    // 	}
                    // }

                    // String hazmatTag = key.equals("hazmat") ? value : null;
                    // if ("no".equals(hazmatTag)) {
                    // 	_hgvType |= HeavyVehicleAttributes.HAZMAT;
                    // }

                    // (access=no) + access:conditional=delivery @
                    // (07:00-11:00); customer @ (07:00-17:00)
                }
            }
        }
    }

    @Override
    public void processEdge(ReaderWay way, EdgeIteratorState edge) {
        //if (_hgvType > HeavyVehicleAttributes.UNKNOWN || _hgvDestination > 0 || _hasRestrictionValues)
        {
            if (_hasRestrictionValues)
                _storage.setEdgeValue(edge.getEdge(), _hgvType, 0, _restrictionValues);
            else
                _storage.setEdgeValue(edge.getEdge(), _hgvType, 0, null);
        }
    }

    @Override
    public String getName() {
        return "EmergencyVehicle";
    }
}
