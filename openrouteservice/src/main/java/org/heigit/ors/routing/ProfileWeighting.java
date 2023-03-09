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

package org.heigit.ors.routing;

import com.graphhopper.util.Helper;
import com.graphhopper.util.PMap;
import org.heigit.ors.exceptions.InternalServerException;
import org.heigit.ors.util.StringUtility;

public class ProfileWeighting {
	private final String name;
	private PMap params;

	public ProfileWeighting(String name) throws InternalServerException {
		if (Helper.isEmpty(name))
			throw new InternalServerException(RoutingErrorCodes.EMPTY_ELEMENT, "'name' can't be null or empty");
		this.name =  name;
	}

	public String getName()
	{
		return name;
	}

	public void addParameter(String name, Object value) {
		getParameters().putObject(name, value);
	}

	public PMap getParameters()
	{
		if (params == null)
			params = new PMap();
		return params;
	}

	public static String encodeName(String name)
	{
		return "weighting_#" + name + "#";
	}

	public static String decodeName(String value) {
		if (value.startsWith("weighting_#"))
			return StringUtility.substring(value, '#');
		else
			return null;
	}
}
