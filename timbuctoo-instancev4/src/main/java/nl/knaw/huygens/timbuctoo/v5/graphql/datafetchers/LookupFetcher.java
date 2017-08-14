package nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.dto.SubjectReference;

/**
 * Creates a SubjectReference from the passed argument instead of reading it from the database
 */
public class LookupFetcher implements DataFetcher {
  private final String uriArgument;

  public LookupFetcher(String uriArgument) {
    this.uriArgument = uriArgument;
  }

  @Override
  public Object get(DataFetchingEnvironment environment) {
    return SubjectReference.create(environment.getArgument(uriArgument));
  }
}
