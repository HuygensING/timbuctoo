package nl.knaw.huygens.timbuctoo.v5.bdbdatafetchers;

import nl.knaw.huygens.timbuctoo.v5.bdbdatafetchers.datafetchers.CollectionDataFetcher;
import nl.knaw.huygens.timbuctoo.v5.bdbdatafetchers.datafetchers.EnityDataFetcher;
import nl.knaw.huygens.timbuctoo.v5.bdbdatafetchers.datafetchers.RelationDataFetcher;
import nl.knaw.huygens.timbuctoo.v5.bdbdatafetchers.datafetchers.TypedLiteralDataFetcher;
import nl.knaw.huygens.timbuctoo.v5.bdbdatafetchers.datafetchers.UnionDataFetcher;
import nl.knaw.huygens.timbuctoo.v5.bdbdatafetchers.datafetchers.UriDataFetcher;
import nl.knaw.huygens.timbuctoo.v5.dataset.Direction;
import nl.knaw.huygens.timbuctoo.v5.dataset.QuadStore;
import nl.knaw.huygens.timbuctoo.v5.dataset.CollectionIndex;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.CollectionFetcher;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.DataFetcherFactory;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.EntityFetcher;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.RelatedDataFetcher;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.UriFetcher;

public class DataStoreDataFetcherFactory implements DataFetcherFactory {
  private final QuadStore tripleStore;
  private final CollectionIndex collectionIndex;

  public DataStoreDataFetcherFactory(QuadStore tripleStore, CollectionIndex collectionIndex) {
    this.tripleStore = tripleStore;
    this.collectionIndex = collectionIndex;
  }

  @Override
  public CollectionFetcher collectionFetcher(String typeUri) {
    return new CollectionDataFetcher(typeUri, collectionIndex);
  }

  @Override
  public EntityFetcher entityFetcher() {
    return new EnityDataFetcher();
  }

  @Override
  public RelatedDataFetcher relationFetcher(String predicate, Direction direction) {
    return new RelationDataFetcher(predicate, direction, tripleStore);
  }

  @Override
  public RelatedDataFetcher typedLiteralFetcher(String predicate) {
    return new TypedLiteralDataFetcher(predicate, tripleStore);
  }

  @Override
  public RelatedDataFetcher unionFetcher(String predicate, Direction direction) {
    return new UnionDataFetcher(predicate, direction, tripleStore);
  }

  @Override
  public UriFetcher entityUriDataFetcher() {
    return new UriDataFetcher();
  }
}
