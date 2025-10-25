package com.example.mock;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

/**
 * A tiny embedded HTTP server that serves a deterministic mock of SauceDemo pages
 * sufficient for the @demo checkout scenario. It hosts a login page, an inventory page
 * with fixed prices, a cart page, checkout info, overview, and completion page.
 *
 * Start/stop via start(0) and stop(). Use base URL: http://localhost:<port>/
 */
public class MockServer {
    private HttpServer server;
    private int port;

    public void start(int port) {
        try {
            this.server = HttpServer.create(new InetSocketAddress(port), 0);
            this.server.createContext("/", new StaticHandler(Pages.LOGIN));
            this.server.createContext("/inventory.html", new StaticHandler(Pages.INVENTORY));
            this.server.createContext("/cart.html", new StaticHandler(Pages.CART));
            this.server.createContext("/checkout-step-one.html", new StaticHandler(Pages.CHECKOUT_INFO));
            this.server.createContext("/checkout-step-two.html", new StaticHandler(Pages.CHECKOUT_OVERVIEW));
            this.server.createContext("/checkout-complete.html", new StaticHandler(Pages.CHECKOUT_COMPLETE));
            this.server.setExecutor(null);
            this.server.start();
            this.port = ((InetSocketAddress) server.getAddress()).getPort();
            System.out.println("[MockServer] Started at http://localhost:" + this.port);
        } catch (IOException e) {
            throw new RuntimeException("Failed to start MockServer", e);
        }
    }

    public void stop() {
        if (server != null) {
            server.stop(0);
            server = null;
            System.out.println("[MockServer] Stopped");
        }
    }

    public String baseUrl() { return "http://localhost:" + this.port + "/"; }

    private static class StaticHandler implements HttpHandler {
        private final String content;
        StaticHandler(String content) { this.content = content; }
        @Override public void handle(HttpExchange exchange) throws IOException {
            byte[] bytes = content.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "text/html; charset=utf-8");
            exchange.sendResponseHeaders(200, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) { os.write(bytes); }
        }
    }

    // Minimal HTMLs using the same data-test and id attributes our selectors rely on.
    static class Pages {
        static final String LOGIN = "" +
                "<html><head><title>Login</title></head><body>" +
                "<input id='user-name'/><input id='password' type='password'/>" +
                "<button id='login-button' onclick=\"location.href='inventory.html'\">Login</button>" +
                "</body></html>";

        static final String INVENTORY = "" +
                "<html><head><title>Inventory</title></head><body>" +
                "<div class='title'>Products</div>" +
                // Three fixed items with deterministic prices
                "<div class='inventory_item'>" +
                " <div data-test='inventory-item-name'>Alpha</div>" +
                " <div class='inventory_item_price'>$9.99</div>" +
                " <button data-test='add-to-cart-alpha' onclick=\"addToCart()\">Add</button>" +
                "</div>" +
                "<div class='inventory_item'>" +
                " <div data-test='inventory-item-name'>Bravo</div>" +
                " <div class='inventory_item_price'>$29.99</div>" +
                " <button data-test='add-to-cart-bravo' onclick=\"addToCart()\">Add</button>" +
                "</div>" +
                "<div class='inventory_item'>" +
                " <div data-test='inventory-item-name'>Charlie</div>" +
                " <div class='inventory_item_price'>$49.99</div>" +
                " <button data-test='add-to-cart-charlie' onclick=\"addToCart()\">Add</button>" +
                "</div>" +
                "<div id='shopping_cart_container' onclick=\"location.href='cart.html'\">Cart</div>" +
                "<script>function addToCart(){document.querySelector('.shopping_cart_badge')?" +
                "document.querySelector('.shopping_cart_badge').innerText=1:(function(){var b=document.createElement('span');b.className='shopping_cart_badge';b.innerText='1';document.body.appendChild(b)})()}</script>" +
                "</body></html>";

        static final String CART = "" +
                "<html><head><title>Cart</title></head><body>" +
                "<div class='title'>Your Cart</div>" +
                "<div class='cart_item'><div class='inventory_item_name'>Charlie</div></div>" +
                "<button id='checkout' onclick=\"location.href='checkout-step-one.html'\">Checkout</button>" +
                "</body></html>";

        static final String CHECKOUT_INFO = "" +
                "<html><head><title>Checkout Info</title></head><body>" +
                "<div class='title'>Checkout: Your Information</div>" +
                "<input id='first-name'/><input id='last-name'/><input id='postal-code'/>" +
                "<button id='continue' onclick=\"location.href='checkout-step-two.html'\">Continue</button>" +
                "</body></html>";

        static final String CHECKOUT_OVERVIEW = "" +
                "<html><head><title>Checkout Overview</title></head><body>" +
                "<div class='title'>Checkout: Overview</div>" +
                "<button id='finish' onclick=\"location.href='checkout-complete.html'\">Finish</button>" +
                "</body></html>";

        static final String CHECKOUT_COMPLETE = "" +
                "<html><head><title>Complete</title></head><body>" +
                "<h2 class='complete-header'>Thank you for your order!</h2>" +
                "<button id='back-to-products' onclick=\"location.href='inventory.html'\">Back</button>" +
                "</body></html>";
    }
}

