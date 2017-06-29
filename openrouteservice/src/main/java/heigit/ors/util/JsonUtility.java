package heigit.ors.util;

import org.json.JSONArray;

import heigit.ors.exceptions.ParameterValueException;

public class JsonUtility {

	public static int[] parseIntArray(JSONArray array, String elemName, int errorCode) throws Exception
	{
		if (array.length() <= 0)
			return null;

		try
		{
			int[] res = new int[array.length()];
			for (int i = 0; i < array.length(); i++)
				res [i] = array.getInt(i);
			return res;
		}
		catch(Exception ex)
		{
			throw new ParameterValueException(errorCode, "Unable to parse the element '" + elemName + "'. " + ex.getMessage());
		}
	}
}
