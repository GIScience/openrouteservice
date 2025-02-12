package org.heigit.ors.benchmark;

import io.gatling.javaapi.core.*;
import io.gatling.javaapi.http.HttpProtocolBuilder;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.http;
import static io.gatling.javaapi.http.HttpDsl.status;

public class IsochronesLoadTest extends Simulation {

    static final int BATCH_SIZE_UPTO = 5;

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
                        .body(StringBody("{\"locations\":[" + locations(batchSize) + "] , \"range\":[100, 200, 300]}"))
                        .check(status().is(200))
                        .check(status().saveAs("responseStatus"))
                        .check(bodyString().saveAs("responseBody"))
        ).exec(session -> {
            int responseStatus = session.getInt("responseStatus");
            if (responseStatus != 200) {
                System.out.println("Response status: " + responseStatus + ", Response body: " + session.getString("responseBody"));
            }
            return session;
        });
    }

    HttpProtocolBuilder httpProtocol =
//            http.baseUrl("https://api.openrouteservice.org")
//                    .authorizationHeader("Bearer API_KEY")
            http.baseUrl("http://localhost:8082/ors")
                    .authorizationHeader("Bearer API KEY")
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
