package org.freeopenls.tools;

import org.apache.log4j.Logger;

public class MemoryUtility {

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
