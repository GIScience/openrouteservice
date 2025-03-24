package org.heigit.ors.coordinates_generator.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class RouteRepositoryTest {

    private RouteRepository repository;
    private final Set<String> profiles = Set.of("driving-car", "cycling-regular");

    @BeforeEach
    @SuppressWarnings("unused")
    void setUp() {
        repository = new RouteRepository(profiles);
    }

    @Test
    void testAddRoute() {
        double[] start = {8.681495, 49.41461};
        double[] end = {8.686507, 49.41943};
        double distance = 1500.5;
        String profile = "driving-car";

        Route route = new Route(start, end, distance, profile);
        boolean added = repository.addRoute(route);

        assertTrue(added);
        assertEquals(1, repository.getRouteCount(profile));
        assertEquals(0, repository.getRouteCount("cycling-regular"));
    }

    @Test
    void testAddDuplicateRoute() {
        double[] start = {8.681495, 49.41461};
        double[] end = {8.686507, 49.41943};
        double distance = 1500.5;
        String profile = "driving-car";

        Route route1 = new Route(start, end, distance, profile);
        Route route2 = new Route(start, end, distance, profile);

        assertTrue(repository.addRoute(route1));
        assertFalse(repository.addRoute(route2));
        assertEquals(1, repository.getRouteCount(profile));
    }

    @Test
    void testAddRouteIfUnique() {
        double[] start = {8.681495, 49.41461};
        double[] end = {8.686507, 49.41943};
        double distance = 1500.5;
        String profile = "driving-car";

        Route route = new Route(start, end, distance, profile);
        boolean added = repository.addRouteIfUnique(route);

        assertTrue(added);
        assertEquals(1, repository.getRouteCount(profile));
    }

    @Test
    void testClear() {
        // Add some routes
        for (String profile : profiles) {
            for (int i = 0; i < 5; i++) {
                double[] start = {8.681495 + i * 0.001, 49.41461 + i * 0.001};
                double[] end = {8.686507 + i * 0.001, 49.41943 + i * 0.001};
                repository.addRoute(new Route(start, end, 1500.5 + i * 100, profile));
            }
        }

        assertEquals(5, repository.getRouteCount("driving-car"));
        assertEquals(5, repository.getRouteCount("cycling-regular"));
        assertEquals(10, repository.getTotalRouteCount());

        repository.clear();

        assertEquals(0, repository.getRouteCount("driving-car"));
        assertEquals(0, repository.getRouteCount("cycling-regular"));
        assertEquals(0, repository.getTotalRouteCount());
    }

    @Test
    void testGetTotalRouteCount() {
        // Add some routes
        for (String profile : profiles) {
            for (int i = 0; i < 3; i++) {
                double[] start = {8.681495 + i * 0.001, 49.41461 + i * 0.001};
                double[] end = {8.686507 + i * 0.001, 49.41943 + i * 0.001};
                repository.addRoute(new Route(start, end, 1500.5 + i * 100, profile));
            }
        }

        assertEquals(6, repository.getTotalRouteCount());
    }

    @Test
    void testIsProfileComplete() {
        // Add routes just for driving-car
        for (int i = 0; i < 5; i++) {
            double[] start = {8.681495 + i * 0.001, 49.41461 + i * 0.001};
            double[] end = {8.686507 + i * 0.001, 49.41943 + i * 0.001};
            repository.addRoute(new Route(start, end, 1500.5 + i * 100, "driving-car"));
        }

        assertTrue(repository.isProfileComplete("driving-car", 5));
        assertFalse(repository.isProfileComplete("driving-car", 6));
        assertFalse(repository.isProfileComplete("cycling-regular", 1));
    }

    @Test
    void testAreAllProfilesComplete() {
        // Add routes for all profiles
        for (String profile : profiles) {
            for (int i = 0; i < 5; i++) {
                double[] start = {8.681495 + i * 0.001, 49.41461 + i * 0.001};
                double[] end = {8.686507 + i * 0.001, 49.41943 + i * 0.001};
                repository.addRoute(new Route(start, end, 1500.5 + i * 100, profile));
            }
        }

        assertTrue(repository.areAllProfilesComplete(5));
        assertFalse(repository.areAllProfilesComplete(6));
    }

    @Test
    void testGetAllRoutes() {
        // Add routes for all profiles
        for (String profile : profiles) {
            for (int i = 0; i < 5; i++) {
                double[] start = {8.681495 + i * 0.001, 49.41461 + i * 0.001};
                double[] end = {8.686507 + i * 0.001, 49.41943 + i * 0.001};
                repository.addRoute(new Route(start, end, 1500.5 + i * 100, profile));
            }
        }

        List<Route> allRoutes = repository.getAllRoutes(10);
        assertEquals(10, allRoutes.size());

        // Test with limit
        List<Route> limitedRoutes = repository.getAllRoutes(3);
        assertEquals(6, limitedRoutes.size()); // 3 from each profile
    }

    @Test
    void testWriteToCSV(@TempDir Path tempDir) throws IOException {
        // Add routes for all profiles
        for (String profile : profiles) {
            for (int i = 0; i < 2; i++) {
                double[] start = {8.681495 + i * 0.001, 49.41461 + i * 0.001};
                double[] end = {8.686507 + i * 0.001, 49.41943 + i * 0.001};
                repository.addRoute(new Route(start, end, 1500.5 + i * 100, profile));
            }
        }

        String filename = tempDir.resolve("routes.csv").toString();
        repository.writeToCSV(filename);

        List<String> lines = Files.readAllLines(Path.of(filename));
        assertEquals(5, lines.size()); // Header + 4 routes
        assertTrue(lines.get(0).startsWith("start_longitude,start_latitude"));
        for (int i = 1; i < lines.size(); i++) {
            assertTrue(lines.get(i).contains("driving-car") || lines.get(i).contains("cycling-regular"));
        }

        // Test with limit
        String limitedFilename = tempDir.resolve("routes_limited.csv").toString();
        repository.writeToCSV(limitedFilename, 1);

        lines = Files.readAllLines(Path.of(limitedFilename));
        assertEquals(3, lines.size()); // Header + 2 routes (1 per profile)
    }

    @Test
    void testGetProgressMessage() {
        // Add different numbers of routes for each profile
        for (int i = 0; i < 3; i++) {
            double[] start = {8.681495 + i * 0.001, 49.41461 + i * 0.001};
            double[] end = {8.686507 + i * 0.001, 49.41943 + i * 0.001};
            repository.addRoute(new Route(start, end, 1500.5 + i * 100, "driving-car"));
        }
        
        for (int i = 0; i < 2; i++) {
            double[] start = {8.681495 + i * 0.001, 49.41461 + i * 0.001};
            double[] end = {8.686507 + i * 0.001, 49.41943 + i * 0.001};
            repository.addRoute(new Route(start, end, 1500.5 + i * 100, "cycling-regular"));
        }

        String message = repository.getProgressMessage();
        assertTrue(message.contains("driving-car: 3"));
        assertTrue(message.contains("cycling-regular: 2"));
    }

    @Test
    void testRepositoryWithUnknownProfile() {
        Set<String> definedProfiles = new HashSet<>(profiles);
        RouteRepository testRepo = new RouteRepository(definedProfiles);
        
        // Try to add a route for an undefined profile
        double[] start = {8.681495, 49.41461};
        double[] end = {8.686507, 49.41943};
        Route route = new Route(start, end, 1500.5, "undefined-profile");
        
        // This shouldn't throw an exception but should fail to add
        boolean added = testRepo.addRoute(route);
        assertFalse(added);
    }
}
