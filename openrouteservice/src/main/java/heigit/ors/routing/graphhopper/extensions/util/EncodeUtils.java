/*|----------------------------------------------------------------------------------------------
 *|														Heidelberg University
 *|	  _____ _____  _____      _                     	Department of Geography		
 *|	 / ____|_   _|/ ____|    (_)                    	Chair of GIScience
 *|	| |  __  | | | (___   ___ _  ___ _ __   ___ ___ 	(C) 2014
 *|	| | |_ | | |  \___ \ / __| |/ _ \ '_ \ / __/ _ \	
 *|	| |__| |_| |_ ____) | (__| |  __/ | | | (_|  __/	Berliner Strasse 48								
 *|	 \_____|_____|_____/ \___|_|\___|_| |_|\___\___|	D-69120 Heidelberg, Germany	
 *|	        	                                       	http://www.giscience.uni-hd.de
 *|								
 *|----------------------------------------------------------------------------------------------*/

// Authors: M. Rylov

package heigit.ors.routing.graphhopper.extensions.util;

public class EncodeUtils {

	public static double getValue(int restValue, int index) {
		if (restValue == 0)
			return 0.0;

		int decodedValue = 0;
		if (index == 0)
			decodedValue = restValue >> 24;
		else if (index == 1)
			decodedValue = (restValue & 0x00FF0000) >> 16;
		else if (index == 2)
			decodedValue = (restValue & 0x0000FF00) >> 8;
		else if (index == 3)
			decodedValue = (restValue & 0x000000FF);

		return (double) decodedValue / 10d;
	}

	/**
	 * Encodes 4 double values contained in a double array into one integer
	 * variable. <li>Allow range of values for <code>value[0]</code>: -12.8d <=
	 * <code>value[0]</code> < 12.7d</li> <li>Allow range of values for
	 * <code>value[1]</code>: 0d <= <code>value[0]</code> < 25.5d</li> <li>Allow
	 * range of values for <code>value[2]</code>: 0d <= <code>value[0]</code> <
	 * 25.5d</li> <li>Allow range of values for <code>value[3]</code>: 0d <=
	 * <code>value[0]</code> < 25.5d</li>
	 * 
	 * @param values
	 *            the array containing 4 double values
	 * @return the integer containing the encoded 4 double values
	 */
	public static int setValue(double[] values) {
		int value = (int) ((((int) (values[0] * 10) & 0xFF) << 24) | (((int) (values[1] * 10) & 0xFF) << 16)
				| (((int) (values[2] * 10) & 0xFF) << 8) | ((int) (values[3] * 10) & 0xFF));

		return value;
	}

	public static void main(String[] args) {

		double[] test = new double[4];
		test[0] = 12.7d;
		test[1] = 0d;
		test[2] = 3.5d;
		test[3] = 25.5d;

		int value = setValue(test);

		System.out.println("RoadRestrictionEncoder.main() 0=" + getValue(value, 0) + ", 1=" + getValue(value, 1)
				+ ", 2=" + getValue(value, 2) + ", 3=" + getValue(value, 3));
	}
}
