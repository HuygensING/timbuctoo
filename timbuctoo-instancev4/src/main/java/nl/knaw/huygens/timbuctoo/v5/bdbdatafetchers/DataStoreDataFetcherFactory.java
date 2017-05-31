package nl.knaw.huygens.timbuctoo.v5.bdbdatafetchers;

import nl.knaw.huygens.timbuctoo.v5.bdbdatafetchers.datafetchers.CollectionDataFetcher;
import nl.knaw.huygens.timbuctoo.v5.bdbdatafetchers.datafetchers.RelationDataFetcher;
import nl.knaw.huygens.timbuctoo.v5.bdbdatafetchers.datafetchers.TypedLiteralDataFetcher;
import nl.knaw.huygens.timbuctoo.v5.bdbdatafetchers.datafetchers.UnionDataFetcher;
import nl.knaw.huygens.timbuctoo.v5.bdbdatafetchers.datafetchers.UriDataFetcher;
import nl.knaw.huygens.timbuctoo.v5.bdbdatafetchers.stores.BdbCollectionIndex;
import nl.knaw.huygens.timbuctoo.v5.bdbdatafetchers.stores.BdbTripleStore;
import nl.knaw.huygens.timbuctoo.v5.dataset.DataProvider;
import nl.knaw.huygens.timbuctoo.v5.datastores.exceptions.DataStoreCreationException;
import nl.knaw.huygens.timbuctoo.v5.dropwizard.BdbDatabaseFactory;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.CollectionFetcher;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.DataFetcherFactory;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.RelatedDataFetcher;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.UriFetcher;

public class DataStoreDataFetcherFactory implements DataFetcherFactory {
  private final BdbTripleStore tripleStore;
  private final BdbCollectionIndex collectionIndex;

  public DataStoreDataFetcherFactory(String userId, String dataSetId, DataProvider dataProvider, BdbDatabaseFactory dbFactory)
    throws DataStoreCreationException {
    this.tripleStore = new BdbTripleStore(dataProvider, dbFactory, userId, dataSetId);
    this.collectionIndex = new BdbCollectionIndex(dataProvider, dbFactory, userId, dataSetId);
  }

  @Override
  public CollectionFetcher collectionFetcher(String typeUri) {
    return new CollectionDataFetcher(typeUri, collectionIndex);
  }

  @Override
  public RelatedDataFetcher relationFetcher(String predicate) {
    return new RelationDataFetcher(predicate, tripleStore);
  }

  @Override
  public RelatedDataFetcher typedLiteralFetcher(String predicate) {
    return new TypedLiteralDataFetcher(predicate, tripleStore);
  }

  @Override
  public RelatedDataFetcher unionFetcher(String predicate) {
    return new UnionDataFetcher(predicate, tripleStore);
  }

  @Override
  public UriFetcher entityUriDataFetcher() {
    return new UriDataFetcher();
  }
}
