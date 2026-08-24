package com.ap01.url_shortener.utils;

public class Base62 {

    private static final String CHARSET =
            "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";

    private static final int BASE = 62;

    public static String encode(long id) {

        if (id < 0) {
            throw new IllegalArgumentException("ID must be non-negative");
        }

        if (id == 0) {
            return String.valueOf(CHARSET.charAt(0));
        }

        StringBuilder sb = new StringBuilder();

        while (id > 0) {
            int remainder = (int) (id % BASE);
            sb.append(CHARSET.charAt(remainder));
            id = id / BASE;
        }

        return sb.reverse().toString();
    }

    public static long decode(String base62) {

        if (base62 == null || base62.isEmpty()) {
            throw new IllegalArgumentException(
                    "Base62 value cannot be null or empty"
            );
        }

        long result = 0;

        for (int i = 0; i < base62.length(); i++) {

            char c = base62.charAt(i);

            int index = CHARSET.indexOf(c);

            if (index == -1) {
                throw new IllegalArgumentException(
                        "Illegal character: " + c
                );
            }

            result = (result * BASE) + index;
        }

        return result;
    }
}