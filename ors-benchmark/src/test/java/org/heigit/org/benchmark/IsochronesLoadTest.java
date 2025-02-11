package org.heigit.org.benchmark;

import io.gatling.javaapi.core.ChainBuilder;
import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Simulation;
import io.gatling.javaapi.http.HttpProtocolBuilder;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.http;
import static io.gatling.javaapi.http.HttpDsl.status;

public class IsochronesLoadTest extends Simulation {

//    FeederBuilder<String> feeder = csv("search.csv").random();
//
//    ChainBuilder search = exec(
//        http("Home").get("/"),
//        pause(1),
//        feed(feeder),
//        http("Search")
//            .get("/computers?f=#{searchCriterion}")
//            .check(
//                css("a:contains('#{searchComputerName}')", "href").saveAs("computerUrl")
//            ),
//        pause(1),
//        http("Select")
//            .get("#{computerUrl}")
//            .check(status().is(200)),
//        pause(1)
//    );

    ChainBuilder request = exec(
        http("Post")
                .post("/v2/isochrones/driving-car")
                .body(StringBody("{\"locations\":[[8.681495,49.41461],[8.686507,49.41943]],\"range\":[300]}"))
                .check(status().is(200))
                .check(status().saveAs("responseStatus"))
                .check(bodyString().saveAs("responseBody"))
    ).exec(session -> {
        int responseStatus = session.getInt("responseStatus");
        if (responseStatus == 200) {
            System.out.println("Response status: " + responseStatus + ", Response body: " + session.getString("responseBody"));
        }
        return session;
    });

    HttpProtocolBuilder httpProtocol =
//            http.baseUrl("https://api.openrouteservice.org")
            http.baseUrl("http://localhost:8082/ors")
                    .authorizationHeader("Bearer API_KEY")
                    .acceptHeader("application/geo+json; charset=utf-8")
                    .contentTypeHeader("application/json; charset=utf-8")
                    .userAgentHeader(
                            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10.15; rv:109.0) Gecko/20100101 Firefox/119.0"
                    );

    ScenarioBuilder users = scenario("Users").exec(request);

    {
        setUp(
                users.injectOpen(rampUsers(10).during(10))
        ).protocols(httpProtocol);
    }
}
