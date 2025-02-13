package org.heigit.ors.benchmark;

import io.gatling.javaapi.core.*;
import io.gatling.javaapi.http.HttpProtocolBuilder;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.http;
import static io.gatling.javaapi.http.HttpDsl.status;

public class IsochronesLoadTest extends Simulation {

    static final int BATCH_SIZE_UPTO = 5;
    static final String BASE_URL;
    static final String API_KEY;

    static {
        BASE_URL = System.getProperty("base_url") != null ? System.getProperty("base_url") : "http://localhost:8082/ors";
        API_KEY = System.getProperty("api_key") != null ? System.getProperty("api_key") : "API KEY";
    }

    static String locations(int num) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < num; i++) {
            sb.append("[#{point(").append(i).append(")}]");
            if (i < num - 1) {
                sb.append(",");
            }
        }
        return sb.toString();
    }

    static ChainBuilder makeRequest(int batchSize) {
        FeederBuilder<String> feeder = ssv("search.csv").random();
        return exec(
                feed(feeder, batchSize),
                http("Post")
                        .post("/v2/isochrones/driving-car")
                        .body(StringBody("{\"locations\":[" + locations(batchSize) + "] , \"range\":[300]}"))
                        .check(status().is(200))
                        .check(status().saveAs("responseStatus"))
                        .check(bodyString().saveAs("responseBody"))
        ).exec(session -> {
            if (!session.contains("responseStatus")) {
                System.out.println("Connection failed, check the server status or baseURL: " + BASE_URL);
                return session;
            }
            int responseStatus = session.getInt("responseStatus");
            if (responseStatus != 200) {
                System.out.println("Response status: " + responseStatus + ", Response body: " + session.getString("responseBody"));
            }
            return session;
        });
    }

    HttpProtocolBuilder httpProtocol =
            http.baseUrl(BASE_URL)
                    .authorizationHeader("Bearer " + API_KEY)
                    .acceptHeader("application/geo+json; charset=utf-8")
                    .contentTypeHeader("application/json; charset=utf-8")
                    .userAgentHeader(
                            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10.15; rv:109.0) Gecko/20100101 Firefox/119.0"
                    );

    PopulationBuilder executions;
    {
        OpenInjectionStep injection = rampUsers(100).during(5);
        for (int i = 1; i <= BATCH_SIZE_UPTO; i++) {
            if (executions == null) {
                executions = scenario("Scenario " + i).exec(makeRequest(i)).injectOpen(injection);
            } else {
                executions = executions.andThen(scenario("Scenario " + i).exec(makeRequest(i)).injectOpen(injection));
            }
        }
        setUp(executions).protocols(httpProtocol);
    }
}
