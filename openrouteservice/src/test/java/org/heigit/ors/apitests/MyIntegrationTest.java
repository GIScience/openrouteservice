package org.heigit.ors.apitests;

import io.restassured.RestAssured;
import org.heigit.ors.apitests.common.InitializeGraphsOnce;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ExtendWith(InitializeGraphsOnce.class)
class MyIntegrationTest {


	@Autowired
	private TestRestTemplate testRestTemplate; // available with Spring Web MVC

	@LocalServerPort
	private Integer port;

	@BeforeEach
	public void setup() {
		RestAssured.port = port;
		RestAssured.baseURI = testRestTemplate.getRootUri();
	}

	@Test
	void restsTemplateExample() {
		ResponseEntity<String> response = this.testRestTemplate
				.getForEntity("/v2/status", String.class);

		Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
		// System.out.println("#### " + response.getBody());
	}

	@Test
	void restAssuredExample() {
		RestAssured.given().get("/v2/status").then().statusCode(200);
	}

	@Test
	void checkHealth() {
		RestAssured.given().get("/v2/health").then().statusCode(200);
	}
}
