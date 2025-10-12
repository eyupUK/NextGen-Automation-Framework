package com.example.performance.gatling.simulations;

import com.example.performance.config.PerformanceConfig;
import io.gatling.javaapi.core.*;
import io.gatling.javaapi.http.*;

import java.time.Duration;
import java.util.List;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;

public class EcommerceApiPerformanceSimulation extends Simulation {

    private final HttpProtocolBuilder httpProtocol = http
            .baseUrl(PerformanceConfig.FAKESTORE_API_BASE_URL)
            .acceptHeader("application/json")
            .contentTypeHeader("application/json")
            .userAgentHeader("Performance-Test-Suite/1.0");

    private final FeederBuilder<String> productIdFeeder = csv("performance/product_ids.csv").circular();

    private final ScenarioBuilder browseProductsScenario = scenario("Browse Products")
            .exec(
                    http("Get All Products")
                            .get("/products")
                            .check(status().is(200))
                            .check(jsonPath("$[*].id").findAll().saveAs("productIds"))
                            .check(responseTimeInMillis().lte(1500))
            )
            .pause(Duration.ofSeconds(1), Duration.ofSeconds(3))
            .exec(
                    http("Get Products by Category - Electronics")
                            .get("/products/category/electronics")
                            .check(status().is(200))
                            .check(responseTimeInMillis().lte(1000))
            )
            .pause(Duration.ofSeconds(2))
            .exec(
                    http("Get Single Product Details")
                            .get("/products/1")
                            .check(status().is(200))
                            .check(jsonPath("$.title").exists())
                            .check(jsonPath("$.price").exists())
            );

    private final ScenarioBuilder cartOperationsScenario = scenario("Shopping Cart Operations")
            .exec(
                    http("Get All Carts")
                            .get("/carts")
                            .check(status().is(200))
                            .check(responseTimeInMillis().lte(1500))
            )
            .pause(Duration.ofSeconds(1))
            .exec(
                    http("Get User Cart")
                            .get("/carts/user/1")
                            .check(status().is(200))
            )
            .pause(Duration.ofSeconds(2))
            .exec(
                    http("Create New Cart")
                            .post("/carts")
                            .body(StringBody("""
                                    {
                                        "userId": 1,
                                        "date": "2025-10-08",
                                        "products": [
                                            {"productId": 1, "quantity": 2},
                                            {"productId": 5, "quantity": 1}
                                        ]
                                    }
                                    """))
                            .check(status().is(200))
                            .check(jsonPath("$.id").exists())
                            .check(responseTimeInMillis().lte(2000))
            );

    private final ScenarioBuilder authScenario = scenario("User Authentication")
            .exec(
                    http("Login User")
                            .post("/auth/login")
                            .body(StringBody("""
                                    {
                                        "username": "johnd",
                                        "password": "m38rmF$"
                                    }
                                    """))
                            .check(status().is(200))
                            .check(jsonPath("$.token").exists().saveAs("authToken"))
                            .check(responseTimeInMillis().lte(1000))
            )
            .pause(Duration.ofSeconds(1))
            .exec(
                    http("Get User Profile")
                            .get("/users/1")
                            .check(status().is(200))
            );

    // Injection profiles using PerformanceConfig
    private final List<OpenInjectionStep> browseProductsProfile = List.of(
            rampUsers(PerformanceConfig.USERS).during(Duration.ofSeconds(PerformanceConfig.RAMP_UP_TIME)),
            constantUsersPerSec(PerformanceConfig.USERS).during(Duration.ofSeconds(PerformanceConfig.DURATION))
    );

    private final List<OpenInjectionStep> cartOperationsProfile = List.of(
            rampUsers(PerformanceConfig.USERS / 2).during(Duration.ofSeconds(PerformanceConfig.RAMP_UP_TIME)),
            constantUsersPerSec(PerformanceConfig.USERS / 2).during(Duration.ofSeconds(PerformanceConfig.DURATION))
    );

    private final List<OpenInjectionStep> authProfile = List.of(
            rampUsers(PerformanceConfig.USERS / 4).during(Duration.ofSeconds(PerformanceConfig.RAMP_UP_TIME)),
            constantUsersPerSec(PerformanceConfig.USERS / 4).during(Duration.ofSeconds(PerformanceConfig.DURATION))
    );

    {
        setUp(
                browseProductsScenario.injectOpen(browseProductsProfile.toArray(new OpenInjectionStep[0])).protocols(httpProtocol),
                cartOperationsScenario.injectOpen(cartOperationsProfile.toArray(new OpenInjectionStep[0])).protocols(httpProtocol),
                authScenario.injectOpen(authProfile.toArray(new OpenInjectionStep[0])).protocols(httpProtocol)
        )
                .assertions(
                        global().responseTime().percentile3().lte(2000),
                        global().responseTime().percentile4().lte(3000),
                        global().successfulRequests().percent().gte(95.0),
                        forAll().failedRequests().percent().lte(5.0)
                );
    }
}