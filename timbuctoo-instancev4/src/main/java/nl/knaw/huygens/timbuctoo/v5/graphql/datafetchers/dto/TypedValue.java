package nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.dto;

import org.immutables.value.Value;
import org.slf4j.Logger;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Set;

import static org.slf4j.LoggerFactory.getLogger;

@Value.Immutable
public interface TypedValue {

  Logger LOG = getLogger(TypedValue.class);

  String getValue();

  Set<String> getType();

  static TypedValue createFromNative(Object value) {
    if (value == null) {
      return null;
    } else if (value instanceof Integer) {
      return create(value + "", "http://www.w3.org/TR/xmlschema11-2/#int");
    } else if (value instanceof Long) {
      return create(value + "", "http://www.w3.org/TR/xmlschema11-2/#long");
    } else if (value instanceof Short) {
      return create(value + "", "http://www.w3.org/TR/xmlschema11-2/#short");
    } else if (value instanceof Byte) {
      return create(value + "", "http://www.w3.org/TR/xmlschema11-2/#byte");
    } else if (value instanceof Double) {
      return create(value + "", "http://www.w3.org/TR/xmlschema11-2/#double");
    } else if (value instanceof BigInteger) {
      return create(value + "", "http://www.w3.org/TR/xmlschema11-2/#integer");
    } else if (value instanceof BigDecimal) {
      return create(value + "", "http://www.w3.org/TR/xmlschema11-2/#decimal");
    } else if (value instanceof String) {
      return create((String) value, "http://www.w3.org/TR/xmlschema11-2/#string");
    } else if (value instanceof Boolean) {
      return create((Boolean) value ? "true" : "false", "http://www.w3.org/TR/xmlschema11-2/#boolean");
    } else if (value instanceof Character) {
      return create(value + "", "http://www.w3.org/TR/xmlschema11-2/#string");
    } else {
      LOG.error("Unknown type: " + value.getClass());
      return create(value + "", "http://www.w3.org/TR/xmlschema11-2/#string");
    }
  }

  static TypedValue create(String value) {
    return ImmutableTypedValue.builder()
      .value(value)
      .build();
  }

  static TypedValue create(String value, String type) {
    return ImmutableTypedValue.builder()
      .value(value)
      .addType(type)
      .build();
  }

  static TypedValue create(String value, Set<String> types) {
    return ImmutableTypedValue.builder()
      .value(value)
      .type(types)
      .build();
  }

}
