package nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers;

import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.dto.TypedValue;

public interface UriFetcher {
  String getUri(TypedValue source);
}
