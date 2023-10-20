package org.heigit.ors.api.servlet.listeners;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ORSInitContextListenerTest {

    ORSInitContextListener orsInitContextListener;

    @BeforeEach
    void setUp(){
        orsInitContextListener = new ORSInitContextListener(null, null);
    }
    @ParameterizedTest
    @CsvSource(value = {
            "https://example.com/foo1/foo2/foo3/test-repo,      https://example.com, test-repo, true",
            "https://example.com/test-repo/,     https://example.com, test-repo, true",
            "https://example.com/test-repo,  https://example.com, test-repo, true",
            "' https://example.com/test-repo ',  https://example.com, test-repo, true",
            "https://example.com/test-repos/, https://example.com, test-repos, true",
            "http://example.com/test-repo/,  http://example.com, test-repo, true",
            "/foo/test-repo/,  null, null, false"
    })
    void extractSourceFileElements_withURL(String url, String expectedBaseUrl, String expectedRepoName, Boolean isUrl){
        ORSInitContextListener.SourceFileElements elements = orsInitContextListener.extractSourceFileElements(url);
        if (isUrl) {
            assertEquals(expectedBaseUrl, elements.repoBaseUrlString());
            assertEquals(expectedRepoName, elements.repoName());
            assertEquals("", elements.localOsmFilePath());
        } else {
            assertEquals(url, elements.localOsmFilePath());
            assertNull(elements.repoBaseUrlString());
            assertNull(elements.repoName());
        }
    }
}