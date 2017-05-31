package nl.knaw.huygens.timbuctoo.v5.bdbdatafetchers.datafetchers;

import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.UriFetcher;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.dto.TypedValue;

public class UriDataFetcher implements UriFetcher {

  @Override
  public String getUri(TypedValue source) {
    return source.getValue();
  }
}
