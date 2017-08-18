package nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.dto;

import org.immutables.value.Value;

@Value.Immutable
public interface TypedValue extends DatabaseResult {

  String getValue();

  String getType();

  static TypedValue create(String value, String type) {
    return ImmutableTypedValue.builder()
      .value(value)
      .type(type)
      .build();
  }

}
