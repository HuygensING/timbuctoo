package nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.dto.DatabaseResult;
import org.eclipse.rdf4j.common.net.ParsedURI;

/**
 * Creates a SubjectReference from the passed argument instead of reading it from the database
 */
public class LookUpSubjectByUriFetcherWrapper implements DataFetcher {
  private final String uriArgument;
  private final LookUpSubjectByUriFetcher lookUpSubjectByUriFetcher;
  private final ParsedURI baseUri;

  public LookUpSubjectByUriFetcherWrapper(String uriArgument, LookUpSubjectByUriFetcher lookUpSubjectByUriFetcher,
                                          String baseUri) {
    this.uriArgument = uriArgument;
    this.lookUpSubjectByUriFetcher = lookUpSubjectByUriFetcher;
    this.baseUri = new ParsedURI(baseUri);
  }

  @Override
  public Object get(DataFetchingEnvironment environment) {
    String uri = environment.getArgument(uriArgument);
    return lookUpSubjectByUriFetcher.getItem(
      baseUri.resolve(uri).toString(),
      ((DatabaseResult) environment.getSource()).getDataSet()
    );
  }
}
