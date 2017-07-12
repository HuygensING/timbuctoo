package nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.dto.TypedValue;

public class UriFetcherWrapper implements DataFetcher {
  private final UriFetcher uriFetcher;

  public UriFetcherWrapper(UriFetcher uriFetcher) {
    this.uriFetcher = uriFetcher;
  }

  @Override
  public Object get(DataFetchingEnvironment environment) {
    if (environment.getSource() instanceof TypedValue) {
      return uriFetcher.getUri(environment.getSource());
    } else {
      throw new IllegalStateException("Source is not a BoundSubject");
    }
  }
}
