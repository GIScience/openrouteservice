package org.heigit.ors.util;

import org.apache.log4j.Logger;

/**
 * A logger that logs progress percentage at specified steps.
 */
public class ProgressPercentageLogger {
    final private Logger LOGGER;
    final private int maxIterations;
    private int alreadyLoggedPercentage = -1;
    final private String message;
    final private int steps;

    public ProgressPercentageLogger(int maxIterations, String message, Logger logger){
        this(maxIterations, message, logger, 10);
    }
    public ProgressPercentageLogger(int maxIterations, String message, Logger logger, int steps) {
        this.maxIterations = maxIterations;
        this.message = message;
        this.LOGGER = logger;
        this.steps = steps;
    }

    public void update(int iteration){
        int percentage = (int) ((double) iteration / maxIterations * 100);
        if (percentage % steps == 0) {
            if (percentage > alreadyLoggedPercentage) {
                alreadyLoggedPercentage = percentage;
                LOGGER.info(message + percentage + "%");
            }
        }
    }

}
