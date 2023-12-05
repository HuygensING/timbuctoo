package nl.knaw.huygens.timbuctoo.graphql.rootquery.dataproviders;

import org.immutables.value.Value;

@Value.Immutable
public interface License {

  String getUri();

  static License license(String uri) {
    return ImmutableLicense.builder()
      .uri(uri)
      .build();
  }
}
