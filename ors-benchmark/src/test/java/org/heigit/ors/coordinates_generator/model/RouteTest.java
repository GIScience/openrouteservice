package org.heigit.ors.coordinates_generator.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RouteTest {

    @Test
    void testRouteConstruction() {
        double[] start = {8.681495, 49.41461};
        double[] end = {8.686507, 49.41943};
        double distance = 1500.5;
        String profile = "driving-car";

        Route route = new Route(start, end, distance, profile);

        assertNotNull(route);
        assertArrayEquals(start, route.getStart());
        assertArrayEquals(end, route.getEnd());
        assertEquals(distance, route.getDistance());
        assertEquals(profile, route.getProfile());
    }

    @Test
    void testRouteEquals() {
        double[] start1 = {8.681495, 49.41461};
        double[] end1 = {8.686507, 49.41943};
        double distance1 = 1500.5;
        String profile1 = "driving-car";

        double[] start2 = {8.681495, 49.41461};
        double[] end2 = {8.686507, 49.41943};
        double distance2 = 1500.5;
        String profile2 = "driving-car";

        Route route1 = new Route(start1, end1, distance1, profile1);
        Route route2 = new Route(start2, end2, distance2, profile2);

        assertEquals(route1, route2);
        assertEquals(route1.hashCode(), route2.hashCode());
    }

    @Test
    void testRouteNotEqualsWithDifferentStart() {
        double[] start1 = {8.681495, 49.41461};
        double[] start2 = {8.682000, 49.41500};
        double[] end = {8.686507, 49.41943};
        double distance = 1500.5;
        String profile = "driving-car";

        Route route1 = new Route(start1, end, distance, profile);
        Route route2 = new Route(start2, end, distance, profile);

        assertNotEquals(route1, route2);
        assertNotEquals(route1.hashCode(), route2.hashCode());
    }

    @Test
    void testRouteNotEqualsWithDifferentEnd() {
        double[] start = {8.681495, 49.41461};
        double[] end1 = {8.686507, 49.41943};
        double[] end2 = {8.687000, 49.42000};
        double distance = 1500.5;
        String profile = "driving-car";

        Route route1 = new Route(start, end1, distance, profile);
        Route route2 = new Route(start, end2, distance, profile);

        assertNotEquals(route1, route2);
        assertNotEquals(route1.hashCode(), route2.hashCode());
    }

    @Test
    void testRouteNotEqualsWithDifferentDistance() {
        double[] start = {8.681495, 49.41461};
        double[] end = {8.686507, 49.41943};
        double distance1 = 1500.5;
        double distance2 = 2000.0;
        String profile = "driving-car";

        Route route1 = new Route(start, end, distance1, profile);
        Route route2 = new Route(start, end, distance2, profile);

        assertNotEquals(route1, route2);
        assertNotEquals(route1.hashCode(), route2.hashCode());
    }

    @Test
    void testRouteNotEqualsWithDifferentProfile() {
        double[] start = {8.681495, 49.41461};
        double[] end = {8.686507, 49.41943};
        double distance = 1500.5;
        String profile1 = "driving-car";
        String profile2 = "cycling-regular";

        Route route1 = new Route(start, end, distance, profile1);
        Route route2 = new Route(start, end, distance, profile2);

        assertNotEquals(route1, route2);
        assertNotEquals(route1.hashCode(), route2.hashCode());
    }

    @Test
    void testRouteEqualsWithSameReference() {
        double[] start = {8.681495, 49.41461};
        double[] end = {8.686507, 49.41943};
        double distance = 1500.5;
        String profile = "driving-car";

        Route route = new Route(start, end, distance, profile);

        assertEquals(route, route);
    }

    @Test
    void testRouteNotEqualsWithNull() {
        double[] start = {8.681495, 49.41461};
        double[] end = {8.686507, 49.41943};
        double distance = 1500.5;
        String profile = "driving-car";

        Route route = new Route(start, end, distance, profile);

        assertNotEquals(null, route);
    }

    @Test
    void testRouteNotEqualsWithDifferentClass() {
        double[] start = {8.681495, 49.41461};
        double[] end = {8.686507, 49.41943};
        double distance = 1500.5;
        String profile = "driving-car";

        Route route = new Route(start, end, distance, profile);

        assertNotEquals("Not a Route object", route);
    }
}
