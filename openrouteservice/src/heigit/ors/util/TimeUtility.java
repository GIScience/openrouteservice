package org.freeopenls.tools;

public final class TimeUtility {

	public static String getElapsedTime(long startTime, boolean addSeconds) {
		return getElapsedTime(startTime, System.currentTimeMillis(), addSeconds);
	}
	
	public static String getElapsedTime(long startTime, long endTime, boolean addSeconds) {
		long time = endTime - startTime;
		double handlingTimeSeconds = (double) time / 1000;

		String res = Double.toString(handlingTimeSeconds).replace(".", ",");

		if (addSeconds)
			res += "s";

		return res;
	}
}
