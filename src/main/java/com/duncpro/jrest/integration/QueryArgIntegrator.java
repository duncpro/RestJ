package com.duncpro.jrest.integration;

import javax.inject.Qualifier;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static java.util.Objects.requireNonNull;

@Qualifier
@Retention(RetentionPolicy.RUNTIME)
public @interface QueryArgIntegrator {
}
