package nl.knaw.huygens.timbuctoo.serializable.dto;

import nl.knaw.huygens.timbuctoo.serializable.dto.ImmutableValue;
import org.slf4j.Logger;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Optional;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Represents a leaf node that contains the actual data. In the graphql query it's the object that is represented by
 * {value}
 */
@org.immutables.value.Value.Immutable
public interface Value extends RdfData {
  Logger LOG = getLogger(Value.class);

  String getType();

  String getValue();

  Optional<String> getGraphqlTypeName();

  static Value create(String value, String type) {
    return ImmutableValue.builder()
      .value(value)
      .type(type)
      .build();
  }

  static Value create(String value, String type, String graphqlType) {
    return ImmutableValue.builder()
      .value(value)
      .type(type)
      .graphqlTypeName(graphqlType)
      .build();
  }

  default Serializable withGraphqlType(String graphqlType) {
    return ImmutableValue.copyOf(this).withGraphqlTypeName(graphqlType);
  }

  static Value fromRawJavaType(Object value) {
    switch (value) {
      case null -> {
        return null;
      }
      case Integer i -> {
        return create(value + "", "http://www.w3.org/2001/XMLSchema#int");
      }
      case Long l -> {
        return create(value + "", "http://www.w3.org/2001/XMLSchema#long");
      }
      case Short i -> {
        return create(value + "", "http://www.w3.org/2001/XMLSchema#short");
      }
      case Byte b -> {
        return create(value + "", "http://www.w3.org/2001/XMLSchema#byte");
      }
      case Double v -> {
        return create(value + "", "http://www.w3.org/2001/XMLSchema#double");
      }
      case BigInteger bigInteger -> {
        return create(value + "", "http://www.w3.org/2001/XMLSchema#integer");
      }
      case BigDecimal bigDecimal -> {
        return create(value + "", "http://www.w3.org/2001/XMLSchema#decimal");
      }
      case String s -> {
        return create(s, "http://www.w3.org/2001/XMLSchema#string");
      }
      case Boolean b -> {
        return create(b ? "true" : "false", "http://www.w3.org/2001/XMLSchema#boolean");
      }
      case Character c -> {
        return create(value + "", "http://www.w3.org/2001/XMLSchema#string");
      }
      default -> {
        LOG.error("Unknown type: " + value.getClass());
        return create(value + "", "http://www.w3.org/2001/XMLSchema#string");
      }
    }
  }
}
