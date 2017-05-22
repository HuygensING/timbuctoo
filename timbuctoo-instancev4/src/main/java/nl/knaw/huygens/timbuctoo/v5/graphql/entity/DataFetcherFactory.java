package nl.knaw.huygens.timbuctoo.v5.graphql.entity;

import graphql.schema.DataFetcher;

public interface DataFetcherFactory {
  DataFetcher relationFetcher(String predicate, boolean isList);

  DataFetcher typedLiteralFetcher(String predicate, boolean isList);

  DataFetcher unionFetcher(String predicate, boolean isList);

  DataFetcher entityUriDataFetcher();
}
