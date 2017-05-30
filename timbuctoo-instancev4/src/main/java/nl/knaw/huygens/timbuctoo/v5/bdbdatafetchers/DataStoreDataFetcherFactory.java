package nl.knaw.huygens.timbuctoo.v5.bdbdatafetchers;

import graphql.language.InlineFragment;
import graphql.language.Selection;
import graphql.schema.DataFetcher;
import graphql.schema.GraphQLObjectType;
import graphql.schema.TypeResolver;
import nl.knaw.huygens.timbuctoo.v5.bdbdatafetchers.datafetchers.CollectionDataFetcher;
import nl.knaw.huygens.timbuctoo.v5.bdbdatafetchers.datafetchers.RelationDataFetcher;
import nl.knaw.huygens.timbuctoo.v5.bdbdatafetchers.datafetchers.TypedLiteralDataFetcher;
import nl.knaw.huygens.timbuctoo.v5.bdbdatafetchers.datafetchers.UnionDataFetcher;
import nl.knaw.huygens.timbuctoo.v5.bdbdatafetchers.datafetchers.UriDataFetcher;
import nl.knaw.huygens.timbuctoo.v5.bdbdatafetchers.dto.BoundSubject;
import nl.knaw.huygens.timbuctoo.v5.bdbdatafetchers.stores.BdbCollectionIndex;
import nl.knaw.huygens.timbuctoo.v5.bdbdatafetchers.stores.BdbTripleStore;
import nl.knaw.huygens.timbuctoo.v5.dataset.DataSet;
import nl.knaw.huygens.timbuctoo.v5.datastores.exceptions.DataStoreCreationException;
import nl.knaw.huygens.timbuctoo.v5.dropwizard.BdbDatabaseFactory;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.DataFetcherFactory;
import org.slf4j.Logger;

import java.util.Map;
import java.util.Set;

import static org.slf4j.LoggerFactory.getLogger;

public class DataStoreDataFetcherFactory implements DataFetcherFactory {
  private final BdbTripleStore tripleStore;
  private final BdbCollectionIndex collectionIndex;
  private static final Logger LOG = getLogger(DataStoreDataFetcherFactory.class);

  public DataStoreDataFetcherFactory(String userId, String dataSetId, DataSet dataSet, BdbDatabaseFactory dbFactory)
    throws DataStoreCreationException {
    this.tripleStore = new BdbTripleStore(dataSet, dbFactory, userId, dataSetId);
    this.collectionIndex = new BdbCollectionIndex(dataSet, dbFactory, userId, dataSetId);
  }

  @Override
  public DataFetcher collectionFetcher(String typeUri) {
    return new CollectionDataFetcher(typeUri, collectionIndex);
  }

  @Override
  public TypeResolver objectResolver(Map<String, String> typeMappings, Map<String, GraphQLObjectType> typesMap) {
    return environment -> {
      //Often a thing has one type. In that case this lambda is easy to implement. Simply return that type
      //In rdf things can have more then one type though (types are like java interfaces)
      //Since this lambda only allows us to return 1 type we need to do a bit more work and return one of the types that
      //the user actually requested
      Set<String> typeUris = ((BoundSubject) environment.getObject()).getType();
      for (Selection selection : environment.getField().getSelectionSet().getSelections()) {
        if (selection instanceof InlineFragment) {
          InlineFragment fragment = (InlineFragment) selection;
          String typeUri = typeMappings.get(fragment.getTypeCondition().getName());
          if (typeUris.contains(typeUri)) {
            return typesMap.get(typeUri);
          }
        } else {
          LOG.error("I have a union type whose selection is not an InlineFragment!");
        }
      }
      return typeUris.isEmpty() ? null : typesMap.get(typeUris.iterator().next());
    };
  }

  @Override
  public TypeResolver valueResolver(Map<String, GraphQLObjectType> typesMap) {
    return environment -> typesMap.get(((BoundSubject) environment.getObject()).getType().iterator().next());
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
