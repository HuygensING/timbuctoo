package nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import org.eclipse.rdf4j.common.net.ParsedURI;

/**
 * Creates a SubjectReference from the passed argument instead of reading it from the database
 */
public class LookupFetcherWrapper implements DataFetcher {
  private final String uriArgument;
  private final LookupFetcher lookupFetcher;
  private final ParsedURI baseUri;

  public LookupFetcherWrapper(String uriArgument, LookupFetcher lookupFetcher, String baseUri) {
    this.uriArgument = uriArgument;
    this.lookupFetcher = lookupFetcher;
    this.baseUri = new ParsedURI(baseUri);
  }

  @Override
  public Object get(DataFetchingEnvironment environment) {
    String uri = environment.getArgument(uriArgument);
    return lookupFetcher.getItem(baseUri.resolve(uri).toString());
  }
}
