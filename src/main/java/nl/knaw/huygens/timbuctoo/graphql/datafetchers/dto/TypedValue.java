package nl.knaw.huygens.timbuctoo.graphql.datafetchers.dto;

import nl.knaw.huygens.timbuctoo.dataset.dto.DataSet;
import org.immutables.value.Value;

@Value.Immutable
public interface TypedValue extends DatabaseResult {
  String getValue();

  String getType();

  static TypedValue create(String value, String type, DataSet dataSet) {
    return ImmutableTypedValue.builder()
      .value(value)
      .type(type)
      .dataSet(dataSet)
      .build();
  }
}
