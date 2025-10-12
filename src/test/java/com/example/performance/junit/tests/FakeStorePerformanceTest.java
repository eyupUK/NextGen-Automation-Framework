package com.example.performance.junit.tests;

import com.example.util.ConfigurationReader;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static io.restassured.RestAssured.given;
import static org.junit.Assert.assertTrue;

public class FakeStorePerformanceTest {

    private ExecutorService exec;
    private RequestSpecification spec;

    @Before
    public void setUp() {
        String baseUrl = ConfigurationReader.get("fake_store_api_base_url");
        if (baseUrl == null || baseUrl.isBlank()) {
            baseUrl = "https://fakestoreapi.com";
        }
        spec = new RequestSpecBuilder().setBaseUri(baseUrl).build();
        exec = Executors.newFixedThreadPool(10);
    }

    @After
    public void tearDown() throws InterruptedException {
        if (exec != null) {
            exec.shutdown();
            exec.awaitTermination(30, TimeUnit.SECONDS);
        }
    }

    @Test
    public void testProductsEndpointUnderLoad() throws Exception {
        int requests = 100;
        List<Future<Response>> futures = new ArrayList<>(requests);
        AtomicInteger success = new AtomicInteger();

        for (int i = 0; i < requests; i++) {
            futures.add(exec.submit(() -> given().spec(spec).get("/products")));
        }
        for (Future<Response> f : futures) {
            try {
                Response r = f.get(10, TimeUnit.SECONDS);
                if (r.statusCode() == 200) success.incrementAndGet();
            } catch (Exception e) {
                // ignore; counted as failure
            }
        }
        double rate = success.get() / (double) requests;
        System.out.println("FakeStore /products success rate: " + String.format("%.2f%%", rate * 100));
        assertTrue("Success rate below 95%", rate >= 0.95);
    }

    @Test
    public void testProductDetailUnderLoad() throws Exception {
        int requests = 50;
        List<Future<Response>> futures = new ArrayList<>(requests);
        AtomicInteger success = new AtomicInteger();

        for (int i = 0; i < requests; i++) {
            futures.add(exec.submit(() -> given().spec(spec).get("/products/1")));
        }
        for (Future<Response> f : futures) {
            try {
                Response r = f.get(10, TimeUnit.SECONDS);
                if (r.statusCode() == 200) success.incrementAndGet();
            } catch (Exception e) {
                // ignore
            }
        }
        double rate = success.get() / (double) requests;
        System.out.println("FakeStore /products/1 success rate: " + String.format("%.2f%%", rate * 100));
        assertTrue("Success rate below 98%", rate >= 0.98);
    }
}

