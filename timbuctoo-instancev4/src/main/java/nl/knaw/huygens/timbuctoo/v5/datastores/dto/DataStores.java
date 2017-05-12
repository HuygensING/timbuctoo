package nl.knaw.huygens.timbuctoo.v5.datastores.dto;

import com.sleepycat.je.Environment;
import nl.knaw.huygens.timbuctoo.rml.DataSource;
import nl.knaw.huygens.timbuctoo.v5.datastores.collectionindex.CollectionIndex;
import nl.knaw.huygens.timbuctoo.v5.datastores.prefixstore.TypeNameStore;
import nl.knaw.huygens.timbuctoo.v5.datastores.schema.SchemaStore;
import nl.knaw.huygens.timbuctoo.v5.datastores.triples.TripleStore;
import nl.knaw.huygens.timbuctoo.v5.graphql.collectionindex.CollectionIndexFetcherFactory;
import nl.knaw.huygens.timbuctoo.v5.graphql.entity.DataFetcherFactory;
import nl.knaw.huygens.timbuctoo.v5.logprocessing.datastore.LogStorage;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

import static java.lang.Math.min;

public class DataStores implements AutoCloseable {
  private final Environment commonEnvironment;
  private final CollectionIndex collectionIndex;
  private final TypeNameStore typeNameStore;
  private final TripleStore tripleStore;
  private final BiFunction<String, Map<String, String>, DataSource> tripleStoreDataSourceFactory;
  private final SchemaStore schemaStore;
  private final CollectionIndexFetcherFactory collectionIndexFetcherFactory;
  private final DataFetcherFactory dataFetcherFactory;
  private final LogStorage logStorage;

  public DataStores(Environment commonEnvironment, CollectionIndex collectionIndex, TypeNameStore typeNameStore,
                    TripleStore tripleStore, BiFunction<String, Map<String, String>, DataSource> tripleStoreDataSourceFactory,
                    SchemaStore schemaStore, CollectionIndexFetcherFactory collectionIndexFetcherFactory,
                    DataFetcherFactory dataFetcherFactory, LogStorage logStorage) {
    this.commonEnvironment = commonEnvironment;
    this.collectionIndex = collectionIndex;
    this.typeNameStore = typeNameStore;
    this.tripleStore = tripleStore;
    this.tripleStoreDataSourceFactory = tripleStoreDataSourceFactory;
    this.schemaStore = schemaStore;
    this.collectionIndexFetcherFactory = collectionIndexFetcherFactory;
    this.dataFetcherFactory = dataFetcherFactory;
    this.logStorage = logStorage;
  }


  public CollectionIndex getCollectionIndex() {
    return collectionIndex;
  }

  public TypeNameStore getTypeNameStore() {
    return typeNameStore;
  }

  public SchemaStore getSchemaStore() {
    return schemaStore;
  }

  public TripleStore getTripleStore() {
    return tripleStore;
  }

  public LogStorage getLogStorage() {
    return logStorage;
  }

  public BiFunction<String, Map<String, String>, DataSource> getDataSourceFactory() {
    return tripleStoreDataSourceFactory;
  }

  public CollectionIndexFetcherFactory getCollectionIndexFetcherFactory() {
    return collectionIndexFetcherFactory;
  }

  public DataFetcherFactory getDataFetcherFactory() {
    return dataFetcherFactory;
  }

  public long getCurrentVersion() {
    return min(
      collectionIndex.getStatus().getCurrentVersion(),
      min(
        typeNameStore.getStatus().getCurrentVersion(),
        min(
          schemaStore.getStatus().getCurrentVersion(),
          tripleStore.getStatus().getCurrentVersion()
        )
      )
    );
  }

  @Override
  public void close() throws Exception {
    typeNameStore.close();
    collectionIndex.close();
    tripleStore.close();
    schemaStore.close();
    commonEnvironment.close();
  }

  public Map<String, StoreStatus> getStatus() {
    Map<String, StoreStatus> result = new HashMap<>();
    result.put("collectionIndex", collectionIndex.getStatus());
    result.put("schemaStore", schemaStore.getStatus());
    result.put("typeNameStore", typeNameStore.getStatus());
    result.put("tripleStore", tripleStore.getStatus());
    return result;
  }
}
