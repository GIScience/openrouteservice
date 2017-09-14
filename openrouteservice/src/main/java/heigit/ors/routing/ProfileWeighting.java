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

import com.graphhopper.util.Helper;
import com.graphhopper.util.PMap;

import heigit.ors.util.StringUtility;

public class ProfileWeighting {
	private String _name;
	private PMap _params;

	public ProfileWeighting(String name) throws Exception
	{
		if (Helper.isEmpty(name))
			throw new Exception("'name' cann't be null or empty");

		_name =  name;
	}

	public String getName()
	{
		return _name;
	}

	public void addParameter(String name, String value)
	{
		if (_params == null)
			_params = new PMap();

		_params.put(name, value);
	}

	public PMap getParameters()
	{
		return _params;
	}
	public static String encodeName(String name)
	{
		return "weighting_#" + name + "#";
	}

	public static String decodeName(String value)
	{
		if (value.startsWith("weighting_#"))
			return StringUtility.substring(value, '#');
		else
			return null;
	}
}
