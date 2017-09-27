package nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.berkeleydb;

import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.berkeleydb.datafetchers.CollectionDataFetcher;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.berkeleydb.datafetchers.QuadStoreLookUpSubjectByUriFetcher;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.berkeleydb.datafetchers.RelationDataFetcher;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.berkeleydb.datafetchers.TypedLiteralDataFetcher;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.berkeleydb.datafetchers.UnionDataFetcher;
import nl.knaw.huygens.timbuctoo.v5.datastores.collectionindex.CollectionIndex;
import nl.knaw.huygens.timbuctoo.v5.datastores.quadstore.dto.Direction;
import nl.knaw.huygens.timbuctoo.v5.datastores.quadstore.QuadStore;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.CollectionFetcher;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.DataFetcherFactory;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.LookUpSubjectByUriFetcher;
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
  public LookUpSubjectByUriFetcher lookupFetcher() {
    return new QuadStoreLookUpSubjectByUriFetcher(tripleStore);
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
