package org.heigit.ors.integrationtests;

import io.restassured.RestAssured;
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
class ServiceTest {


	@Autowired
	private TestRestTemplate testRestTemplate;

	@LocalServerPort
	private Integer port;

	@BeforeEach
	public void setup() {
		RestAssured.port = port;
		RestAssured.baseURI = testRestTemplate.getRootUri();
	}

	@Test
	void checkHealth() {
		RestAssured.given().get("/v2/health").then().statusCode(200);
	}
}
