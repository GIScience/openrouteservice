package utils;

import org.junit.jupiter.api.Assertions;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.JsonNode;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class OrsApiHelper {

    private static final String REQUEST_BODY = "{\"coordinates\":[[8.681495,49.41461],[8.686507,49.41943],[8.687872,49.420318]],\"options\":{\"avoid_polygons\":{\"coordinates\":[[[8.684881031827587,49.41768066444595],[8.684881031827587,49.41699648134178],[8.685816955915811,49.41699648134178],[8.685816955915811,49.41768066444595],[8.684881031827587,49.41768066444595]]],\"type\":\"Polygon\"}}}";

    public static boolean checkAvoidAreaRequest(String url, int expectedHttpCode) {
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json; charset=utf-8");
            connection.setRequestProperty("Accept", "application/geo+json; charset=utf-8");
            connection.setDoOutput(true);
            connection.getOutputStream().write(REQUEST_BODY.getBytes(StandardCharsets.UTF_8));

            int responseCode = connection.getResponseCode();
            return responseCode == expectedHttpCode;
        } catch (IOException e) {
            return false;
        }
    }

    private static JsonNode getProfiles(String address, int port) throws IOException {
        // Create a new URL object with the address and port
        URL url = new URL("http://" + address + ":" + port + "/ors/v2/status");
        // Create a new HTTP connection object with the URL
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        // Connect to the URL
        connection.connect();
        // Read the response from the connection
        String json = new String(connection.getInputStream().readAllBytes());
        // Close the connection
        connection.disconnect();
        // Parse the response as a JSON object
        ObjectMapper mapper = new ObjectMapper();
        JsonNode node = mapper.readTree(json);

        // Extract profiles from the JSON node
        List<String> profiles = new ArrayList<>();
        JsonNode profileNames = node.get("profiles");

        return profileNames;
    }

    public static void assertProfilesLoaded(GenericContainer<?> container, Map<String, Boolean> expectedProfiles) {
        JsonNode profiles;
        try {
            profiles = OrsApiHelper.getProfiles(container.getHost(), container.getFirstMappedPort());
            Assertions.assertEquals(expectedProfiles.size(), profiles.size());
            for (Map.Entry<String, Boolean> profile : expectedProfiles.entrySet()) {
                if (profile.getValue())
                    Assertions.assertTrue(profiles.has(profile.getKey()));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
