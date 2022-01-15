package com.duncpro.jrest.util;

import java.util.HashMap;
import java.util.Map;

import static java.util.Objects.requireNonNull;

public class URLUtils {
    public static Map<String, String> parseQueryParams(String s) {
        final var params = new HashMap<String, String>();
        if (s == null) return params;

        final var pairs = s.split("&");
        for (String item : pairs) {
            if (item.isBlank()) continue;
            final var pair = item.split("=");
            params.put(pair[0], pair[1]);
        }

        return params;
    }
}
