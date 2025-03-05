package org.heigit.ors.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.ConsoleAppender;

class ProgressBarLoggerTest {
    private Logger logger;
    private ch.qos.logback.classic.Logger logbackLogger;

    @BeforeEach
    @SuppressWarnings("unused")
    void setUp() {
        logger = ProgressBarLogger.getLogger();
        logbackLogger = (ch.qos.logback.classic.Logger) logger;
    }

    @Test
    void testLoggerInitialization() {
        assertNotNull(logger, "Logger should not be null");
    }

    @Test
    void testLoggerClass() {
        assertTrue(logger instanceof Logger, "Logger should be instance of SLF4J Logger");
    }

    @Test
    void testLoggerName() {
        assertEquals("ProgressBarLogger", logger.getName(), "Logger name should match");
    }

    @Test
    void testConsoleAppender() {
        ConsoleAppender<ILoggingEvent> appender = (ConsoleAppender<ILoggingEvent>) logbackLogger
                .getAppender("ProgressBarLogger");
        assertNotNull(appender, "Appender should not be null");
        assertEquals("ProgressBarLogger", appender.getName(), "Appender name should match");
        assertTrue(appender.isStarted(), "Appender should be started");
    }

    @Test
    void testLoggerSingleton() {
        Logger secondLogger = ProgressBarLogger.getLogger();
        assertSame(logger, secondLogger, "Should return the same logger instance");
    }
}