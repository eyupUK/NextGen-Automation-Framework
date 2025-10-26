package com.example.mock;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Minimal embedded HTTP server to emulate WeatherAPI for demo runs (@api tests).
 * Endpoints:
 *  - GET /current.json?q=... -> 200 with deterministic payload
 *  - GET /current.json (no q) -> 400 error code 1003
 *  - GET /current.json?q=this-is-not-a-real-place-xyz -> 400 error code 1006
 *  - POST /current.json?q=bulk -> 400 error code 2009 (free plan restriction)
 *  - GET /forecast.json?q=...&days=N -> 200 with N forecast days
 */
public class WeatherApiMockServer {
    private HttpServer server;
    private int port;

    public void start(int port) {
        try {
            server = HttpServer.create(new InetSocketAddress(port), 0);
            server.createContext("/current.json", new CurrentHandler());
            server.createContext("/forecast.json", new ForecastHandler());
            server.setExecutor(null);
            server.start();
            this.port = ((InetSocketAddress) server.getAddress()).getPort();
            System.out.println("[WeatherApiMock] Started at " + baseUrl());
        } catch (IOException e) {
            throw new RuntimeException("Failed to start WeatherApiMockServer", e);
        }
    }

    public void stop() {
        if (server != null) {
            server.stop(0);
            server = null;
            System.out.println("[WeatherApiMock] Stopped");
        }
    }

    public String baseUrl() {
        return "http://localhost:" + this.port;
    }

    private static class CurrentHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            URI uri = exchange.getRequestURI();
            Map<String, String> query = parseQuery(uri.getRawQuery());
            String method = exchange.getRequestMethod();

            // Missing q
            if (!query.containsKey("q") || query.get("q").isBlank()) {
                writeJson(exchange, 400, error(1003, "Parameter q is missing"));
                return;
            }

            String q = query.get("q");
            if ("POST".equalsIgnoreCase(method) && "bulk".equalsIgnoreCase(q)) {
                writeJson(exchange, 400, error(2009, "does not have access"));
                return;
            }

            if ("this-is-not-a-real-place-xyz".equalsIgnoreCase(q)) {
                writeJson(exchange, 400, error(1006, "No location found matching parameter 'q'"));
                return;
            }

            // Success payload matching current_schema.json and typed checks
            Map<String, Object> payload = new HashMap<>();
            Map<String, Object> location = new HashMap<>();
            location.put("name", deriveName(q));
            location.put("country", deriveCountry(q));
            location.put("lat", 51.5074);
            location.put("lon", -0.1278);
            payload.put("location", location);

            Map<String, Object> condition = new HashMap<>();
            condition.put("text", "Partly cloudy");
            condition.put("icon", "//cdn.weatherapi.com/weather/64x64/day/116.png");

            Map<String, Object> current = new HashMap<>();
            current.put("temp_c", 18.5);
            current.put("condition", condition);
            current.put("last_updated_epoch", 1690000000);
            payload.put("current", current);

            writeJson(exchange, 200, Json.write(payload));
        }
    }

    private static class ForecastHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            URI uri = exchange.getRequestURI();
            Map<String, String> query = parseQuery(uri.getRawQuery());
            if (!query.containsKey("q") || query.get("q").isBlank()) {
                writeJson(exchange, 400, error(1003, "Parameter q is missing"));
                return;
            }
            int days = 1;
            try {
                days = Math.max(1, Math.min(14, Integer.parseInt(query.getOrDefault("days", "1"))));
            } catch (NumberFormatException ignored) {}

            Map<String, Object> location = new HashMap<>();
            location.put("name", deriveName(query.get("q")));
            location.put("country", deriveCountry(query.get("q")));
            location.put("lat", 51.5074);
            location.put("lon", -0.1278);

            List<Map<String, Object>> forecastDays = new ArrayList<>();
            for (int i = 0; i < days; i++) {
                Map<String, Object> day = new HashMap<>();
                day.put("date", "2025-10-" + String.format("%02d", (10 + i)));
                Map<String, Object> dayObj = new HashMap<>();
                dayObj.put("maxtemp_c", 20.0 + i);
                dayObj.put("mintemp_c", 10.0 + i);
                day.put("day", dayObj);
                forecastDays.add(day);
            }

            Map<String, Object> forecast = new HashMap<>();
            forecast.put("forecastday", forecastDays);

            Map<String, Object> payload = new HashMap<>();
            payload.put("location", location);
            payload.put("forecast", forecast);

            writeJson(exchange, 200, Json.write(payload));
        }
    }

    // --- helpers ---
    private static String error(int code, String messageContains) {
        String msg = messageContains;
        return Json.write(Map.of("error", Map.of("code", code, "message", msg)));
    }

    private static void writeJson(HttpExchange ex, int status, String json) throws IOException {
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().add("Content-Type", "application/json; charset=utf-8");
        ex.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = ex.getResponseBody()) { os.write(bytes); }
    }

    private static Map<String, String> parseQuery(String raw) {
        Map<String, String> map = new HashMap<>();
        if (raw == null || raw.isBlank()) return map;
        for (String pair : raw.split("&")) {
            int idx = pair.indexOf('=');
            if (idx > 0) {
                String key = decode(pair.substring(0, idx));
                String val = decode(pair.substring(idx + 1));
                map.put(key, val);
            } else {
                map.put(decode(pair), "");
            }
        }
        return map;
    }

    private static String decode(String s) {
        return s.replace("+", " ");
    }

    private static String deriveCountry(String q) {
        if (q == null) return "United Kingdom";
        String qq = q.toLowerCase();
        if ("london".equals(qq)) return "United Kingdom";
        if ("90201".equals(qq)) return "USA";
        if ("48.8567,2.3508".equals(qq)) return "France";
        if ("sw1".equals(qq)) return "UK";
        return "United Kingdom";
    }

    private static String deriveName(String q) {
        if (q == null || q.isBlank()) return "Mock City";
        if ("90201".equals(q)) return "Commerce";
        if ("48.8567,2.3508".equals(q)) return "Paris";
        if ("sw1".equalsIgnoreCase(q)) return "London SW1";
        return capitalize(q);
    }

    private static String capitalize(String s) {
        if (s.isEmpty()) return s;
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }

    // Tiny inline JSON writer to avoid external deps
    static class Json {
        static String write(Object obj) {
            if (obj instanceof Map) {
                StringBuilder sb = new StringBuilder("{");
                boolean first = true;
                for (Map.Entry<?, ?> e : ((Map<?, ?>) obj).entrySet()) {
                    if (!first) sb.append(','); first = false;
                    sb.append('"').append(escape(String.valueOf(e.getKey()))).append('"').append(':').append(write(e.getValue()));
                }
                return sb.append('}').toString();
            } else if (obj instanceof List) {
                StringBuilder sb = new StringBuilder("[");
                boolean first = true;
                for (Object o : (List<?>) obj) { if (!first) sb.append(','); first = false; sb.append(write(o)); }
                return sb.append(']').toString();
            } else if (obj instanceof String) {
                return '"' + escape((String) obj) + '"';
            } else if (obj instanceof Number || obj instanceof Boolean) {
                return String.valueOf(obj);
            } else if (obj == null) {
                return "null";
            } else {
                return '"' + escape(obj.toString()) + '"';
            }
        }
        private static String escape(String s) { return s.replace("\\", "\\\\").replace("\"", "\\\""); }
    }
}

