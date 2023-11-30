package nl.knaw.huygens.timbuctoo.graphql.rootquery.dataproviders;

import org.immutables.value.Value;

@Value.Immutable
public interface PrefixMapping {
  String getPrefix();

  String getUri();
}
