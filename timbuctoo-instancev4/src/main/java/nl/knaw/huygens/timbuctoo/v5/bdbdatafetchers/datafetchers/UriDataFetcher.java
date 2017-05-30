package nl.knaw.huygens.timbuctoo.v5.bdbdatafetchers.datafetchers;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import nl.knaw.huygens.timbuctoo.v5.bdbdatafetchers.dto.BoundSubject;

public class UriDataFetcher implements DataFetcher {
  @Override
  public Object get(DataFetchingEnvironment environment) {
    if (environment.getSource() instanceof BoundSubject) {
      return ((BoundSubject) environment.getSource()).getValue();
    } else {
      return null;
    }
  }
}
