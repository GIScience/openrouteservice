package heigit.ors.api_tests;

import static io.restassured.RestAssured.*;
import static io.restassured.matcher.RestAssuredMatchers.*;
import static org.hamcrest.Matchers.*;
import org.junit.Test;

public class OrsFirstTest {
	
	@Test
    public void makeSureORSIsUp() {

    	given().when().get("http://localhost:8082/openrouteservice-4.0.0/status").then().statusCode(200);
    
	}
	
}
