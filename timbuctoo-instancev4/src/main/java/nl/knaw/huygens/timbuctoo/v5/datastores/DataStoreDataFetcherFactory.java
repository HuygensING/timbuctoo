package nl.knaw.huygens.timbuctoo.v5.datastores;

import graphql.schema.DataFetcher;
import nl.knaw.huygens.timbuctoo.v5.datastores.collectionindex.CollectionIndex;
import nl.knaw.huygens.timbuctoo.v5.datastores.triples.TripleStore;
import nl.knaw.huygens.timbuctoo.v5.graphql.collectionindex.CollectionIndexFetcherFactory;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.CollectionDataFetcher;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.RelationDataFetcher;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.TypedLiteralDataFetcher;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.UnionDataFetcher;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.UriDataFetcher;
import nl.knaw.huygens.timbuctoo.v5.graphql.entity.DataFetcherFactory;

public class DataStoreDataFetcherFactory implements DataFetcherFactory, CollectionIndexFetcherFactory {
  private final TripleStore tripleStore;
  private final CollectionIndex collectionIndex;

  public DataStoreDataFetcherFactory(TripleStore tripleStore, CollectionIndex collectionIndex) {
    this.tripleStore = tripleStore;
    this.collectionIndex = collectionIndex;
  }

  @Override
  public DataFetcher collectionFetcher(String typeUri) {
    return new CollectionDataFetcher(typeUri, collectionIndex);
  }

  @Override
  public DataFetcher relationFetcher(String predicate, boolean isList) {
    return new RelationDataFetcher(predicate, isList, tripleStore);
  }

  @Override
  public DataFetcher typedLiteralFetcher(String predicate, boolean isList) {
    return new TypedLiteralDataFetcher(predicate, isList, tripleStore);
  }

  @Override
  public DataFetcher unionFetcher(String predicate, boolean isList) {
    return new UnionDataFetcher(predicate, isList, tripleStore);
  }

  @Override
  public DataFetcher entityUriDataFetcher() {
    return new UriDataFetcher();
  }
}
