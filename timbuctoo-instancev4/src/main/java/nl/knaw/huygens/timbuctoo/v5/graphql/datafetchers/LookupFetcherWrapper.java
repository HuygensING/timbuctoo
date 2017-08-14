package nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;

/**
 * Creates a SubjectReference from the passed argument instead of reading it from the database
 */
public class LookupFetcherWrapper implements DataFetcher {
  private final String uriArgument;
  private final LookupFetcher lookupFetcher;

  public LookupFetcherWrapper(String uriArgument, LookupFetcher lookupFetcher) {
    this.uriArgument = uriArgument;
    this.lookupFetcher = lookupFetcher;
  }

  @Override
  public Object get(DataFetchingEnvironment environment) {
    return lookupFetcher.getItem(environment.getArgument(uriArgument));
  }
}
