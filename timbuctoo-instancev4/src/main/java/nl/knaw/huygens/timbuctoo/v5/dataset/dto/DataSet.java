package nl.knaw.huygens.timbuctoo.v5.dataset.dto;

import nl.knaw.huygens.timbuctoo.v5.berkeleydb.BdbDatabaseCreator;
import nl.knaw.huygens.timbuctoo.v5.dataset.DataSetConfiguration;
import nl.knaw.huygens.timbuctoo.v5.dataset.ImportManager;
import nl.knaw.huygens.timbuctoo.v5.dataset.exceptions.DataStoreCreationException;
import nl.knaw.huygens.timbuctoo.v5.datastores.implementations.bdb.BdbBackedData;
import nl.knaw.huygens.timbuctoo.v5.datastores.implementations.bdb.BdbRmlDataSourceStore;
import nl.knaw.huygens.timbuctoo.v5.datastores.implementations.bdb.BdbSchemaStore;
import nl.knaw.huygens.timbuctoo.v5.datastores.implementations.bdb.BdbTripleStore;
import nl.knaw.huygens.timbuctoo.v5.datastores.implementations.bdb.BdbTruePatchStore;
import nl.knaw.huygens.timbuctoo.v5.datastores.implementations.bdb.BdbTypeNameStore;
import nl.knaw.huygens.timbuctoo.v5.datastores.implementations.bdb.StoreUpdater;
import nl.knaw.huygens.timbuctoo.v5.datastores.implementations.bdb.UpdatedPerPatchStore;
import nl.knaw.huygens.timbuctoo.v5.datastores.implementations.bdb.VersionStore;
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
                         BdbDatabaseCreator dataStoreFactory, ResourceSync resourceSync, Runnable onUpdated)
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
    BdbTripleStore quadStore = new BdbTripleStore(dataStoreFactory, userId, dataSetId);
    final BdbTypeNameStore typeNameStore = new BdbTypeNameStore(
      new BdbBackedData(dataStoreFactory, userId, dataSetId, "typenames")
    );
    final BdbSchemaStore schema = new BdbSchemaStore(
      new BdbBackedData(dataStoreFactory, userId, dataSetId, "schema")
    );
    final BdbTruePatchStore truePatchStore = new BdbTruePatchStore(
      dataStoreFactory,
      userId,
      dataSetId
    );

    final UpdatedPerPatchStore updatedPerPatchStore = new UpdatedPerPatchStore(
      dataStoreFactory,
      userId,
      dataSetId
    );
    final BdbRmlDataSourceStore rmlDataSourceStore = new BdbRmlDataSourceStore(
      userId,
      dataSetId,
      dataStoreFactory,
      importManager
    );
    VersionStore versionStore = new VersionStore(dataStoreFactory, userId, dataSetId);
    final StoreUpdater storeUpdater = new StoreUpdater(
      dataStoreFactory,
      quadStore,
      typeNameStore,
      truePatchStore,
      updatedPerPatchStore,
      schema,
      rmlDataSourceStore,
      versionStore
    );
    importManager.subscribeToRdf(storeUpdater);
    return ImmutableDataSet.builder()
      .metadata(metadata)
      .quadStore(quadStore)
      .typeNameStore(typeNameStore)
      .schemaStore(schema)
      .dataSource(new RdfDataSourceFactory(rmlDataSourceStore))
      .importManager(importManager)
      .build();
  }

  SchemaStore getSchemaStore();

  TypeNameStore getTypeNameStore();

  ImportManager getImportManager();

  RdfDataSourceFactory getDataSource();

  QuadStore getQuadStore();

  PromotedDataSet getMetadata();
}
