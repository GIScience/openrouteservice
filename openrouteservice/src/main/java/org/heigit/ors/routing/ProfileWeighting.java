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

package heigit.ors.routing;

import com.graphhopper.util.Helper;
import com.graphhopper.util.PMap;

import heigit.ors.exceptions.InternalServerException;
import heigit.ors.util.StringUtility;

public class ProfileWeighting {
	private String _name;
	private PMap _params;

	public ProfileWeighting(String name) throws InternalServerException
	{
		if (Helper.isEmpty(name))
			throw new InternalServerException(RoutingErrorCodes.EMPTY_ELEMENT, "'name' can't be null or empty");

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
