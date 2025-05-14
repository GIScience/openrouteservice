package org.heigit.ors.coordinates_generator.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.function.Function;

public class CoordinateSnapper {
    private static final Logger LOGGER = LoggerFactory.getLogger(CoordinateSnapper.class);
    private static final double DEFAULT_SNAP_RADIUS = 1000; // 350 meters radius for snapping
    private static final String LOCATIONS_KEY = "locations";
    private static final String LOCATION_KEY = "location";

    private final String baseUrl;
    private final Map<String, String> headers;
    private final ObjectMapper mapper;
    private final Function<HttpPost, String> requestExecutor;

    public CoordinateSnapper(String baseUrl, Map<String, String> headers, ObjectMapper mapper,
                             Function<HttpPost, String> requestExecutor) {
        this.baseUrl = baseUrl;
        this.headers = headers;
        this.mapper = mapper;
        this.requestExecutor = requestExecutor;
    }
    
    public List<double[]> snapCoordinates(List<double[]> coordinates, String profile) {
        LOGGER.debug("Snapping {} coordinates for profile: {}", coordinates.size(), profile);
        
        try {
            HttpPost request = createSnapRequest(coordinates, profile);
            LOGGER.debug("Snap Request URI: {}", request.getRequestUri());
            // Payload is logged in createSnapRequest

            String response = requestExecutor.apply(request);
            LOGGER.debug("Snap Raw Response: {}", response);
            
            if (response == null) {
                LOGGER.debug("Received null response from snap API");
                return Collections.emptyList();
            }
            
            return processSnapResponse(response);
        } catch (IOException e) {
            LOGGER.error("Error snapping coordinates: {}", e.getMessage());
            return Collections.emptyList();
        }
    }
    
    private HttpPost createSnapRequest(List<double[]> coordinates, String profile) throws JsonProcessingException {
        Map<String, Object> payload = new HashMap<>();
        payload.put(LOCATIONS_KEY, coordinates);
        payload.put("radius", DEFAULT_SNAP_RADIUS);
        LOGGER.debug("Snap Request Payload: {}", payload); // Log payload here

        HttpPost request = new HttpPost(baseUrl + "/v2/snap/" + profile);
        headers.forEach(request::addHeader);
        request.setEntity(new StringEntity(mapper.writeValueAsString(payload), ContentType.APPLICATION_JSON));
        return request;
    }
    
    @SuppressWarnings("unchecked")
    private List<double[]> processSnapResponse(String response) throws JsonProcessingException {
        List<double[]> snappedCoordinates = new ArrayList<>();

        Map<String, Object> responseMap = mapper.readValue(response,
                new TypeReference<Map<String, Object>>() {});

        Object locationsObj = responseMap.get(LOCATIONS_KEY);
        if (!(locationsObj instanceof List<?>)) {
            LOGGER.debug("Snap response contained no valid locations array");
            return snappedCoordinates;
        }

        List<Map<String, Object>> locations = (List<Map<String, Object>>) locationsObj;
        for (Map<String, Object> location : locations) {
            if (location == null) continue;
            
            List<Number> coords = (List<Number>) location.get(LOCATION_KEY);
            if (coords != null && coords.size() >= 2) {
                double[] point = new double[] { coords.get(0).doubleValue(), coords.get(1).doubleValue() };
                snappedCoordinates.add(point);
                LOGGER.debug("Added snapped coordinate: [{}, {}]", point[0], point[1]);
            } else {
                LOGGER.debug("Invalid location in snap response");
            }
        }

        return snappedCoordinates;
    }
}
