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

import com.graphhopper.util.Helper;

import heigit.ors.exceptions.UnknownParameterValueException;

public class LocationDetailsType
{
	public static final int NONE = 0;
	public static final int ADDRESS = 1;
	public static final int CONTACT = 2;
	public static final int ATTRIBUTES = 4;

	public static boolean isSet(int details, int value)
	{
		return (details & value) == value;
	}

	public static int fromString(String value) throws UnknownParameterValueException {
		if (Helper.isEmpty(value))
			return NONE;

		int res = NONE;

		String[] values = value.split("\\|");
		for (int i = 0; i < values.length; ++i) {
			switch (values[i].toLowerCase()) {
			case "address":
				res |= ADDRESS;
				break;
			case "contact":
				res |= CONTACT;
				break;
			case "attributes":
				res |= ATTRIBUTES;
				break;
			}
		}

		return res;
	}
}
