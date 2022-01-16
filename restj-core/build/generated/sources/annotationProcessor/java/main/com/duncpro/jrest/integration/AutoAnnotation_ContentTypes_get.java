package com.duncpro.jrest.integration;

import java.io.Serializable;
import javax.annotation.processing.Generated;

@Generated("com.google.auto.value.processor.AutoAnnotationProcessor")
final class AutoAnnotation_ContentTypes_get implements ContentType, Serializable {
  private static final long serialVersionUID = -4770846455854161599L;

  private final String value;

  AutoAnnotation_ContentTypes_get(
      String value) {
    if (value == null) {
      throw new NullPointerException("Null value");
    }
    this.value = value;
  }

  @Override
  public Class<? extends ContentType> annotationType() {
    return ContentType.class;
  }

  @Override
  public String value() {
    return value;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder("@com.duncpro.jrest.integration.ContentType(");
    appendQuoted(sb, value);
    return sb.append(')').toString();
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (o instanceof ContentType) {
      ContentType that = (ContentType) o;
      return value.equals(that.value());
    }
    return false;
  }

  @Override
  public int hashCode() {
    return
        + (1335633679 ^ value.hashCode())
    // 1335633679 is 127 * "value".hashCode()
    ;
  }

  private static void appendQuoted(StringBuilder sb, String s) {
    sb.append('"');
    for (int i = 0; i < s.length(); i++) {
      appendEscaped(sb, s.charAt(i));
    }
    sb.append('"');
  }

  private static void appendEscaped(StringBuilder sb, char c) {
    switch (c) {
      case '\\':
      case '"':
      case '\'':
      sb.append('\\').append(c);
      break;
      case '\n':
      sb.append("\\n");
      break;
      case '\r':
      sb.append("\\r");
      break;
      case '\t':
      sb.append("\\t");
      break;
      default:
      if (c < 0x20) {
        sb.append('\\');
        appendWithZeroPadding(sb, Integer.toOctalString(c), 3);
      } else if (c < 0x7f || Character.isLetter(c)) {
        sb.append(c);
      } else {
        sb.append("\\u");
        appendWithZeroPadding(sb, Integer.toHexString(c), 4);
      }
      break;
    }
  }

  private static void appendWithZeroPadding(StringBuilder sb, String s, int width) {
    for (int i = width - s.length(); i > 0; i--) {
      sb.append('0');
    }
    sb.append(s);
  }
}
