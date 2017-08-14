package nl.knaw.huygens.timbuctoo.v5.bdbdatafetchers;

import nl.knaw.huygens.timbuctoo.v5.bdbdatafetchers.datafetchers.CollectionDataFetcher;
import nl.knaw.huygens.timbuctoo.v5.bdbdatafetchers.datafetchers.RelationDataFetcher;
import nl.knaw.huygens.timbuctoo.v5.bdbdatafetchers.datafetchers.TypedLiteralDataFetcher;
import nl.knaw.huygens.timbuctoo.v5.bdbdatafetchers.datafetchers.UnionDataFetcher;
import nl.knaw.huygens.timbuctoo.v5.dataset.CollectionIndex;
import nl.knaw.huygens.timbuctoo.v5.dataset.Direction;
import nl.knaw.huygens.timbuctoo.v5.dataset.QuadStore;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.CollectionFetcher;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.DataFetcherFactory;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.LookupFetcher;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.RelatedDataFetcher;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.dto.DatabaseResult;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.dto.SubjectReference;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.dto.TypedValue;

public class DataStoreDataFetcherFactory implements DataFetcherFactory {
  private final QuadStore tripleStore;
  private final CollectionIndex collectionIndex;

  public DataStoreDataFetcherFactory(QuadStore tripleStore, CollectionIndex collectionIndex) {
    this.tripleStore = tripleStore;
    this.collectionIndex = collectionIndex;
  }

  @Override
  public CollectionFetcher collectionFetcher(String typeUri) {
    return new CollectionDataFetcher(typeUri, collectionIndex, tripleStore);
  }

  @Override
  public LookupFetcher lookupFetcher() {
    return new nl.knaw.huygens.timbuctoo.v5.bdbdatafetchers.datafetchers.LookupFetcher(tripleStore);
  }

  @Override
  public RelatedDataFetcher<SubjectReference> relationFetcher(String predicate, Direction direction) {
    return new RelationDataFetcher(predicate, direction, tripleStore);
  }

  @Override
  public RelatedDataFetcher<TypedValue> typedLiteralFetcher(String predicate) {
    return new TypedLiteralDataFetcher(predicate, tripleStore);
  }

  @Override
  public RelatedDataFetcher<DatabaseResult> unionFetcher(String predicate, Direction direction) {
    return new UnionDataFetcher(predicate, direction, tripleStore);
  }

}
