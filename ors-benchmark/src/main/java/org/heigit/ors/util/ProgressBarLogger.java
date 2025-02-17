package org.heigit.ors.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.core.ConsoleAppender;

public class ProgressBarLogger {
    private static final String LOGGER_NAME = "ProgressBarLogger";
    private static Logger logger;

    private static Logger initializeLogger() {
        if (logger == null) {
            LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
            
            // Create a console appender
            ConsoleAppender<ch.qos.logback.classic.spi.ILoggingEvent> appender = new ConsoleAppender<>();
            appender.setContext(context);
            appender.setName(LOGGER_NAME);

            // Create and set the encoder
            PatternLayoutEncoder encoder = new PatternLayoutEncoder();
            encoder.setContext(context);
            // Simplified pattern: only shows message with carriage return
            encoder.setPattern("%msg\r");
            encoder.start();

            appender.setEncoder(encoder);
            appender.start();

            // Get the logger and set the appender
            ch.qos.logback.classic.Logger logbackLogger = context.getLogger(LOGGER_NAME);
            logbackLogger.setAdditive(false);
            logbackLogger.addAppender(appender);

            logger = LoggerFactory.getLogger(LOGGER_NAME);
        }
        return logger;
    }

    public static Logger getLogger() {
        return initializeLogger();
    }
}
