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

		try
		{
			String[] array = strArray.split(",");
			int[] res = new int[array.length];
			for (int i = 0; i < array.length; i++)
				res [i] = Integer.parseInt(array[i].trim());

			return res;
		}
		catch(Exception ex)
		{
			throw new ParameterValueException(errorCode, "Unable to parse the element '" + elemName + "'. " + ex.getMessage());
		}
	}
}
