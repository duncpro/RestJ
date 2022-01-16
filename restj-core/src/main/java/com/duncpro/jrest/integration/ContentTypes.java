package com.duncpro.jrest.integration;

import com.google.auto.value.AutoAnnotation;

public class ContentTypes {
    @AutoAnnotation
    public static ContentType get(String value) {
        return new AutoAnnotation_ContentTypes_get(value.toLowerCase());
    }
}
