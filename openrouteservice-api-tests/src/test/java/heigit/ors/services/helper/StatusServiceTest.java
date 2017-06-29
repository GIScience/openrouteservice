package heigit.ors.services.helper;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.is;

import org.junit.Test;

import heigit.ors.services.common.EndPointAnnotation;
import heigit.ors.services.common.ServiceTest;

@EndPointAnnotation(name="status")
public class StatusServiceTest extends ServiceTest {
	
	public StatusServiceTest() {
	}

	@Test
	public void pingTest() {

		given()
				.when()
				.get(getEndPointName())
				.then()
				.body("any { it.key == 'profiles' }", is(true))
				.statusCode(200);
	}
}
