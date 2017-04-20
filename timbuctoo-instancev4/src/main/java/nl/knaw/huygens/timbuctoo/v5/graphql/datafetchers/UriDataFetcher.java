package nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.dto.BoundSubject;

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
