package nl.knaw.huygens.timbuctoo.v5.dataset.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.sleepycat.bind.tuple.TupleBinding;
import nl.knaw.huygens.timbuctoo.v5.berkeleydb.BdbEnvironmentCreator;
import nl.knaw.huygens.timbuctoo.v5.berkeleydb.BdbWrapper;
import nl.knaw.huygens.timbuctoo.v5.berkeleydb.exceptions.BdbDbCreationException;
import nl.knaw.huygens.timbuctoo.v5.berkeleydb.isclean.StringIntegerIsCleanHandler;
import nl.knaw.huygens.timbuctoo.v5.berkeleydb.isclean.StringStringIsCleanHandler;
import nl.knaw.huygens.timbuctoo.v5.dataset.ChangesRetriever;
import nl.knaw.huygens.timbuctoo.v5.dataset.CurrentStateRetriever;
import nl.knaw.huygens.timbuctoo.v5.dataset.ImportManager;
import nl.knaw.huygens.timbuctoo.v5.dataset.ReadOnlyChecker;
import nl.knaw.huygens.timbuctoo.v5.dataset.exceptions.DataStoreCreationException;
import nl.knaw.huygens.timbuctoo.v5.datastorage.DataSetStorage;
import nl.knaw.huygens.timbuctoo.v5.datastores.implementations.RdfDescriptionSaver;
import nl.knaw.huygens.timbuctoo.v5.datastores.implementations.bdb.BdbBackedData;
import nl.knaw.huygens.timbuctoo.v5.datastores.implementations.bdb.BdbRmlDataSourceStore;
import nl.knaw.huygens.timbuctoo.v5.datastores.implementations.bdb.BdbSchemaStore;
import nl.knaw.huygens.timbuctoo.v5.datastores.implementations.bdb.BdbQuadStore;
import nl.knaw.huygens.timbuctoo.v5.datastores.implementations.bdb.BdbTruePatchStore;
import nl.knaw.huygens.timbuctoo.v5.datastores.implementations.bdb.BdbTypeNameStore;
import nl.knaw.huygens.timbuctoo.v5.datastores.implementations.bdb.DefaultResourcesStore;
import nl.knaw.huygens.timbuctoo.v5.datastores.implementations.bdb.OldSubjectTypesStore;
import nl.knaw.huygens.timbuctoo.v5.datastores.implementations.bdb.GraphStore;
import nl.knaw.huygens.timbuctoo.v5.datastores.implementations.bdb.StoreUpdater;
import nl.knaw.huygens.timbuctoo.v5.datastores.implementations.bdb.UpdatedPerPatchStore;
import nl.knaw.huygens.timbuctoo.v5.datastores.prefixstore.TypeNameStore;
import nl.knaw.huygens.timbuctoo.v5.datastores.quadstore.QuadStore;
import nl.knaw.huygens.timbuctoo.v5.datastores.schemastore.SchemaStore;
import nl.knaw.huygens.timbuctoo.v5.datastores.schemastore.dto.ExplicitField;
import nl.knaw.huygens.timbuctoo.v5.filestorage.FileStorage;
import nl.knaw.huygens.timbuctoo.v5.graphql.customschema.SchemaHelper;
import nl.knaw.huygens.timbuctoo.v5.graphql.mutations.dto.CustomProvenance;
import nl.knaw.huygens.timbuctoo.v5.rml.RdfDataSourceFactory;
import org.immutables.value.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.stream.Stream;

@Value.Immutable
public abstract class DataSet {
  private static final Logger LOG = LoggerFactory.getLogger(DataSet.class);

  public static DataSet dataSet(DataSetMetaData metadata, ExecutorService executorService,
                                String rdfPrefix, BdbEnvironmentCreator dataStoreFactory,
                                Runnable onUpdated, DataSetStorage dataSetStorage, ReadOnlyChecker readOnlyChecker)
    throws IOException, DataStoreCreationException {

    String userId = metadata.getOwnerId();
    String dataSetId = metadata.getDataSetId();
    File descriptionFile = dataSetStorage.getResourceSyncDescriptionFile();
    FileStorage fileStorage = dataSetStorage.getFileStorage();

    ImportManager importManager = new ImportManager(
      dataSetStorage.getLogList(),
      fileStorage,
      fileStorage,
      dataSetStorage.getLogStorage(),
      executorService,
      dataSetStorage.getRdfIo(),
      onUpdated
    );

    try {
      importManager.subscribeToRdf(new RdfDescriptionSaver(descriptionFile, metadata.getBaseUri(),
        importManager.getImportStatus()));
    } catch (ParserConfigurationException | SAXException e) {
      LOG.error("Could not construct import manager of data set", e);
    }

    final TupleBinding<String> stringBinding = TupleBinding.getPrimitiveBinding(String.class);
    final TupleBinding<Integer> integerBinding = TupleBinding.getPrimitiveBinding(Integer.class);

    try {
      StringStringIsCleanHandler stringStringIsCleanHandler = new StringStringIsCleanHandler();
      StringIntegerIsCleanHandler stringIntIsCleanHandler = new StringIntegerIsCleanHandler();

      final BdbQuadStore quadStore = new BdbQuadStore(dataStoreFactory.getDatabase(
        userId,
        dataSetId,
        "rdfData",
        true,
        stringBinding,
        stringBinding,
        stringStringIsCleanHandler
      ));

      final GraphStore graphStore = new GraphStore(dataStoreFactory.getDatabase(
          userId,
          dataSetId,
          "graphStore",
          true,
          stringBinding,
          stringBinding,
          stringStringIsCleanHandler
      ));

      final DefaultResourcesStore defaultResourcesStore = new DefaultResourcesStore(
          dataStoreFactory.getDatabase(
              userId,
              dataSetId,
              "defaultResourcesStore",
              true,
              stringBinding,
              stringBinding,
              stringStringIsCleanHandler
          ),
          importManager.getImportStatus()
      );

      final BdbTypeNameStore typeNameStore = new BdbTypeNameStore(
        new BdbBackedData(dataStoreFactory.getDatabase(
          userId,
          dataSetId,
          "typenames",
          false,
          stringBinding,
          stringBinding,
          stringStringIsCleanHandler
        )),
        rdfPrefix
      );

      final BdbSchemaStore schema = new BdbSchemaStore(
        new BdbBackedData(dataStoreFactory.getDatabase(
          userId,
          dataSetId,
          "schema",
          false,
          stringBinding,
          stringBinding,
          stringStringIsCleanHandler
        )),
        importManager.getImportStatus()
      );

      BdbWrapper<String, Integer> updatedPerPatchStoreWrapper = dataStoreFactory.getDatabase(
          userId,
          dataSetId,
          "updatedPerPatch",
          true,
          stringBinding,
          integerBinding,
          stringIntIsCleanHandler
      );

      try (Stream<String> keys = updatedPerPatchStoreWrapper.databaseGetter().getAll()
                                                            .getKeys(updatedPerPatchStoreWrapper.keyRetriever())) {
        keys.findFirst();
      } catch (IllegalArgumentException e) {
        updatedPerPatchStoreWrapper.close();
        dataStoreFactory.closeDatabase(userId, dataSetId, "updatedPerPatch");
        dataStoreFactory.renameDatabase(userId, dataSetId, "updatedPerPatch", "updatedPerPatchOld");

        updatedPerPatchStoreWrapper = dataStoreFactory.getDatabase(
            userId,
            dataSetId,
            "updatedPerPatch",
            true,
            stringBinding,
            integerBinding,
            stringIntIsCleanHandler
        );
      }

      final UpdatedPerPatchStore updatedPerPatchStore = new UpdatedPerPatchStore(updatedPerPatchStoreWrapper);

      final BdbTruePatchStore truePatchStore = new BdbTruePatchStore(version ->
          dataStoreFactory.getDatabase(
              userId,
              dataSetId,
              "truePatch" + version,
              true,
              stringBinding,
              stringBinding,
              stringStringIsCleanHandler
          ), updatedPerPatchStore
      );

      final OldSubjectTypesStore oldSubjectTypesStore = new OldSubjectTypesStore(dataStoreFactory.getDatabase(
          userId,
          dataSetId,
          "oldSubjectTypes",
          true,
          stringBinding,
          stringBinding,
          stringStringIsCleanHandler
      ));

      final BdbRmlDataSourceStore rmlDataSourceStore = new BdbRmlDataSourceStore(
        dataStoreFactory.getDatabase(
          userId,
          dataSetId,
          "rmlSource",
          true,
          stringBinding,
          stringBinding,
          stringStringIsCleanHandler
        ),
        importManager.getImportStatus()
      );

      final GraphStore graphStore = new GraphStore(
          dataStoreFactory.getDatabase(
              userId,
              dataSetId,
              "graphStore",
              true,
              stringBinding,
              stringBinding,
              stringStringIsCleanHandler
          )
      );

      final StoreUpdater storeUpdater = new StoreUpdater(
        quadStore,
        graphStore,
        typeNameStore,
        truePatchStore,
        updatedPerPatchStore,
        oldSubjectTypesStore,
        Lists.newArrayList(schema, rmlDataSourceStore, defaultResourcesStore),
        importManager.getImportStatus()
      );
      importManager.subscribeToRdf(storeUpdater);

      final ChangesRetriever changesRetriever = new ChangesRetriever(truePatchStore, updatedPerPatchStore);

      final CurrentStateRetriever currentStateRetriever = new CurrentStateRetriever(quadStore);

      ImmutableDataSet dataSet = ImmutableDataSet.builder()
        .ownerId(userId)
        .dataSetName(dataSetId)
        .bdbEnvironmentCreator(dataStoreFactory)
        .metadata(metadata)
        .quadStore(quadStore)
        .graphStore(graphStore)
        .defaultResourcesStore(defaultResourcesStore)
        .typeNameStore(typeNameStore)
        .schemaStore(schema)
        .updatedPerPatchStore(updatedPerPatchStore)
        .truePatchStore(truePatchStore)
        .oldSubjectTypesStore(oldSubjectTypesStore)
        .dataSource(new RdfDataSourceFactory(rmlDataSourceStore))
        .importManager(importManager)
        .dataSetStorage(dataSetStorage)
        .changesRetriever(changesRetriever)
        .currentStateRetriever(currentStateRetriever)
        .readOnlyChecker(readOnlyChecker)
        .build();
      importManager.init(dataSet);

      if (!quadStore.isClean() || !graphStore.isClean() || !defaultResourcesStore.isClean() ||
        !typeNameStore.isClean() || !schema.isClean() || !truePatchStore.isClean() ||
        !updatedPerPatchStore.isClean() || !oldSubjectTypesStore.isClean() || !rmlDataSourceStore.isClean()) {
        LOG.error("Data set '{}__{}' data is corrupted, starting to reimport.", userId, dataSetId);
        quadStore.empty();
        graphStore.empty();
        defaultResourcesStore.empty();
        typeNameStore.empty();
        schema.empty();
        truePatchStore.empty();
        updatedPerPatchStore.empty();
        oldSubjectTypesStore.empty();
        rmlDataSourceStore.empty();
        graphStore.empty();

        importManager.reprocessLogs();
      } else {
        importManager.processLogs(); // process unprocessed logs
      }

      return dataSet;
    } catch (BdbDbCreationException e) {
      throw new DataStoreCreationException(e.getCause());
    }
  }

  public void stop() {
    // close the database environment
    getBdbEnvironmentCreator().closeEnvironment(getOwnerId(), getDataSetName());
  }

  public File getResourceSyncDescription() {
    return getDataSetStorage().getResourceSyncDescriptionFile();
  }

  private File getCustomSchemaFile() {
    return getDataSetStorage().getCustomSchemaFile();
  }

  public FileStorage getFileStorage() throws IOException {
    return getDataSetStorage().getFileStorage();
  }

  public Map<String, List<ExplicitField>> getCustomSchema() {
    return SchemaHelper.readExistingSchema(getCustomSchemaFile());
  }

  public void saveCustomSchema(Map<String, List<ExplicitField>> schema) throws IOException {
    SchemaHelper.saveSchema(schema, getCustomSchemaFile());
  }

  public CustomProvenance getCustomProvenance() {
    File customProvenanceFile = getDataSetStorage().getCustomProvenanceFile();
    if (customProvenanceFile.exists()) {
      try {
        return new ObjectMapper().readValue(customProvenanceFile, CustomProvenance.class);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
    return new CustomProvenance(Collections.emptyList());
  }

  public void setCustomProvenance(CustomProvenance customProvenance) throws IOException {
    new ObjectMapper().writeValue(getDataSetStorage().getCustomProvenanceFile(), customProvenance);
  }

  protected abstract String getOwnerId();

  protected abstract String getDataSetName();

  protected abstract BdbEnvironmentCreator getBdbEnvironmentCreator();

  protected abstract DataSetStorage getDataSetStorage();

  public abstract SchemaStore getSchemaStore();

  public abstract UpdatedPerPatchStore getUpdatedPerPatchStore();

  public abstract BdbTruePatchStore getTruePatchStore();

  public abstract TypeNameStore getTypeNameStore();

  public abstract OldSubjectTypesStore getOldSubjectTypesStore();

  public abstract DefaultResourcesStore getDefaultResourcesStore();

  public abstract ImportManager getImportManager();

  public abstract RdfDataSourceFactory getDataSource();

  public abstract QuadStore getQuadStore();

  public abstract GraphStore getGraphStore();

  public abstract DataSetMetaData getMetadata();

  public abstract ChangesRetriever getChangesRetriever();

  public abstract CurrentStateRetriever getCurrentStateRetriever();

  public abstract ReadOnlyChecker getReadOnlyChecker();

  public void subscribeToDataChanges(Runnable updateListener) {
    getImportManager().subscribeImportSucceeded(updateListener);
  }

  public List<String> getUnavailableStores() {
    return getBdbEnvironmentCreator().getUnavailableDatabases(getMetadata().getOwnerId(), getMetadata().getDataSetId());
  }

  public void backupDatabases(String backupPath) throws IOException {
    getBdbEnvironmentCreator().backUpDatabases(backupPath, getMetadata().getOwnerId(), getMetadata().getDataSetId());
  }
}
