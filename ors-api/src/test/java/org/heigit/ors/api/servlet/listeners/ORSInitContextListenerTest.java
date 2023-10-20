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
            "https://example.com/foo1/foo2/foo3/test-repo,      https://example.com, test-repo",
            "https://example.com/test-repo/,     https://example.com, test-repo",
            "https://example.com/test-repo,  https://example.com, test-repo",
            "' https://example.com/test-repo ',  https://example.com, test-repo",
            "https://example.com/test-repos/, https://example.com, test-repos",
            "http://example.com/test-repo/,  http://example.com, test-repo"
    })
    void extractSourceFileElements_withURL(String url, String expectedBaseUrl, String expectedRepoName){
        ORSInitContextListener.SourceFileElements elements = orsInitContextListener.extractSourceFileElements(url);
        assertEquals(expectedBaseUrl, elements.repoBaseUrlString());
        assertEquals(expectedRepoName, elements.repoName());
    }
}