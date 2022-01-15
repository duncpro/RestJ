package com.duncpro.jrest.integration;

import static java.util.Objects.requireNonNull;

public interface StringSerializer {
    String serialize(Object o);
}
