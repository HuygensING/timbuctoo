package nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers;


public interface DataFetcherFactory {
  EntityFetcher entityFetcher();

  RelatedDataFetcher relationFetcher(String predicate);

  RelatedDataFetcher typedLiteralFetcher(String predicate);

  RelatedDataFetcher unionFetcher(String predicate);

  UriFetcher entityUriDataFetcher();

  CollectionFetcher collectionFetcher(String typeUri);
}
