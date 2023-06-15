/*
 *  Licensed to GIScience Research Group, Heidelberg University (GIScience)
 *
 *   	http://www.giscience.uni-hd.de
 *   	http://www.heigit.org
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
package org.heigit.ors.isochrones.statistics;

import java.util.List;
import java.util.Map;

public class StatisticsProviderConfiguration {
	private final int id;
	private final String name;
	private final Map<String, Object> parameters;
	private final Map<String, String> mapping;
	private final String attribution;
	
	public StatisticsProviderConfiguration(int id, String name, Map<String, Object> parameters, Map<String, String> mapping, String attribution) {
		this.id = id;
		this.name = name;
		this.parameters = parameters;
		this.mapping = mapping;
		this.attribution = attribution;
	}
	
	@Override
	public int hashCode()
	{
		return id;
	}

	@Override
	public boolean equals(Object obj) {
		return obj != null && obj.getClass() == this.getClass() && id == ((StatisticsProviderConfiguration) obj).id;
	}

	public int getId()
	{
		return id;
	}
	
	public String getName() {
		return name;
	}

	public String getAttribution() {
		return attribution;
	}

	public Map<String, Object> getParameters() {
		return parameters;
	}
	
	public String[] getMappedProperties(List<String> props) {
		String[] res = new String[props.size()];
		for (int i = 0; i < props.size(); i++) {
			res[i] = mapping.get(props.get(i));
		}
		return res;
	}
}
