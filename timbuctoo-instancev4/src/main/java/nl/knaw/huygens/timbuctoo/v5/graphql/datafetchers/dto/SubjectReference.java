package nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.dto;

import org.immutables.value.Value;

import java.util.Set;

@Value.Immutable
public interface SubjectReference extends DatabaseResult {

  String getValue();

  Set<String> getTypes();

  static SubjectReference create(String value) {
    return ImmutableSubjectReference.builder()
      .value(value)
      .build();
  }

  static SubjectReference create(String value, Iterable<String> types) {
    return ImmutableSubjectReference.builder()
      .value(value)
      .types(types)
      .build();
  }

}
