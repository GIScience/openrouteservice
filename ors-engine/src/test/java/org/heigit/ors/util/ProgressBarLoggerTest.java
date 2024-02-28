package org.heigit.ors.util;

import org.apache.log4j.Appender;
import org.apache.log4j.Logger;
import org.apache.logging.log4j.core.config.AppenderRef;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.apache.logging.log4j.Level;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ProgressBarLoggerTest {

    private Logger logger;

    @BeforeEach
    public void setUp() {
        logger = ProgressBarLogger.getLogger();
    }

    @Test
    public void testLoggerInitialization() {
        assertNotNull(logger, "Logger should not be null");
    }

    @Test
    public void testLoggerClass() {
        assertEquals(Logger.class, logger.getClass(), "Logger should be of class 'org.apache.log4j.Logger'");
    }

    @Test
    public void testLoggerName() {
        assertEquals(ProgressBarLogger.getLoggerName(), logger.getName(), "Logger name should be 'ProgressBar'");
    }

    @Test
    public void testLoggerLevel() {
        assertEquals(Level.ERROR.toString(), logger.getLevel().toString(), "Logger level should be 'ERROR' in testing");
    }

    @Test
    public void testGetLogger() {
        assertEquals(Logger.getLogger("ProgressBarLogger"), logger, "Logger should be the same instance");
    }

    @Test
    public void testConsoleAppender() {
        Appender appenderRefs = ProgressBarLogger.getLogger().getAppender("ProgressBarLogger");
        assertNotNull(appenderRefs, "Appender should not be null");
        assertEquals(ProgressBarLogger.getLoggerName(), appenderRefs.getName(), "Appender name should be 'ProgressBarLogger'");
    }
}