package com.example.support;

import io.restassured.path.json.JsonPath;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;


public class JsonAsserts {

    public static void assertCurrentTypes(JsonPath jp) {
        assertNotNull(jp.get("location.name"));
        assertTrue(jp.get("location.lat") instanceof Float || jp.get("location.lat") instanceof Double);
        assertTrue(jp.get("location.lon") instanceof Float || jp.get("location.lon") instanceof Double);
        assertTrue(jp.get("current.temp_c") instanceof Number);
        assertTrue(jp.get("current.condition.text") instanceof String);
    }

    public static void assertForecastDays(JsonPath jp, int expectedDays) {
        List<Map<String, Object>> days = jp.getList("forecast.forecastday");
        assertNotNull(days);
        assertEquals( "Mismatch in forecast days", days.size(), expectedDays);
        Map<String, Object> first = days.get(0);
        assertTrue(first.get("date") instanceof String);
        Map<?, ?> day = (Map<?, ?>) first.get("day");
        assertTrue(day.get("maxtemp_c") instanceof Number);
        assertTrue(day.get("mintemp_c") instanceof Number);
    }
}
