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
package org.heigit.ors.apitests.status;

import org.heigit.ors.apitests.common.EndPointAnnotation;
import org.heigit.ors.apitests.common.ServiceTest;
import org.heigit.ors.apitests.common.VersionAnnotation;
import org.heigit.ors.kafka.ORSKafkaConsumerRunner;
import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;

@EndPointAnnotation(name = "status")
@VersionAnnotation(version = "v2")
class ResultTest extends ServiceTest {

    @Test
    void testGetStatus() throws InterruptedException {
        waitForKafkaConsumer();

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

    @SuppressWarnings("BusyWait")
    private void waitForKafkaConsumer() {
        // This is a hack. See comment in ORSKafkaConsumerRunner
        int seconds = 0;
        final int timeout = 10;
        while(!ORSKafkaConsumerRunner.hasRunOnce) {
            if (seconds++ >= timeout) {
                String message = String.format("Waiting for ORSKafkaConsumerRunner failed after %s seconds.", timeout);
                throw new AssertionFailedError(message);
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
