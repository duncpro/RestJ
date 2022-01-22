package com.duncpro.restj.integration;

import java.util.concurrent.Flow;

public interface BodySerializer {
    Flow.Publisher<byte[]> serialize(Object body);
}
