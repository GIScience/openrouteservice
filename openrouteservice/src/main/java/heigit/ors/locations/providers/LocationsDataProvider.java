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
package heigit.ors.locations.providers;

import java.util.List;
import java.util.Map;

import heigit.ors.locations.LocationsCategory;
import heigit.ors.locations.LocationsRequest;
import heigit.ors.locations.LocationsResult;

public interface LocationsDataProvider 
{
	public void init(Map<String, Object> parameters) throws Exception;	
	
	public void close() throws Exception;
	
	public List<LocationsResult> findLocations(LocationsRequest request) throws Exception;
	
	public List<LocationsCategory> findCategories(LocationsRequest request) throws Exception;
	
	public String getName();
}
