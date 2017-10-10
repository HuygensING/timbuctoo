package nl.knaw.huygens.timbuctoo.v5.dataset.dto;

import nl.knaw.huygens.timbuctoo.v5.dataset.DataSetConfiguration;
import nl.knaw.huygens.timbuctoo.v5.dataset.DataStoreFactory;
import nl.knaw.huygens.timbuctoo.v5.dataset.ImportManager;
import nl.knaw.huygens.timbuctoo.v5.dataset.exceptions.DataStoreCreationException;
import nl.knaw.huygens.timbuctoo.v5.datastores.collectionindex.CollectionIndex;
import nl.knaw.huygens.timbuctoo.v5.datastores.implementations.json.JsonSchemaStore;
import nl.knaw.huygens.timbuctoo.v5.datastores.implementations.json.JsonTypeNameStore;
import nl.knaw.huygens.timbuctoo.v5.datastores.prefixstore.TypeNameStore;
import nl.knaw.huygens.timbuctoo.v5.datastores.quadstore.QuadStore;
import nl.knaw.huygens.timbuctoo.v5.datastores.resourcesync.ResourceSync;
import nl.knaw.huygens.timbuctoo.v5.datastores.resourcesync.ResourceSyncException;
import nl.knaw.huygens.timbuctoo.v5.datastores.schemastore.SchemaStore;
import nl.knaw.huygens.timbuctoo.v5.filestorage.implementations.filesystem.FileHelper;
import nl.knaw.huygens.timbuctoo.v5.rml.RdfDataSourceFactory;
import org.immutables.value.Value;

import java.io.IOException;
import java.util.concurrent.ExecutorService;

@Value.Immutable
public interface DataSet {

  static DataSet dataSet(PromotedDataSet metadata, DataSetConfiguration configuration,
                         FileHelper fileHelper, ExecutorService executorService,
                         DataStoreFactory dataStoreFactory, ResourceSync resourceSync, Runnable onUpdated)
    throws IOException, DataStoreCreationException, ResourceSyncException {

    String userId = metadata.getOwnerId();
    String dataSetId = metadata.getDataSetId();
    ImportManager importManager = new ImportManager(
      fileHelper.fileInDataSet(userId, dataSetId, "log.json"),
      configuration.getFileStorage().makeFileStorage(userId, dataSetId),
      configuration.getFileStorage().makeFileStorage(userId, dataSetId),
      configuration.getFileStorage().makeLogStorage(userId, dataSetId),
      executorService,
      configuration.getRdfIo(),
      resourceSync.resourceList(userId, dataSetId),
      onUpdated
    );
    QuadStore quadStore = dataStoreFactory.createQuadStore(importManager, userId, dataSetId);
    CollectionIndex collectionIndex = dataStoreFactory.createCollectionIndex(importManager, userId, dataSetId);
    return ImmutableDataSet.builder()
      .metadata(metadata)
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
      .importManager(importManager)
      .build();
  }

  SchemaStore getSchemaStore();

  TypeNameStore getTypeNameStore();

  ImportManager getImportManager();

  RdfDataSourceFactory getDataSource();

  QuadStore getQuadStore();

  CollectionIndex getCollectionIndex();

  PromotedDataSet getMetadata();
}
