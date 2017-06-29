package heigit.ors.services.helper;

import static io.restassured.RestAssured.*;

import org.junit.Test;

import heigit.ors.services.common.EndPointAnnotation;
import heigit.ors.services.common.ServiceTest;

@EndPointAnnotation(name="health")
public class HealthServiceTest extends ServiceTest {
	
	public HealthServiceTest() {
	}

	@Test
	public void pingTest() {

		given()
				.when()
				.get(getEndPointName())
				.then()
				.statusCode(200);
	}
}
