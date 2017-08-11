package nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.dto;

import org.immutables.value.Value;
import org.slf4j.Logger;

import java.util.Set;

import static org.slf4j.LoggerFactory.getLogger;

@Value.Immutable
// FIXME: RED-107 This class is the combination of both TypedValue and TypedEntity.
public interface TypedValue {

  Logger LOG = getLogger(TypedValue.class);

  String getValue();

  Set<String> getType();

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
