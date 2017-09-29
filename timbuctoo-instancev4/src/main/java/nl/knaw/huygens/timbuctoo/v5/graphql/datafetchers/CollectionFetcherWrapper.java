package nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.dto.DatabaseResult;

import static nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.PaginationArgumentsHelper.getPaginationArguments;

public class CollectionFetcherWrapper implements DataFetcher {
  private final CollectionFetcher fetcher;

  public CollectionFetcherWrapper(CollectionFetcher fetcher) {
    this.fetcher = fetcher;
  }

  @Override
  public Object get(DataFetchingEnvironment environment) {
    return fetcher.getList(
      getPaginationArguments(environment),
      ((DatabaseResult) environment.getSource()).getDataSet()
    );
  }

}
