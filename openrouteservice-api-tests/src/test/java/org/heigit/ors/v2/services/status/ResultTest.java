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
package org.heigit.ors.v2.services.status;

import org.heigit.ors.v2.services.common.EndPointAnnotation;
import org.heigit.ors.v2.services.common.ServiceTest;
import org.heigit.ors.v2.services.common.VersionAnnotation;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;

@EndPointAnnotation(name = "status")
@VersionAnnotation(version = "v2")
class ResultTest extends ServiceTest {

    @Test
    void testGetStatus() {
        given()
                .get(getEndPointPath())
                .then().log().ifValidationFails()
                .assertThat()
                .body("any { it.key == 'engine' }", is(true))
                .body("any { it.key == 'services' }", is(true))
                .body("any { it.key == 'languages' }", is(true))
                .body("any { it.key == 'profiles' }", is(true))
                .body("any { it.key == 'kafkaConsumer' }", is(true))
                .body("kafkaConsumer.runners", is(greaterThan(0)))
                .body("kafkaConsumer.processed", is(greaterThan(0)))
                .body("kafkaConsumer.failed", is(0))
                .statusCode(200);
    }
}
