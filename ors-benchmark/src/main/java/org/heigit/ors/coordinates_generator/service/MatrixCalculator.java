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

public class MatrixCalculator {
    private static final Logger LOGGER = LoggerFactory.getLogger(MatrixCalculator.class);
    private static final String LOCATIONS_KEY = "locations";
    private static final String SOURCES_KEY = "sources";
    private static final String DESTINATIONS_KEY = "destinations";
    private static final String ERROR_CALCULATING_MATRIX = "Error calculating matrix: {}";

    private final String baseUrl;
    private final Map<String, String> headers;
    private final ObjectMapper mapper;
    private final Function<HttpPost, String> requestExecutor;

    public MatrixCalculator(String baseUrl, Map<String, String> headers, ObjectMapper mapper,
                           Function<HttpPost, String> requestExecutor) {
        this.baseUrl = baseUrl;
        this.headers = headers;
        this.mapper = mapper;
        this.requestExecutor = requestExecutor;
    }

    public Optional<MatrixResult> calculateMatrix(List<double[]> coordinates, String profile) {
        try {
            HttpPost request = createMatrixRequest(coordinates, profile);
            LOGGER.debug("Matrix Request URI: {}", request.getRequestUri());
            // Payload is logged in createMatrixRequest
            String response = requestExecutor.apply(request);
            LOGGER.debug("Matrix Raw Response: {}", response);
            
            if (response == null) {
                LOGGER.debug("Received null response from matrix API");
                return Optional.empty();
            }
            
            return Optional.of(processMatrixResponse(response));
        } catch (IOException e) {
            LOGGER.error(ERROR_CALCULATING_MATRIX, e.getMessage());
            return Optional.empty();
        }
    }
    
    public String calculateMatrixRaw(List<double[]> coordinates, String profile) {
        try {
            HttpPost request = createMatrixRequest(coordinates, profile);
            LOGGER.debug("Matrix Raw Request URI: {}", request.getRequestUri());
            // Payload is logged in createMatrixRequest
            String response = requestExecutor.apply(request);
            LOGGER.debug("Matrix Raw Response (from calculateMatrixRaw): {}", response);
            return response;
        } catch (IOException e) {
            LOGGER.error(ERROR_CALCULATING_MATRIX, e.getMessage());
            return null;
        }
    }

    public Optional<MatrixResult> calculateAsymmetricMatrix(List<double[]> coordinates, int[] sources, int[] destinations, String profile) {
        try {
            HttpPost request = createAsymmetricMatrixRequest(coordinates, sources, destinations, profile);
            LOGGER.debug("Asymmetric Matrix Request URI: {}", request.getRequestUri());
            // Payload is logged in createAsymmetricMatrixRequest
            String response = requestExecutor.apply(request);
            LOGGER.debug("Asymmetric Matrix Raw Response: {}", response);

            if (response == null) {
                LOGGER.debug("Received null response from matrix API");
                return Optional.empty();
            }

            return Optional.of(processMatrixResponse(response));
        } catch (IOException e) {
            LOGGER.error(ERROR_CALCULATING_MATRIX, e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Create a matrix request where all coordinates are treated as both sources as destinations
     */
    private HttpPost createMatrixRequest(List<double[]> coordinates, String profile) throws JsonProcessingException {
        Map<String, Object> payload = new HashMap<>();
        payload.put(LOCATIONS_KEY, coordinates);
        payload.put("metrics", new String[] { "distance" });
        LOGGER.debug("Matrix Request Payload: {}", payload);

        HttpPost request = new HttpPost(baseUrl + "/v2/matrix/" + profile);
        headers.forEach(request::addHeader);
        request.setEntity(new StringEntity(mapper.writeValueAsString(payload), ContentType.APPLICATION_JSON));
        return request;
    }

    /**
     * Create a matrix request with specified sources and destinations
     * Sources and destinations are interpreted as indices into the list of coordinates
     */
    private HttpPost createAsymmetricMatrixRequest(List<double[]> coordinates, int[] sources, int[] destinations, String profile) throws JsonProcessingException {
        Map<String, Object> payload = new HashMap<>();
        payload.put(LOCATIONS_KEY, coordinates);
        payload.put(SOURCES_KEY, sources);
        payload.put(DESTINATIONS_KEY, destinations);
        payload.put("metrics", new String[] { "distance" });
        LOGGER.debug("Asymmetric Matrix Request Payload: {}", payload);

        HttpPost request = new HttpPost(baseUrl + "/v2/matrix/" + profile);
        headers.forEach(request::addHeader);
        request.setEntity(new StringEntity(mapper.writeValueAsString(payload), ContentType.APPLICATION_JSON));
        return request;
    }
    
    private MatrixResult processMatrixResponse(String response) throws JsonProcessingException {
        Map<String, Object> responseMap = mapper.readValue(response, 
                new TypeReference<Map<String, Object>>() {});
        
        List<List<Double>> distances = extractDistances(responseMap);
        List<Map<String, Object>> sources = extractLocations(responseMap, SOURCES_KEY);
        List<Map<String, Object>> destinations = extractLocations(responseMap, DESTINATIONS_KEY);
        
        return new MatrixResult(sources, destinations, distances);
    }
    
    @SuppressWarnings("unchecked")
    private List<List<Double>> extractDistances(Map<String, Object> responseMap) {
        Object distancesObj = responseMap.get("distances");
        if (distancesObj instanceof List<?>) {
            return (List<List<Double>>) distancesObj;
        }
        return Collections.emptyList();
    }
    
    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> extractLocations(Map<String, Object> responseMap, String key) {
        Object locationsObj = responseMap.get(key);
        if (locationsObj instanceof List<?>) {
            return (List<Map<String, Object>>) locationsObj;
        }
        return Collections.emptyList();
    }
    
    public static class MatrixResult {
        private final List<Map<String, Object>> sources;
        private final List<Map<String, Object>> destinations;
        private final List<List<Double>> distances;
        
        public MatrixResult(List<Map<String, Object>> sources, List<Map<String, Object>> destinations, 
                           List<List<Double>> distances) {
            this.sources = sources;
            this.destinations = destinations;
            this.distances = distances;
        }
        
        public List<Map<String, Object>> getSources() {
            return sources;
        }
        
        public List<Map<String, Object>> getDestinations() {
            return destinations;
        }
        
        public List<List<Double>> getDistances() {
            return distances;
        }
        
        public boolean isValid() {
            return !sources.isEmpty() && !destinations.isEmpty() && !distances.isEmpty() && 
                   sources.size() == destinations.size();
        }
    }
}
