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
package heigit.ors.routing;

public class RouteSegmentItem {
	private int _from;
	private int _to;
	private long _value;
	private double _distance;

	public RouteSegmentItem(int from, int to, long value, double distance)
	{
		_from = from;
		_to = to;
		_value  = value;
		_distance = distance;
	}

	public int getFrom() {
		return _from;
	}

	public void setFrom(int from) {
		_from = from;
	}
	
	public int getTo() {
		return _to;
	}
	
	public void setTo(int to) {
		_to = to;
	}

	public long getValue() {
		return _value;
	}

	public void setValue(int value) {
		_value = value;
	}	
	
	public double getDistance()
	{
		return _distance;
	}
	
	public void setDistance(double value)
	{
		_distance = value;
	}
}
