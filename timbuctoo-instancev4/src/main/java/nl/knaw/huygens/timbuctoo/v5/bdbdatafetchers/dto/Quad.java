package nl.knaw.huygens.timbuctoo.v5.bdbdatafetchers.dto;

import org.immutables.value.Value;

import java.util.Optional;

@Value.Immutable
public interface Quad {
  String getSubject();

  String getPredicate();

  String getObject();

  Optional<String> getValuetype();

  Optional<String> getLanguage();

  String getGraph();

  static Quad create(String subject, String predicate, String object, String valueType, String language, String graph) {
    return ImmutableQuad.builder()
      .subject(subject)
      .predicate(predicate)
      .object(object)
      .valuetype(Optional.ofNullable(valueType))
      .language(Optional.ofNullable(language))
      .graph(graph)
      .build();
  }


}
