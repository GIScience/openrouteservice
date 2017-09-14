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
package heigit.ors.locations;

public class LocationsSearchFilter {
	private int[] _category_ids = null;
	private int[] _category_group_ids = null;
	private String _name = null;
	private String _wheelchair;
	private String _smoking;
	private Boolean _fee;
	//private Boolean _opennow; TODO

	public LocationsSearchFilter()
	{

	}

	public int[] getCategoryIds() {
		return _category_ids;
	}

	public void setCategoryIds(int[] values) {
		_category_ids = values;
	}

	public int[] getCategoryGroupIds() {
		return _category_group_ids;
	}

	public void setCategoryGroupIds(int[] values) {
		_category_group_ids = values;
	}

	public String getName() {
		return _name;
	}

	public void setName(String value) {
		_name = value;
	}

	public String getWheelchair() {
		return _wheelchair;
	}

	public void setWheelchair(String value) {
		_wheelchair = value;
	}

	public String getSmoking() {
		return _smoking;
	}

	public void setSmoking(String value) {
		_smoking = value;
	}

	public Boolean getFee() {
		return _fee;
	}

	public void setFee(Boolean value) {
		_fee = value;
	}
	
	public LocationsSearchFilter clone()
	{
		LocationsSearchFilter filter = new LocationsSearchFilter();
		filter._category_group_ids = _category_group_ids;
		filter._category_ids = _category_ids;
	    filter._fee = _fee;
	    filter._name = _name;
	    filter._smoking = _smoking;
	    filter._wheelchair = _wheelchair;
	    
	    return filter;
	}
}
