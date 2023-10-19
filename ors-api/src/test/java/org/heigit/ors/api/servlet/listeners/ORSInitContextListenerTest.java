package org.heigit.ors.api.servlet.listeners;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ORSInitContextListenerTest {

    ORSInitContextListener orsInitContextListener;

    @BeforeEach
    void setUp(){
        orsInitContextListener = new ORSInitContextListener(null, null);
    }
    @ParameterizedTest
    @CsvSource(value = {
            "https://example.com/test-repo/planet,      https://example.com, test-repo, planet",
            "https://example.com/test-repo/planet/,     https://example.com, test-repo, planet",
            "' https://example.com/test-repo/planet ',  https://example.com, test-repo, planet",
            "' https://example.com/test-repo/planet/ ', https://example.com, test-repo, planet",
            "' http://example.com/test-repo/planet/ ',  http://example.com, test-repo, planet"
    })
    void extractSourceFileElements_withURL(String url, String expectedBaseUrl, String expectedRepoName, String expectedCoverage){
        ORSInitContextListener.SourceFileElements elements = orsInitContextListener.extractSourceFileElements(url);
        assertEquals(expectedBaseUrl, elements.repoBaseUrlString());
        assertEquals(expectedRepoName, elements.repoName());
        // TODO fix this function
        // assertEquals(expectedCoverage, elements.repoCoverage());
    }
}