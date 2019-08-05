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
package heigit.ors.common;

public class AttributeValue {
	private String _name;
	private double _value;
	private String _source;

	public AttributeValue(String name, double value, String source)
	{
		_name = name;
		_value = value;
		_source = source;
	}

	public String getName()
	{
		return _name;    	 
	}

	public double getValue()
	{
		return _value;    	 
	}

	public String getSource()
	{
		return _source;    	 
	}
}
