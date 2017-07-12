package nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.dto.TypedValue;

public class LookupFetcher implements DataFetcher {
  private final EntityFetcher fetcher;
  private final String uriArgument;

  public LookupFetcher(EntityFetcher fetcher, String uriArgument) {
    this.fetcher = fetcher;
    this.uriArgument = uriArgument;
  }

  @Override
  public Object get(DataFetchingEnvironment environment) {
    return fetcher.getItem(TypedValue.create(environment.getArgument(uriArgument)));
  }
}
