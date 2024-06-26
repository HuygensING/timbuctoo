package nl.knaw.huygens.timbuctoo.dataset.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.sleepycat.bind.tuple.TupleBinding;
import nl.knaw.huygens.timbuctoo.dataset.exceptions.DataStoreCreationException;
import nl.knaw.huygens.timbuctoo.datastorage.DataSetStorage;
import nl.knaw.huygens.timbuctoo.datastores.implementations.RdfDescriptionSaver;
import nl.knaw.huygens.timbuctoo.datastores.implementations.bdb.BdbBackedData;
import nl.knaw.huygens.timbuctoo.datastores.implementations.bdb.BdbPatchVersionStore;
import nl.knaw.huygens.timbuctoo.datastores.implementations.bdb.BdbQuadStore;
import nl.knaw.huygens.timbuctoo.datastores.implementations.bdb.BdbSchemaStore;
import nl.knaw.huygens.timbuctoo.datastores.implementations.bdb.BdbTypeNameStore;
import nl.knaw.huygens.timbuctoo.datastores.implementations.bdb.DefaultResourcesStore;
import nl.knaw.huygens.timbuctoo.datastores.implementations.bdb.GraphStore;
import nl.knaw.huygens.timbuctoo.datastores.implementations.bdb.OldSubjectTypesStore;
import nl.knaw.huygens.timbuctoo.datastores.implementations.bdb.StoreUpdater;
import nl.knaw.huygens.timbuctoo.datastores.implementations.bdb.UpdatedPerPatchStore;
import nl.knaw.huygens.timbuctoo.datastores.prefixstore.TypeNameStore;
import nl.knaw.huygens.timbuctoo.datastores.quadstore.QuadStore;
import nl.knaw.huygens.timbuctoo.datastores.schemastore.SchemaStore;
import nl.knaw.huygens.timbuctoo.datastores.schemastore.dto.ExplicitField;
import nl.knaw.huygens.timbuctoo.graphql.customschema.SchemaHelper;
import nl.knaw.huygens.timbuctoo.graphql.mutations.dto.CustomProvenance;
import nl.knaw.huygens.timbuctoo.berkeleydb.BdbEnvironmentCreator;
import nl.knaw.huygens.timbuctoo.berkeleydb.exceptions.BdbDbCreationException;
import nl.knaw.huygens.timbuctoo.berkeleydb.isclean.StringIntegerIsCleanHandler;
import nl.knaw.huygens.timbuctoo.berkeleydb.isclean.StringStringIsCleanHandler;
import nl.knaw.huygens.timbuctoo.dataset.CurrentStateRetriever;
import nl.knaw.huygens.timbuctoo.dataset.ImportManager;
import nl.knaw.huygens.timbuctoo.dataset.ReadOnlyChecker;
import nl.knaw.huygens.timbuctoo.filestorage.ChangeLogStorage;
import nl.knaw.huygens.timbuctoo.filestorage.FileStorage;
import nl.knaw.huygens.timbuctoo.filestorage.LogStorage;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
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

@Value.Immutable
public abstract class DataSet {
  private static final Logger LOG = LoggerFactory.getLogger(DataSet.class);

  public static DataSet dataSet(DataSetMetaData dataSetMetaData, ExecutorService executorService,
                                String rdfPrefix, BdbEnvironmentCreator dataStoreFactory,
                                Metadata metadata, Runnable onUpdated,
                                DataSetStorage dataSetStorage, ReadOnlyChecker readOnlyChecker)
    throws IOException, DataStoreCreationException {

    String userId = dataSetMetaData.getOwnerId();
    String dataSetId = dataSetMetaData.getDataSetId();
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
      RdfDescriptionSaver descriptionSaver = new RdfDescriptionSaver(
              metadata, descriptionFile, dataSetMetaData.getBaseUri(), importManager.getImportStatus());
      importManager.subscribeToRdf(descriptionSaver);
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

      final UpdatedPerPatchStore updatedPerPatchStore = new UpdatedPerPatchStore(dataStoreFactory.getDatabase(
          userId,
          dataSetId,
          "updatedPerPatch",
          true,
          stringBinding,
          integerBinding,
          stringIntIsCleanHandler
      ));

      final BdbPatchVersionStore patchVersionStore = new BdbPatchVersionStore(dataStoreFactory.getDatabase(
          userId,
          dataSetId,
          "patchVersion",
          true,
          stringBinding,
          stringBinding,
          stringStringIsCleanHandler
      ));

      final OldSubjectTypesStore oldSubjectTypesStore = new OldSubjectTypesStore(dataStoreFactory.getDatabase(
          userId,
          dataSetId,
          "oldSubjectTypes",
          true,
          stringBinding,
          stringBinding,
          stringStringIsCleanHandler
      ));

      final StoreUpdater storeUpdater = new StoreUpdater(
        quadStore,
        graphStore,
        typeNameStore,
        patchVersionStore,
        updatedPerPatchStore,
        oldSubjectTypesStore,
        Lists.newArrayList(schema, defaultResourcesStore),
        importManager.getImportStatus(),
        dataSetStorage.getChangeLogStorage()
      );
      importManager.subscribeToRdf(storeUpdater);

      final CurrentStateRetriever currentStateRetriever = new CurrentStateRetriever(quadStore);

      ImmutableDataSet dataSet = ImmutableDataSet.builder()
        .ownerId(userId)
        .dataSetName(dataSetId)
        .bdbEnvironmentCreator(dataStoreFactory)
        .metadata(dataSetMetaData)
        .quadStore(quadStore)
        .graphStore(graphStore)
        .defaultResourcesStore(defaultResourcesStore)
        .typeNameStore(typeNameStore)
        .schemaStore(schema)
        .updatedPerPatchStore(updatedPerPatchStore)
        .patchVersionStore(patchVersionStore)
        .oldSubjectTypesStore(oldSubjectTypesStore)
        .importManager(importManager)
        .dataSetStorage(dataSetStorage)
        .currentStateRetriever(currentStateRetriever)
        .readOnlyChecker(readOnlyChecker)
        .build();
      importManager.init(dataSet);

      if (!quadStore.isClean() || !graphStore.isClean() || !defaultResourcesStore.isClean() ||
        !typeNameStore.isClean() || !schema.isClean() || !patchVersionStore.isClean() ||
        !updatedPerPatchStore.isClean() || !oldSubjectTypesStore.isClean()) {
        LOG.error("Data set '{}__{}' data is corrupted, starting to reimport.", userId, dataSetId);
        quadStore.empty();
        graphStore.empty();
        defaultResourcesStore.empty();
        typeNameStore.empty();
        schema.empty();
        patchVersionStore.empty();
        updatedPerPatchStore.empty();
        oldSubjectTypesStore.empty();

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

  public LogStorage getLogStorage() throws IOException {
    return getDataSetStorage().getLogStorage();
  }

  public ChangeLogStorage getChangeLogStorage() {
    return getDataSetStorage().getChangeLogStorage();
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

  public Model initModel() {
    Model model = new LinkedHashModel();
    getTypeNameStore().getMappings().forEach(model::setNamespace);
    return model;
  }

  protected abstract String getOwnerId();

  protected abstract String getDataSetName();

  protected abstract BdbEnvironmentCreator getBdbEnvironmentCreator();

  protected abstract DataSetStorage getDataSetStorage();

  public abstract SchemaStore getSchemaStore();

  public abstract UpdatedPerPatchStore getUpdatedPerPatchStore();

  public abstract BdbPatchVersionStore getPatchVersionStore();

  public abstract TypeNameStore getTypeNameStore();

  public abstract OldSubjectTypesStore getOldSubjectTypesStore();

  public abstract DefaultResourcesStore getDefaultResourcesStore();

  public abstract ImportManager getImportManager();

  public abstract QuadStore getQuadStore();

  public abstract GraphStore getGraphStore();

  public abstract DataSetMetaData getMetadata();

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
