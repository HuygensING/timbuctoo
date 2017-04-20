package nl.knaw.huygens.timbuctoo.v5.datastores.dto;

import nl.knaw.huygens.timbuctoo.v5.datastores.collectionindex.CollectionIndex;
import nl.knaw.huygens.timbuctoo.v5.datastores.prefixstore.TypeNameStore;
import nl.knaw.huygens.timbuctoo.v5.datastores.schema.SchemaStore;
import nl.knaw.huygens.timbuctoo.v5.datastores.triples.TripleStore;
import nl.knaw.huygens.timbuctoo.v5.graphql.collectionindex.CollectionIndexFetcherFactory;
import nl.knaw.huygens.timbuctoo.v5.graphql.entity.DataFetcherFactory;

public class DataStores implements AutoCloseable {
  private final CollectionIndex collectionIndex;
  private final TypeNameStore typeNameStore;
  private final TripleStore tripleStore;
  private final SchemaStore schemaStore;
  private final CollectionIndexFetcherFactory collectionIndexFetcherFactory;
  private final DataFetcherFactory dataFetcherFactory;

  public DataStores(CollectionIndex collectionIndex, TypeNameStore typeNameStore, TripleStore tripleStore,
                    SchemaStore schemaStore, CollectionIndexFetcherFactory collectionIndexFetcherFactory,
                    DataFetcherFactory dataFetcherFactory) {
    this.collectionIndex = collectionIndex;
    this.typeNameStore = typeNameStore;
    this.tripleStore = tripleStore;
    this.schemaStore = schemaStore;
    this.collectionIndexFetcherFactory = collectionIndexFetcherFactory;
    this.dataFetcherFactory = dataFetcherFactory;
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

  public CollectionIndexFetcherFactory getCollectionIndexFetcherFactory() {
    return collectionIndexFetcherFactory;
  }

  public DataFetcherFactory getDataFetcherFactory() {
    return dataFetcherFactory;
  }

  @Override
  public void close() throws Exception {
    collectionIndex.close();
    typeNameStore.close();
    tripleStore.close();
    schemaStore.close();
  }
}
