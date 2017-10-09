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
