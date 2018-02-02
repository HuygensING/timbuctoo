package nl.knaw.huygens.timbuctoo.v5.dataset.dto;

import com.google.common.collect.Lists;
import nl.knaw.huygens.timbuctoo.v5.berkeleydb.BdbEnvironmentCreator;
import nl.knaw.huygens.timbuctoo.v5.berkeleydb.BdbStoreProvider;
import nl.knaw.huygens.timbuctoo.v5.dataset.DataSetConfiguration;
import nl.knaw.huygens.timbuctoo.v5.dataset.ImportManager;
import nl.knaw.huygens.timbuctoo.v5.dataset.exceptions.DataStoreCreationException;
import nl.knaw.huygens.timbuctoo.v5.datastores.implementations.RdfDescriptionSaver;
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
import nl.knaw.huygens.timbuctoo.v5.filehelper.FileHelper;
import nl.knaw.huygens.timbuctoo.v5.filestorage.FileStorage;
import nl.knaw.huygens.timbuctoo.v5.rml.RdfDataSourceFactory;
import org.immutables.value.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutorService;

@Value.Immutable
public abstract class DataSet {
  private static final Logger LOG = LoggerFactory.getLogger(DataSet.class);


  public static DataSet dataSet(DataSetMetaData metadata, DataSetConfiguration configuration,
                         FileHelper fileHelper, ExecutorService executorService, String rdfPrefix,
                         BdbEnvironmentCreator dataStoreFactory, ResourceSync resourceSync, Runnable onUpdated)
    throws IOException, DataStoreCreationException, ResourceSyncException {

    String userId = metadata.getOwnerId();
    String dataSetId = metadata.getDataSetId();
    File descriptionFile = resourceSync.getDataSetDescriptionFile(userId, dataSetId);

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

    BdbStoreProvider storeProvider = new BdbStoreProvider(dataStoreFactory);

    try {
      importManager.subscribeToRdf(new RdfDescriptionSaver(descriptionFile, metadata.getBaseUri(),
        importManager.getImportStatus()));
    } catch (ParserConfigurationException | SAXException e) {
      LOG.error("Could not construct import manager of data set", e);
    }

    final BdbTripleStore quadStore = storeProvider.createTripleStore(userId, dataSetId);
    final BdbTypeNameStore typeNameStore = storeProvider.createTypeNameStore(userId, dataSetId, rdfPrefix);
    final BdbSchemaStore schema = storeProvider.createSchemaStore(userId, dataSetId, importManager.getImportStatus());
    final BdbTruePatchStore truePatchStore = storeProvider.createTruePatchStore(userId, dataSetId);
    final UpdatedPerPatchStore updatedPerPatchStore = storeProvider.createUpdatePerPatchStore(userId, dataSetId);
    final BdbRmlDataSourceStore rmlDataSourceStore = storeProvider.createRmlDataSourceStore(
      userId,
      dataSetId,
      importManager.getImportStatus()
    );
    final VersionStore versionStore = storeProvider.createVersionStore(userId, dataSetId);
    final StoreUpdater storeUpdater = new StoreUpdater(
      quadStore,
      typeNameStore,
      truePatchStore,
      updatedPerPatchStore,
      Lists.newArrayList(schema, rmlDataSourceStore),
      versionStore,
      importManager.getImportStatus()
    );
    importManager.subscribeToRdf(storeUpdater);


    ImmutableDataSet dataSet = ImmutableDataSet.builder()
                                               .ownerId(userId)
                                               .dataSetName(dataSetId)
                                               .bdbEnvironmentCreator(dataStoreFactory)
                                               .metadata(metadata)
                                               .quadStore(quadStore)
                                               .typeNameStore(typeNameStore)
                                               .schemaStore(schema)
                                               .dataSource(new RdfDataSourceFactory(rmlDataSourceStore))
                                               .schemaStore(schema)
                                               .importManager(importManager)
                                               .build();
    importManager.init(dataSet);


    if (!quadStore.isClean() || !typeNameStore.isClean() || !schema.isClean() || !truePatchStore.isClean() ||
      !updatedPerPatchStore.isClean() || !rmlDataSourceStore.isClean() || !versionStore.isClean()) {
      LOG.error("Data set '{}__{}' data is corrupted, starting to reimport.", userId, dataSetId);
      quadStore.empty();
      typeNameStore.empty();
      schema.empty();
      truePatchStore.empty();
      updatedPerPatchStore.empty();
      rmlDataSourceStore.empty();
      versionStore.empty();

      importManager.reprocessLogs();

    } else {
      importManager.processLogs(); // process unprocessed logs
    }


    return dataSet;
  }

  public void stop() {
    // close the database environment
    getBdbEnvironmentCreator().closeEnvironment(getOwnerId(), getDataSetName());

  }

  protected abstract String getOwnerId();

  protected abstract String getDataSetName();

  protected abstract BdbEnvironmentCreator getBdbEnvironmentCreator();

  public abstract SchemaStore getSchemaStore();

  public abstract TypeNameStore getTypeNameStore();

  public abstract ImportManager getImportManager();

  public abstract RdfDataSourceFactory getDataSource();

  public abstract QuadStore getQuadStore();

  public abstract DataSetMetaData getMetadata();

}
