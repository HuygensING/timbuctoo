package nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.dto;

import org.immutables.value.Value;

import java.util.Set;

@Value.Immutable
public interface TypedValue {

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
