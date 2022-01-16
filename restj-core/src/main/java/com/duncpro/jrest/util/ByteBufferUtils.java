package com.duncpro.jrest.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class ByteBufferUtils {
    public static byte[] concat(byte[]... buffers) {
       final var bos = new ByteArrayOutputStream();
       for (final var buffer : buffers) {
           try {
               bos.write(buffer);
           } catch (IOException e) {
               throw new AssertionError(e);
           }
       }
       return bos.toByteArray();
    }
}
