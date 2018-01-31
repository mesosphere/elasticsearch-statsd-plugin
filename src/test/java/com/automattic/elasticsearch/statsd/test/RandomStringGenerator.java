package com.automattic.elasticsearch.statsd.test;

import java.util.UUID;

public class RandomStringGenerator {

    /**
     * <p>
     * Creates a random string whose length is the number of characters specified.
     * </p>
     * <p/>
     * <p>
     * Characters will be chosen from the set of alpha-numeric characters.
     * </p>
     *
     * @param count the length of random string to create
     * @return the random string
     */
    public static String randomAlphabetic(int count) {
        return UUID.randomUUID().toString().replaceAll("-", "").substring(0, count);
    }

}