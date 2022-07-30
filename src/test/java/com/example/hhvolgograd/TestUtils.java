package com.example.hhvolgograd;

import org.apache.commons.lang3.RandomStringUtils;

import java.util.Random;

public class TestUtils {


    public static String randomStringWithNonZeroLength() {
        return RandomStringUtils.random(1 + new Random().nextInt(99));
    }
}
