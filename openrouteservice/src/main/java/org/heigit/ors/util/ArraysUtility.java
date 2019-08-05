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
package heigit.ors.util;

import com.graphhopper.util.Helper;

import heigit.ors.exceptions.ParameterValueException;

public class ArraysUtility {
	public static String toString(int[] a, String separator) {
		if (a == null)
			return null;
		int iMax = a.length - 1;
		if (iMax == -1)
			return "";

		StringBuilder b = new StringBuilder();

		for (int i = 0; ; i++) {
			b.append(a[i]);
			if (i == iMax)
				return b.toString();

			b.append(separator);
		}
	}

	public static int[] parseIntArray(String strArray, String elemName, int errorCode) throws Exception
	{
		if (Helper.isEmpty(strArray))
			return null;

		String value = null;
		
		try
		{
			String[] array = strArray.split(",");
			int[] res = new int[array.length];
			for (int i = 0; i < array.length; i++)
			{
				value = array[i].trim();
				res[i] = Integer.parseInt(value);
			}

			return res;
		}
		catch(Exception ex)
		{
			throw new ParameterValueException(errorCode, elemName, value);
		}
	}
	
	public static double[] parseDoubleArray(String strArray, String elemName, String separator, int errorCode) throws Exception
	{
		if (Helper.isEmpty(strArray))
			return null;

		String value = null;
		
		try
		{
			String[] array = strArray.split(separator);
			double[] res = new double[array.length];
			for (int i = 0; i < array.length; i++)
			{
				value = array[i].trim();
				res[i] = Double.parseDouble(value);
			}

			return res;
		}
		catch(Exception ex)
		{
			throw new ParameterValueException(errorCode, elemName, value);
		}
	}
}
