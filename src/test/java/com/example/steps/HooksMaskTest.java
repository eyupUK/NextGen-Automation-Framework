package com.example.steps;

import org.junit.Test;

import java.lang.reflect.Method;

import static org.junit.Assert.*;

public class HooksMaskTest {

    private String invokeMask(String input) throws Exception {
        Method m = Hooks.class.getDeclaredMethod("mask", String.class);
        m.setAccessible(true);
        return (String) m.invoke(null, input);
    }

    @Test
    public void masksNullAndEmpty() throws Exception {
        assertEquals("<empty>", invokeMask(null));
        assertEquals("<empty>", invokeMask(""));
        assertEquals("<empty>", invokeMask("   ")); // blanks treated as empty
    }

    @Test
    public void masksVeryShortStrings() throws Exception {
        assertEquals("***", invokeMask("a"));
        assertEquals("***", invokeMask("ab"));
        assertEquals("***", invokeMask("abc"));
        assertEquals("***", invokeMask("abcd"));
    }

    @Test
    public void masksFiveAndSixCharStrings() throws Exception {
        // len=5 -> first 2 + *** + last 2
        assertEquals("ab***de", invokeMask("abcde"));
        assertEquals("12***45", invokeMask("12345"));

        // len=6 -> first 3 + *** + last 2
        assertEquals("abc***ef", invokeMask("abcdef"));
        assertEquals("123***56", invokeMask("123456"));
    }

    @Test
    public void masksLongIds() throws Exception {
        assertEquals("abcd***yz", invokeMask("abcdefghijklmnopqrstuvwxyz"));
        assertEquals("clie***45", invokeMask("clientid12345"));
        assertEquals("test***89", invokeMask("test_secret_89"));
    }
}

