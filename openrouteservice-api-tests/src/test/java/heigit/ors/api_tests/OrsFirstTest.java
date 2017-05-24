package heigit.ors.api_tests;

import static io.restassured.RestAssured.*;
import static io.restassured.matcher.RestAssuredMatchers.*;
import static org.hamcrest.Matchers.*;
import org.junit.Test;

public class OrsFirstTest {
	
	@Test
    public void makeSureThatGoogleIsUp() {
		System.out.println("A"); 

    	given().when().get("http://www.google.com").then().statusCode(200);
    }
	
}
