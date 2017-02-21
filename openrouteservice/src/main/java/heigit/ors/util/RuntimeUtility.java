/*|----------------------------------------------------------------------------------------------
 *|														Heidelberg University
 *|	  _____ _____  _____      _                     	Department of Geography		
 *|	 / ____|_   _|/ ____|    (_)                    	Chair of GIScience
 *|	| |  __  | | | (___   ___ _  ___ _ __   ___ ___ 	(C) 2014-2016
 *|	| | |_ | | |  \___ \ / __| |/ _ \ '_ \ / __/ _ \	
 *|	| |__| |_| |_ ____) | (__| |  __/ | | | (_|  __/	Berliner Strasse 48								
 *|	 \_____|_____|_____/ \___|_|\___|_| |_|\___\___|	D-69120 Heidelberg, Germany	
 *|	        	                                       	http://www.giscience.uni-hd.de
 *|								
 *|----------------------------------------------------------------------------------------------*/
package heigit.ors.util;

import org.apache.log4j.Logger;

public class RuntimeUtility {

	public static void clearMemory(Logger logger) {
		printRAMInfo(logger);
		Runtime.getRuntime().gc();
		printRAMInfo(logger);
	}

	public static void printRAMInfo(Logger logger) {
		logger.info("RAM STATS: TotalMemory: " + getMemorySize(Runtime.getRuntime().totalMemory()) + ", FreeMemory: "
				+ getMemorySize(Runtime.getRuntime().freeMemory()) + ", MaxMemory: " + getMemorySize(Runtime.getRuntime().maxMemory())
				+ "  --> UsedMemory: "
				+ getMemorySize(Runtime.getRuntime().totalMemory() -(Runtime.getRuntime().freeMemory())));
	}
	
	private static String getMemorySize(long value)
	{
		double gigaBytes = FormatUtility.roundToDecimals(value * 9.31322574615479e-10, 3);
		return gigaBytes + " GB";
	}
}
