package com.example.performance.config;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class PerformanceConfigInitTest {

    @Test
    public void usersDefaultWhenBlankProperty() {
        // Accessing USERS triggers static initialization of PerformanceConfig
        int users = PerformanceConfig.USERS;
        assertTrue("USERS should default to a positive number when perf.users is blank", users > 0);
    }
}

