package org.heigit.ors.util;

import org.apache.log4j.Logger;

public class ErrorLoggingUtility {
    private ErrorLoggingUtility() {}

    public static void logMissingConfigParameter(Class originalClass, String missingConfigParameter) {
        final Logger logger = Logger.getLogger(originalClass.getName());
        logger.error("Missing config parameter " + missingConfigParameter);
    }

    public static void logMissingConfigParameter(Class originalClass, String missingConfigParameter, String message) {
        final Logger logger = Logger.getLogger(originalClass.getName());
        logger.error("The config parameter " + missingConfigParameter + " raised an error with the following message: " + message);
    }
}
