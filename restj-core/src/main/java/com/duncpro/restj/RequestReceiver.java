package com.duncpro.restj;

import javax.inject.Qualifier;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation is only required when the enclosing class is annotated with {@link com.duncpro.restj.HttpEndpoint}.
 * This annotation and {@link WebSocketEventReceiver} are mutually exclusive.
 */
@Qualifier // Internally used as a qualifier. Not meant for injections within application code.
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER, ElementType.FIELD, ElementType.METHOD})
public @interface RequestReceiver {
}
