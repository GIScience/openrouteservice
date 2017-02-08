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

	/**
	 * Clear Garbage Collector and log some informations
	 * 
	 */
	public static void clearMemAndLogRAM(Logger logger) {
		logRAMInformations(logger);
		Runtime.getRuntime().gc();
		logRAMInformations(logger);
	}

	/**
	 * log some informations about the ram-usage
	 */
	public static void logRAMInformations(Logger logger) {
		logger.info("*  -> TotalMemory: " + Runtime.getRuntime().totalMemory() / 1000000 + " MB  FreeMemory: "
				+ Runtime.getRuntime().freeMemory() / 1000000 + " MB  MaxMemory: " + Runtime.getRuntime().maxMemory()
				/ 1000000 + " MB  --> UsedMemory: "
				+ ((Runtime.getRuntime().totalMemory() / 1000000) - (Runtime.getRuntime().freeMemory() / 1000000))
				+ " MB  <--");
	}
}
