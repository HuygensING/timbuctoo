package nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers;


import nl.knaw.huygens.timbuctoo.v5.dataset.Direction;

public interface DataFetcherFactory {
  EntityFetcher entityFetcher();

  RelatedDataFetcher relationFetcher(String predicate, Direction direction);

  RelatedDataFetcher typedLiteralFetcher(String predicate);

  RelatedDataFetcher unionFetcher(String predicate, Direction direction);

  UriFetcher entityUriDataFetcher();

  CollectionFetcher collectionFetcher(String typeUri);
}
