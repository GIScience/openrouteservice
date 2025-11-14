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
package org.heigit.ors.apitests.dynamicrouting;

import org.heigit.ors.apitests.common.AbstractContainerBaseTest;
import org.heigit.ors.apitests.common.EndPointAnnotation;
import org.heigit.ors.apitests.common.VersionAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.junit.jupiter.TestcontainersExtension;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;

// Test order is needed to ensure that this test runs first because the
// database from which the dynamic data is fetched must be available before
// ORS is started. If other tests run first they may start ORS too early.
// (see also test/resources/junit-platform.properties)
@Order(0)
@EndPointAnnotation(name = "status")
@VersionAnnotation(version = "v2")
@ExtendWith(TestcontainersExtension.class)
@Testcontainers(disabledWithoutDocker = true)
class StatusResultTest extends AbstractContainerBaseTest {

    @Test
    void testGetStatus() {
        given()
                .get(getEndPointPath())
                .then().log().ifValidationFails()
                .assertThat()
                .body("profiles.driving-car.dynamic_data.logie_bridges.mapped_edges", is(1))
                .body("profiles.driving-car.dynamic_data.logie_borders.mapped_edges", is(1))
                .body("profiles.driving-car.dynamic_data.logie_roads.mapped_edges", is(2))
                .body("dynamic_data_service.logie_bridges.count_features", is(1))
                .body("dynamic_data_service.logie_borders.count_features", is(1))
                .body("dynamic_data_service.logie_roads.count_features", is(1))
                .statusCode(200);
    }
}
