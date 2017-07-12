package nl.knaw.huygens.timbuctoo.v5.bdbdatafetchers.datafetchers;

import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.EntityFetcher;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.dto.TypedValue;

public class EnityDataFetcher implements EntityFetcher {

  @Override
  public TypedValue getItem(TypedValue source) {
    return source;
  }
}
