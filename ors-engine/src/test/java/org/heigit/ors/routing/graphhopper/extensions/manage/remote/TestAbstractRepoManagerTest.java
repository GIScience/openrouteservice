package org.heigit.ors.routing.graphhopper.extensions.manage.remote;

import org.heigit.ors.routing.graphhopper.extensions.manage.GraphInfo;
import org.heigit.ors.routing.graphhopper.extensions.manage.PersistedGraphInfo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.CleanupMode;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.util.Date;
import java.util.stream.Stream;

import static org.heigit.ors.routing.graphhopper.extensions.manage.RepoManagerTestHelper.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestAbstractRepoManagerTest {

    @TempDir(cleanup = CleanupMode.ALWAYS)
    private static Path tempDir;

    TestAbstractRepoManager orsGraphRepoManager = new TestAbstractRepoManager();

    /**
     * This class is used to test the methods of AbstractRepoManager
     */
    static class TestAbstractRepoManager extends AbstractRepoManager {
    }

    @ParameterizedTest
    @MethodSource("shouldDownloadGraphMethodSource")
    void shouldDownloadGraph(Date remoteDate, Date activeDate, Date downloadedExtractedDate, Date downloadedCompressedDate, boolean expected) {
        assertEquals(expected, orsGraphRepoManager.shouldDownloadGraph(remoteDate, activeDate, downloadedExtractedDate, downloadedCompressedDate));
    }

    public static Stream<Arguments> shouldDownloadGraphMethodSource() {
        Date earlierDate = new Date(EARLIER_DATE);
        Date laterDate = new Date(LATER_DATE);

        return Stream.of(           //  downloaded        remote                   active
                Arguments.of(laterDate, earlierDate, earlierDate, earlierDate, true),
                Arguments.of(earlierDate, laterDate, earlierDate, earlierDate, false),
                Arguments.of(earlierDate, earlierDate, laterDate, earlierDate, false),
                Arguments.of(earlierDate, earlierDate, earlierDate, laterDate, false),
                Arguments.of(earlierDate, earlierDate, earlierDate, earlierDate, false)
        );
    }

    public static Stream<Arguments> comparisonDates() throws MalformedURLException {
        Date now = new Date();
        Date epocStart = new Date(0);
        return Stream.of(
                Arguments.of(new Date(0), null),
                Arguments.of(epocStart, new GraphInfo()),
                Arguments.of(epocStart, new GraphInfo().setLocalDirectory(tempDir.toFile())),
                Arguments.of(epocStart, new GraphInfo().withRemoteUrl(new URL("http://some.url.ors/"))),
                Arguments.of(epocStart, new GraphInfo().setPersistedGraphInfo(null)),
                Arguments.of(epocStart, new GraphInfo().setPersistedGraphInfo(new PersistedGraphInfo())),
                Arguments.of(epocStart, new GraphInfo().setPersistedGraphInfo(PersistedGraphInfo.withOsmDate(now))),
                Arguments.of(now, new GraphInfo().setPersistedGraphInfo(PersistedGraphInfo.withGraphBuildDate(now)))
        );
    }

    @ParameterizedTest
    @MethodSource("comparisonDates")
    void getDateOrEpocStart(Date expectedDate, GraphInfo graphInfo) {
        assertEquals(expectedDate, orsGraphRepoManager.getDateOrEpocStart(graphInfo));
    }

    @ParameterizedTest
    @MethodSource("comparisonDatesForDownloadFiles")
    void getDateOrEpocStart(Date expectedDate, File downloadFile, PersistedGraphInfo persistedGraphInfo) {
        assertEquals(expectedDate, orsGraphRepoManager.getDateOrEpocStart(downloadFile, persistedGraphInfo));
    }

    public static Stream<Arguments> comparisonDatesForDownloadFiles() throws IOException {
        Date now = new Date();
        Date epocStart = new Date(0);
        File resourcesDir = tempDir.toFile();
        File nonexistingFile = new File(resourcesDir, "missing.ghz");
        File existingFile = new File(resourcesDir, "some.ghz");
        existingFile.createNewFile();
        return Stream.of(
                Arguments.of(epocStart, null, null),
                Arguments.of(epocStart, null, new PersistedGraphInfo()),
                Arguments.of(epocStart, null, PersistedGraphInfo.withOsmDate(now)),
                Arguments.of(epocStart, null, PersistedGraphInfo.withGraphBuildDate(now)),

                Arguments.of(epocStart, nonexistingFile, null),
                Arguments.of(epocStart, nonexistingFile, new PersistedGraphInfo()),
                Arguments.of(epocStart, nonexistingFile, PersistedGraphInfo.withOsmDate(now)),
                Arguments.of(epocStart, nonexistingFile, PersistedGraphInfo.withGraphBuildDate(now)),

                Arguments.of(epocStart, existingFile, null),
                Arguments.of(epocStart, existingFile, new PersistedGraphInfo()),
                Arguments.of(epocStart, existingFile, PersistedGraphInfo.withOsmDate(now)),
                Arguments.of(now, existingFile, PersistedGraphInfo.withGraphBuildDate(now))
        );
    }

    @Test
    void newestDate() {
        assertEquals(new Date(LATER_DATE),
                orsGraphRepoManager.newestDate(
                        new Date(MIDDLE_DATE),
                        new Date(LATER_DATE),
                        new Date(EARLIER_DATE)));
    }
}
