package nl.knaw.huygens.timbuctoo.graphql.rootquery.dataproviders;

import org.immutables.value.Value;

@Value.Immutable
public interface MimeTypeDescription {
  String getName();

  static MimeTypeDescription create(String name) {
    return ImmutableMimeTypeDescription.builder().name(name).build();
  }
}
