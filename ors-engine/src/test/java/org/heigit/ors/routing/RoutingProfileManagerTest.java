package org.heigit.ors.routing;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Property;
import org.heigit.ors.common.DataAccessEnum;
import org.heigit.ors.common.EncoderNameEnum;
import org.heigit.ors.config.EngineProperties;
import org.heigit.ors.config.profile.ProfileProperties;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.CleanupMode;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.junit.jupiter.api.Assertions.*;

class RoutingProfileManagerTest {
    private static final String INIT_FAILED = "Exception at RoutingProfileManager initialization: ";

    @TempDir(cleanup = CleanupMode.ALWAYS)
    Path tempDir;

    private final CapturingAppender appender = new CapturingAppender();
    private final Logger logger = (Logger) LogManager.getLogger(RoutingProfileManager.class.getName());
    private Level originalLevel;

    @BeforeEach
    void captureLogs() {
        originalLevel = logger.getLevel();
        appender.start();
        logger.addAppender(appender);
    }

    @AfterEach
    void stopCapturingLogs() {
        logger.removeAppender(appender);
        appender.stop();
        logger.setLevel(originalLevel);
        // A failed initialization interrupts the calling thread; don't leak that into other tests.
        Thread.interrupted();
    }

    @Test
    void failedProfileLogsRootCauseOnItsOwnLine() throws IOException {
        logger.setLevel(Level.INFO);

        RoutingProfileManager manager = initializeWithCorruptOsmFile();

        assertTrue(manager.hasFailed(), "A profile that cannot be built should fail the initialization");
        List<String> errorLines = errorEvents().stream().map(event -> event.getMessage().getFormattedMessage()).toList();
        int failureLine = indexOfLineStartingWith(errorLines, INIT_FAILED);
        assertTrue(errorLines.get(failureLine).contains("Could not parse OSM file"), "The message should keep the wrapping exception: " + errorLines);
        assertTrue(errorLines.get(failureLine + 1).startsWith("Root cause: IllegalArgumentException: Input file is not of valid type"),
                "The root cause should follow on its own line: " + errorLines);
        assertTrue(appender.events.stream().allMatch(event -> event.getThrown() == null), "Without DEBUG no stack trace should be logged");
    }

    @Test
    void failedProfileLogsStackTraceOnDebug() throws IOException {
        logger.setLevel(Level.DEBUG);

        initializeWithCorruptOsmFile();

        LogEvent stackTrace = appender.events.stream()
                .filter(event -> event.getThrown() != null)
                .findFirst()
                .orElseThrow(() -> new AssertionError("No stack trace was logged"));
        assertEquals(Level.DEBUG, stackTrace.getLevel(), "The stack trace should be logged on DEBUG");
        assertEquals(IllegalArgumentException.class, ExceptionUtils.getRootCause(stackTrace.getThrown()).getClass(),
                "The logged exception should contain the root cause");
    }

    @Test
    void failureWithoutExceptionLogsOnlyTheMessage() {
        logger.setLevel(Level.DEBUG);
        RoutingProfileManager manager = new RoutingProfileManager(new EngineProperties());

        manager.initialize();

        assertTrue(manager.hasFailed(), "Initialization without profiles should fail");
        List<String> errorLines = errorEvents().stream().map(event -> event.getMessage().getFormattedMessage()).toList();
        assertTrue(errorLines.contains("No profiles configured. Exiting."), "The failure should be logged: " + errorLines);
        assertTrue(errorLines.stream().noneMatch(line -> line.startsWith("Root cause:")), "There is no cause to name: " + errorLines);
        assertTrue(appender.events.stream().allMatch(event -> event.getThrown() == null), "There is no exception to log");
    }

    private RoutingProfileManager initializeWithCorruptOsmFile() throws IOException {
        Path corruptOsmFile = Files.write(tempDir.resolve("corrupt.osm.pbf"), new byte[]{0, 0, 0, 42, 1, 2, 3});
        RoutingProfileManager manager = new RoutingProfileManager(engineProperties(corruptOsmFile));
        manager.initialize();
        return manager;
    }

    private static int indexOfLineStartingWith(List<String> lines, String prefix) {
        for (int i = 0; i < lines.size(); i++) {
            if (lines.get(i).startsWith(prefix)) {
                return i;
            }
        }
        throw new AssertionError("No line starting with '" + prefix + "' in " + lines);
    }

    private EngineProperties engineProperties(Path osmFile) {
        EngineProperties engineProperties = new EngineProperties();
        engineProperties.setInitThreads(1);
        engineProperties.setGraphsDataAccess(DataAccessEnum.RAM_STORE);
        engineProperties.getElevation().setPreprocessed(false);
        engineProperties.getProfileDefault().setGraphPath(tempDir.resolve("graphs"));
        engineProperties.getProfileDefault().getBuild().setSourceFile(osmFile);
        ProfileProperties car = new ProfileProperties();
        car.setEnabled(true);
        car.setEncoderName(EncoderNameEnum.DRIVING_CAR);
        engineProperties.getProfiles().put("driving-car", car);
        return engineProperties;
    }

    private List<LogEvent> errorEvents() {
        return appender.events.stream().filter(event -> event.getLevel() == Level.ERROR).toList();
    }

    private static class CapturingAppender extends AbstractAppender {
        private final List<LogEvent> events = new CopyOnWriteArrayList<>();

        CapturingAppender() {
            super("RoutingProfileManagerTestAppender", null, null, true, Property.EMPTY_ARRAY);
        }

        @Override
        public void append(LogEvent event) {
            events.add(event.toImmutable());
        }
    }
}
