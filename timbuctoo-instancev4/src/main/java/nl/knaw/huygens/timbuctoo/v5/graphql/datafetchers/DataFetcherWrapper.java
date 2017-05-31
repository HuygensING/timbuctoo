package nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.dto.TypedValue;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.dto.PaginationArguments;

public class DataFetcherWrapper implements DataFetcher {
  private final boolean isList;
  private final RelatedDataFetcher inner;

  public DataFetcherWrapper(boolean isList, RelatedDataFetcher inner) {
    this.isList = isList;
    this.inner = inner;
  }


  @Override
  public Object get(DataFetchingEnvironment environment) {
    if (environment.getSource() instanceof TypedValue) {
      TypedValue source = environment.getSource();
      if (isList) {
        return inner.getList(source, getPaginationArguments(environment));
      } else {
        return inner.getItem(source);
      }
    } else {
      throw new IllegalStateException("Source is not a BoundSubject");
    }
  }

  private PaginationArguments getPaginationArguments(DataFetchingEnvironment environment) {
    return null;
  }
}
