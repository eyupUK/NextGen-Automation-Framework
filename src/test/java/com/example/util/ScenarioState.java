package com.example.util;

import io.restassured.response.Response;

public class ScenarioState {
    private Response lastResponse;
    private String lastQuery;
    private int lastRequestedDays;

    public Response getLastResponse() { return lastResponse; }
    public void setLastResponse(Response r) { this.lastResponse = r; }

    public String getLastQuery() { return lastQuery; }
    public void setLastQuery(String q) { this.lastQuery = q; }

    public int getLastRequestedDays() { return lastRequestedDays; }
    public void setLastRequestedDays(int days) { this.lastRequestedDays = days; }
}

