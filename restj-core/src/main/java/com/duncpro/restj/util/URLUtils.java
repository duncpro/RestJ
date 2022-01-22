package com.duncpro.restj.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class URLUtils {
    public static Map<String, List<String>> parseQueryParams(String s) {
        final var params = new HashMap<String, List<String>>();
        if (s == null) return params;

        final var pairs = s.split("&");
        for (String item : pairs) {
            if (item.isBlank()) continue;
            final var pair = item.split("=");
            params.merge(pair[0], List.of(pair[1]),
                    (a, b) -> Stream.concat(a.stream(), b.stream()).collect(Collectors.toList()));
        }

        return params;
    }
}
