package nl.knaw.huygens.timbuctoo.graphql.rootquery.dataproviders;

import org.immutables.value.Value;

@Value.Immutable
public interface ContactInfo {
  String getName();

  String getEmail();

  static ContactInfo contactInfo(String name, String email) {
    return ImmutableContactInfo.builder()
      .name(name)
      .email(email)
      .build();
  }
}
