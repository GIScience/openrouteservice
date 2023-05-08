/*  This file is part of Openrouteservice.
 *
 *  Openrouteservice is free software; you can redistribute it and/or modify it under the terms of the
 *  GNU Lesser General Public License as published by the Free Software Foundation; either version 2.1
 *  of the License, or (at your option) any later version.

 *  This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *  See the GNU Lesser General Public License for more details.

 *  You should have received a copy of the GNU Lesser General Public License along with this library;
 *  if not, see <https://www.gnu.org/licenses/>.
 */
package org.heigit.ors.apitests.isochrones.fast;

import org.heigit.ors.apitests.common.EndPointAnnotation;
import org.heigit.ors.apitests.common.ServiceTest;
import org.heigit.ors.apitests.common.VersionAnnotation;
import org.heigit.ors.apitests.isochrones.IsochronesErrorCodes;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;
import static org.heigit.ors.apitests.utils.CommonHeaders.geoJsonContent;
import static org.heigit.ors.apitests.utils.CommonHeaders.jsonContent;

@EndPointAnnotation(name = "isochrones")
@VersionAnnotation(version = "v2")
class ParamsTest extends ServiceTest {

    public ParamsTest() {
        // Locations
        addParameter("preference", "fastest");
        addParameter("hgvProfile", "driving-hgv");

        JSONArray firstLocation = new JSONArray();
        firstLocation.put(8.681495);
        firstLocation.put(49.41461);

        JSONArray secondLocation = new JSONArray();
        secondLocation.put(8.684177);
        secondLocation.put(49.421034);

        JSONArray thirdLocation = new JSONArray();
        thirdLocation.put(8.684177);
        thirdLocation.put(49.421034);

        JSONArray locations_1 = new JSONArray();
        locations_1.put(firstLocation);

        JSONArray locations_2 = new JSONArray();
        locations_2.put(firstLocation);
        locations_2.put(secondLocation);

        JSONArray locations_3 = new JSONArray();
        locations_3.put(firstLocation);
        locations_3.put(secondLocation);
        locations_3.put(thirdLocation);

        JSONArray ranges_2 = new JSONArray();
        ranges_2.put(1800);
        ranges_2.put(1800);
        //ranges_3.put(1800);

        JSONArray ranges_1800 = new JSONArray();
        ranges_1800.put(1800);

        Integer interval_100 = 100;
        Integer interval_200 = 200;
        Integer interval_900 = 900;

        JSONArray attributesReachfactorArea = new JSONArray();
        attributesReachfactorArea.put("area");
        attributesReachfactorArea.put("reachfactor");

        JSONArray attributesReachfactorAreaFaulty = new JSONArray();
        attributesReachfactorAreaFaulty.put("areaaaa");
        attributesReachfactorAreaFaulty.put("reachfactorrrr");

        addParameter("locations_1", locations_1);
        addParameter("locations_2", locations_2);
        addParameter("locations_3", locations_3);
        addParameter("ranges_2", ranges_2);
        addParameter("ranges_1800", ranges_1800);
        addParameter("interval_100", interval_100);
        addParameter("interval_200", interval_200);
        addParameter("interval_900", interval_900);
        addParameter("attributesReachfactorArea", attributesReachfactorArea);
        addParameter("attributesReachfactorAreaFaulty", attributesReachfactorAreaFaulty);
    }

    @Test
    void testObligatoryParams() {

        JSONObject body = new JSONObject();
        body.put("locations", getParameter("locations_2"));
        body.put("range", getParameter("ranges_2"));

        given()
                .headers(geoJsonContent)
                .pathParam("profile", getParameter("hgvProfile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}/geojson")
                .then()
                .body("any { it.key == 'type' }", is(true))
                .body("any { it.key == 'features' }", is(true))
                .statusCode(200);
    }

    @Test
    void testNotEnoughParams() {

        JSONObject body = new JSONObject();
        body.put("locations", getParameter("locations_2"));

        given()
                .headers(geoJsonContent)
                .pathParam("profile", getParameter("hgvProfile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}/geojson")
                .then()
                .body("error.code", is(IsochronesErrorCodes.MISSING_PARAMETER))
                .statusCode(400);
    }

    @Test
    void testParamSpelling() {

        JSONObject body = new JSONObject();
        body.put("locations", getParameter("locations_2"));
        body.put("rangeee", getParameter("ranges_2"));

        given()
                .headers(geoJsonContent)
                .pathParam("profile", getParameter("hgvProfile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}/geojson")
                .then()
                .body("error.code", is(IsochronesErrorCodes.UNKNOWN_PARAMETER))
                .statusCode(400);
    }

    @Test
    void testRangeInput() {

        JSONArray rangesFaulty = new JSONArray();
        rangesFaulty.put("1800sdf");

        JSONObject body = new JSONObject();
        body.put("locations", getParameter("locations_1"));
        body.put("range", rangesFaulty);

        given()
                .headers(geoJsonContent)
                .pathParam("profile", getParameter("hgvProfile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}/geojson")
                .then()
                .body("error.code", is(IsochronesErrorCodes.INVALID_PARAMETER_FORMAT))
                .statusCode(400);
    }

    @Test
    void testWrongLocationType() {

        JSONObject body = new JSONObject();
        body.put("locations", getParameter("locations_1"));
        body.put("range", getParameter("ranges_1800"));
        body.put("range_type", "time");
        body.put("interval", getParameter("interval_900"));
        body.put("location_type", "start123123");

        given()
                .headers(geoJsonContent)
                .pathParam("profile", getParameter("hgvProfile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}/geojson")
                .then()
                .body("error.code", is(IsochronesErrorCodes.INVALID_PARAMETER_VALUE))
                .statusCode(400);

    }

    @Test
    void testTooManyIntervals() {

        JSONObject body = new JSONObject();
        body.put("locations", getParameter("locations_1"));
        body.put("range", getParameter("ranges_1800"));
        body.put("range_type", "time");
        body.put("interval", getParameter("interval_100"));

        given()
                .headers(geoJsonContent)
                .pathParam("profile", getParameter("hgvProfile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}/geojson")
                .then()
                .body("error.code", is(IsochronesErrorCodes.PARAMETER_VALUE_EXCEEDS_MINIMUM))
                .statusCode(400);
    }

    // too many locations
    @Test
    void testTooManyLocations() {

        JSONObject body = new JSONObject();
        body.put("locations", getParameter("locations_3"));
        body.put("range", getParameter("ranges_1800"));
        body.put("range_type", "time");
        body.put("interval", getParameter("interval_100"));

        given()
                .headers(geoJsonContent)
                .pathParam("profile", getParameter("hgvProfile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}/geojson")
                .then()
                .body("error.code", is(IsochronesErrorCodes.PARAMETER_VALUE_EXCEEDS_MAXIMUM))
                .statusCode(400);
    }

    // unknown units
    @Test
    void testUnknownUnits() {

        JSONObject body = new JSONObject();
        body.put("locations", getParameter("locations_2"));
        body.put("range", getParameter("ranges_1800"));
        body.put("range_type", "distance");
        body.put("interval", "100");
        body.put("units", "mfff");

        given()
                .headers(geoJsonContent)
                .pathParam("profile", getParameter("hgvProfile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}/geojson")
                .then()
                .body("error.code", is(IsochronesErrorCodes.INVALID_PARAMETER_VALUE))
                .statusCode(400);

    }

    @Test
    void testUnknownLocationType() {
        JSONObject body = new JSONObject();
        body.put("locations", getParameter("locations_2"));
        body.put("range", getParameter("ranges_1800"));
        body.put("range_type", "distance");
        body.put("interval", "100");
        body.put("location_type", "blah");

        given()
                .headers(geoJsonContent)
                .pathParam("profile", getParameter("hgvProfile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}/geojson")
                .then()
                .body("error.code", is(IsochronesErrorCodes.INVALID_PARAMETER_VALUE))
                .statusCode(400);
    }

    @Test
    void testUnknownRangeType() {
        JSONObject body = new JSONObject();
        body.put("locations", getParameter("locations_2"));
        body.put("range", getParameter("ranges_1800"));
        body.put("interval", "100");
        body.put("range_type", "blah");

        given()
                .headers(geoJsonContent)
                .pathParam("profile", getParameter("hgvProfile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}/geojson")
                .then()
                .body("error.code", is(IsochronesErrorCodes.INVALID_PARAMETER_VALUE))
                .statusCode(400);
    }

    @Test
    void testUnknownAttributes() {
        JSONArray ranges = new JSONArray();
        ranges.put(400);

        JSONObject body = new JSONObject();
        body.put("locations", getParameter("locations_1"));
        body.put("range", ranges);
        body.put("range_type", "time");
        body.put("attributes", getParameter("attributesReachfactorAreaFaulty"));

        given()
                .headers(geoJsonContent)
                .pathParam("profile", getParameter("hgvProfile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath()+"/{profile}/geojson")
                .then()
                .assertThat()
                .body("error.code", is(IsochronesErrorCodes.INVALID_PARAMETER_VALUE))
                .statusCode(400);

        body.put("attributes", new JSONArray(Arrays.asList("blah", "reachfactor")));

        given()
                .headers(geoJsonContent)
                .pathParam("profile", getParameter("hgvProfile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath()+"/{profile}/geojson")
                .then()
                .assertThat()
                .body("error.code", is(IsochronesErrorCodes.INVALID_PARAMETER_VALUE))
                .statusCode(400);
    }

    @Test
    void expectUnknownAvoidFeatures() {
        JSONObject options = new JSONObject();
        JSONArray avoids = new JSONArray(Arrays.asList("blah", "ferries", "highways"));
        options.put("avoid_features", avoids);

        JSONObject body = new JSONObject();
        body.put("locations", getParameter("locations_2"));
        body.put("range", getParameter("ranges_1800"));
        body.put("range_type", "distance");
        body.put("interval", "100");
        body.put("options", options);

        given()
                .headers(geoJsonContent)
                .pathParam("profile", getParameter("hgvProfile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath()+"/{profile}/geojson")
                .then()
                .assertThat()
                .body("error.code", is(IsochronesErrorCodes.INVALID_PARAMETER_VALUE))
                .statusCode(400);
    }

    @Test
    void expectUnknownAvoidBorders() {
        JSONObject options = new JSONObject();
        options.put("avoid_borders", "blah");

        JSONObject body = new JSONObject();
        body.put("locations", getParameter("locations_2"));
        body.put("range", getParameter("ranges_1800"));
        body.put("range_type", "distance");
        body.put("interval", "100");
        body.put("options", options);

        given()
                .headers(geoJsonContent)
                .pathParam("profile", getParameter("hgvProfile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath()+"/{profile}/geojson")
                .then()
                .assertThat()
                .body("error.code", is(IsochronesErrorCodes.INVALID_PARAMETER_VALUE))
                .statusCode(400);
    }

    @Test
    void expectInvalidResponseFormat() {
        JSONObject body = new JSONObject();
        body.put("locations", getParameter("locations_1"));
        body.put("range", getParameter("ranges_400"));
        body.put("range_type", "time");

        given()
                .headers(geoJsonContent)
                .pathParam("profile", getParameter("hgvProfile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath()+"/{profile}/blah")
                .then()
                .assertThat()
                .body("error.code", is(IsochronesErrorCodes.UNSUPPORTED_EXPORT_FORMAT))
                .statusCode(406);

        given()
                .headers(jsonContent)
                .pathParam("profile", getParameter("hgvProfile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath()+"/{profile}/geojson")
                .then()
                .assertThat()
                .body("error.code", is(IsochronesErrorCodes.UNSUPPORTED_EXPORT_FORMAT))
                .statusCode(406);
    }

    @Test
    @Disabled("Currently no support for location type destination with fast isochrones")
    void testDestination() {

        JSONObject body = new JSONObject();
        body.put("locations", getParameter("locations_1"));
        body.put("range", getParameter("ranges_1800"));
        body.put("range_type", "time");
        body.put("interval", getParameter("interval_200"));
        body.put("location_type", "destination");

        given()
                .header("Accept", "application/geo+json")
                .header("Content-Type", "application/json")
                .pathParam("profile", getParameter("hgvProfile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}/geojson")
                .then()
                .statusCode(200);
    }

    @Test
    void testStart() {

        JSONObject body = new JSONObject();
        body.put("locations", getParameter("locations_1"));
        body.put("range", getParameter("ranges_1800"));
        body.put("range_type", "time");
        body.put("interval", getParameter("interval_200"));
        body.put("location_type", "start");

        given()
                .headers(geoJsonContent)
                .pathParam("profile", getParameter("hgvProfile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}/geojson")
                .then()
                .statusCode(200);
    }

    @Test
    void testRangetypeUnitsKm() {

        JSONObject body = new JSONObject();
        body.put("locations", getParameter("locations_1"));
        body.put("range", getParameter("ranges_1800"));
        body.put("range_type", "distance");
        body.put("interval", getParameter("interval_200"));
        body.put("units", "km");
        body.put("location_type", "start");

        given()
                .headers(geoJsonContent)
                .pathParam("profile", getParameter("hgvProfile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}/geojson")
                .then()
                .statusCode(200);
    }

    // m
    @Test
    void testRangetypeUnitsM() {

        JSONObject body = new JSONObject();
        body.put("locations", getParameter("locations_1"));
        body.put("range", getParameter("ranges_1800"));
        body.put("range_type", "distance");
        body.put("interval", getParameter("interval_200"));
        body.put("units", "m");
        body.put("location_type", "start");

        given()
                .headers(geoJsonContent)
                .pathParam("profile", getParameter("hgvProfile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}/geojson")
                .then()
                .statusCode(200);

    }

    // mi
    @Test
    void testRangetypeUnitsMi() {

        JSONObject body = new JSONObject();
        body.put("locations", getParameter("locations_1"));
        body.put("range", getParameter("ranges_1800"));
        body.put("range_type", "distance");
        body.put("interval", getParameter("interval_200"));
        body.put("units", "mi");
        body.put("location_type", "start");

        given()
                .headers(geoJsonContent)
                .pathParam("profile", getParameter("hgvProfile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}/geojson")
                .then()
                .statusCode(200);

    }

    @Test
    void testRanges() {

        JSONArray ranges = new JSONArray();
        ranges.put(600);
        ranges.put(400);
        ranges.put(300);
        JSONObject body = new JSONObject();
        body.put("locations", getParameter("locations_1"));
        body.put("range", ranges);
        body.put("range_type", "time");
        body.put("interval", getParameter("interval_200"));
        body.put("location_type", "start");

        given()
                .headers(geoJsonContent)
                .pathParam("profile", getParameter("hgvProfile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}/geojson")
                .then()
                .body("features[0].properties.value", is(300.0f))
                .body("features[1].properties.value", is(400.0f))
                .body("features[2].properties.value", is(600.0f))
                .statusCode(200);

    }

    @Test
    void testRangesUserUnits() {

        JSONArray ranges = new JSONArray();
        ranges.put(200);

        JSONObject body = new JSONObject();
        body.put("locations", getParameter("locations_1"));
        body.put("range", ranges);
        body.put("range_type", "distance");
        body.put("units", "km");
        body.put("location_type", "destination");

        given()
                .headers(geoJsonContent)
                .pathParam("profile", getParameter("hgvProfile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}/geojson")
                .then()
                .statusCode(200);
    }

    @Test
    void testRangeRestrictionTime() {

        JSONArray ranges = new JSONArray();
        ranges.put(23700);

        JSONObject body = new JSONObject();
        body.put("locations", getParameter("locations_1"));
        body.put("range", ranges);
        body.put("range_type", "time");

        given()
                .headers(geoJsonContent)
                .pathParam("profile", getParameter("hgvProfile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}/geojson")
                .then()
                .statusCode(400)
                .body("error.code", is(IsochronesErrorCodes.PARAMETER_VALUE_EXCEEDS_MAXIMUM));

    }

    @Test
    void testRangeRestrictionDistance() {

        JSONArray ranges = new JSONArray();
        ranges.put(1100000);

        JSONObject body = new JSONObject();
        body.put("locations", getParameter("locations_1"));
        body.put("range", ranges);
        body.put("range_type", "distance");

        given()
                .headers(geoJsonContent)
                .pathParam("profile", getParameter("hgvProfile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}/geojson")
                .then()
                .statusCode(400)
                .body("error.code", is(IsochronesErrorCodes.PARAMETER_VALUE_EXCEEDS_MAXIMUM));
    }

    @Test
    void testAttributes() {

        JSONArray ranges = new JSONArray();
        ranges.put(400);

        JSONObject body = new JSONObject();
        body.put("locations", getParameter("locations_1"));
        body.put("range", ranges);
        body.put("range_type", "time");
        body.put("attributes", getParameter("attributesReachfactorArea"));

        given()
                .headers(geoJsonContent)
                .pathParam("profile", getParameter("hgvProfile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}/geojson")
                .then()
                .body("features[0].containsKey('properties')", is(true))
                .body("features[0].properties.containsKey('area')", is(true))
                .body("features[0].properties.containsKey('reachfactor')", is(true))
                .statusCode(200);

    }

    @Test
    void testWrongAttributes() {

        JSONArray ranges = new JSONArray();
        ranges.put(400);

        JSONObject body = new JSONObject();
        body.put("locations", getParameter("locations_1"));
        body.put("range", ranges);
        body.put("range_type", "time");
        body.put("attributes", getParameter("attributesReachfactorAreaFaulty"));

        given()
                .headers(geoJsonContent)
                .pathParam("profile", getParameter("hgvProfile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}/geojson")
                .then()
                .statusCode(400)
                .body("error.code", is(IsochronesErrorCodes.INVALID_PARAMETER_VALUE));

    }

    @Test
    void testSmoothingFactor() {

        JSONArray ranges = new JSONArray();
        ranges.put(2000);

        JSONObject body = new JSONObject();
        body.put("locations", getParameter("locations_1"));
        body.put("range", ranges);
        body.put("range_type", "distance");
        body.put("smoothing", "50");

        given()
                .headers(geoJsonContent)
                .pathParam("profile", getParameter("hgvProfile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}/geojson")
                .then()
                .statusCode(200);

    }

    @Test
    void testSmoothingInvalidValue() {

        JSONArray ranges = new JSONArray();
        ranges.put(2000);

        JSONObject body = new JSONObject();
        body.put("locations", getParameter("locations_1"));
        body.put("range", ranges);
        body.put("range_type", "distance");
        body.put("smoothing", "ten");

        // ten, 101, -1
        given()
                .headers(geoJsonContent)
                .pathParam("profile", getParameter("hgvProfile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}/geojson")
                .then()
                .statusCode(400)
                .body("error.code", is(IsochronesErrorCodes.INVALID_PARAMETER_FORMAT));

        body.put("smoothing", "-1");

        given()
                .headers(geoJsonContent)
                .pathParam("profile", getParameter("hgvProfile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}/geojson")
                .then()
                .statusCode(400)
                .body("error.code", is(IsochronesErrorCodes.INVALID_PARAMETER_VALUE));

        body.put("smoothing", "101");

        given()
                .headers(geoJsonContent)
                .pathParam("profile", getParameter("hgvProfile"))
                .body(body.toString())
                .when()
                .post(getEndPointPath() + "/{profile}/geojson")
                .then()
                .statusCode(400)
                .body("error.code", is(IsochronesErrorCodes.INVALID_PARAMETER_VALUE));

    }

}
