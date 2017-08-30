package nl.knaw.huygens.timbuctoo.v5.dataset.dto;

import nl.knaw.huygens.timbuctoo.v5.bdbdatafetchers.DataStoreDataFetcherFactory;
import nl.knaw.huygens.timbuctoo.v5.dataset.CollectionIndex;
import nl.knaw.huygens.timbuctoo.v5.dataset.DataSetConfiguration;
import nl.knaw.huygens.timbuctoo.v5.dataset.DataStoreFactory;
import nl.knaw.huygens.timbuctoo.v5.dataset.ImportManager;
import nl.knaw.huygens.timbuctoo.v5.dataset.QuadStore;
import nl.knaw.huygens.timbuctoo.v5.datastores.exceptions.DataStoreCreationException;
import nl.knaw.huygens.timbuctoo.v5.datastores.implementations.json.JsonSchemaStore;
import nl.knaw.huygens.timbuctoo.v5.datastores.implementations.json.JsonTypeNameStore;
import nl.knaw.huygens.timbuctoo.v5.datastores.prefixstore.TypeNameStore;
import nl.knaw.huygens.timbuctoo.v5.datastores.resourcesync.ResourceSync;
import nl.knaw.huygens.timbuctoo.v5.datastores.resourcesync.ResourceSyncException;
import nl.knaw.huygens.timbuctoo.v5.datastores.schema.SchemaStore;
import nl.knaw.huygens.timbuctoo.v5.filestorage.implementations.filesystem.FileHelper;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.DataFetcherFactory;
import nl.knaw.huygens.timbuctoo.v5.rml.RdfDataSourceFactory;
import org.immutables.value.Value;

import java.io.IOException;
import java.util.concurrent.ExecutorService;

@Value.Immutable
public interface DataSet {

  static DataSet dataSet(String userId, String dataSetId, DataSetConfiguration configuration,
                         FileHelper fileHelper, ExecutorService executorService,
                         DataStoreFactory dataStoreFactory, ResourceSync resourceSync)
    throws IOException, DataStoreCreationException, ResourceSyncException {

    ImportManager importManager = new ImportManager(
      fileHelper.fileInDataSet(userId, dataSetId, "log.json"),
      configuration.getFileStorage().makeFileStorage(userId, dataSetId),
      configuration.getFileStorage().makeFileStorage(userId, dataSetId),
      configuration.getFileStorage().makeLogStorage(userId, dataSetId),
      executorService,
      configuration.getRdfIo(),
      resourceSync.resourceList(userId, dataSetId)
    );
    QuadStore quadStore = dataStoreFactory.createQuadStore(importManager, userId, dataSetId);
    CollectionIndex collectionIndex = dataStoreFactory.createCollectionIndex(importManager, userId, dataSetId);
    return ImmutableDataSet.builder()
      .quadStore(quadStore)
      .collectionIndex(collectionIndex)
      .typeNameStore(new JsonTypeNameStore(
        fileHelper.fileInDataSet(userId, dataSetId, "prefixes.json"),
        importManager
      ))
      .schemaStore(new JsonSchemaStore(
        importManager,
        fileHelper.fileInDataSet(userId, dataSetId, "schema.json")
      ))
      .dataSource(new RdfDataSourceFactory(
        dataStoreFactory.createDataSourceStore(importManager, userId, dataSetId)
      ))
      .dataFetcherFactory(new DataStoreDataFetcherFactory(quadStore, collectionIndex))
      .importManager(importManager)
      .build();
  }

  SchemaStore getSchemaStore();

  TypeNameStore getTypeNameStore();

  ImportManager getImportManager();

  RdfDataSourceFactory getDataSource();

  QuadStore getQuadStore();

  CollectionIndex getCollectionIndex();

  DataFetcherFactory getDataFetcherFactory();

}
