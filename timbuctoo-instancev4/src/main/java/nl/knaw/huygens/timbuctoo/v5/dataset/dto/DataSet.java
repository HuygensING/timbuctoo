package nl.knaw.huygens.timbuctoo.v5.dataset.dto;

import com.google.common.collect.Lists;
import com.sleepycat.bind.tuple.TupleBinding;
import nl.knaw.huygens.timbuctoo.v5.berkeleydb.BdbEnvironmentCreator;
import nl.knaw.huygens.timbuctoo.v5.dataset.DataSetConfiguration;
import nl.knaw.huygens.timbuctoo.v5.dataset.ImportManager;
import nl.knaw.huygens.timbuctoo.v5.dataset.exceptions.DataStoreCreationException;
import nl.knaw.huygens.timbuctoo.v5.datastores.implementations.bdb.BdbBackedData;
import nl.knaw.huygens.timbuctoo.v5.datastores.implementations.bdb.BdbRmlDataSourceStore;
import nl.knaw.huygens.timbuctoo.v5.datastores.implementations.RdfDescriptionSaver;
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
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutorService;

@Value.Immutable
public interface DataSet {

  static DataSet dataSet(PromotedDataSet metadata, DataSetConfiguration configuration,
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

    try {
      importManager.subscribeToRdf(new RdfDescriptionSaver(descriptionFile, metadata.getBaseUri()));
    } catch (ParserConfigurationException e) {
      e.printStackTrace();
    } catch (SAXException e) {
      e.printStackTrace();
    }
    
    final TupleBinding<String> stringBinding = TupleBinding.getPrimitiveBinding(String.class);
    BdbTripleStore quadStore = new BdbTripleStore(dataStoreFactory.getDatabase(
      userId,
      dataSetId,
      "rdfData",
      true,
      stringBinding,
      stringBinding
    ));
    final BdbTypeNameStore typeNameStore = new BdbTypeNameStore(
      new BdbBackedData(dataStoreFactory.getDatabase(
        userId,
        dataSetId,
        "typenames",
        false,
        stringBinding,
        stringBinding
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
        stringBinding
      ))
    );
    final BdbTruePatchStore truePatchStore = new BdbTruePatchStore(
      dataStoreFactory.getDatabase(
        userId,
        dataSetId,
        "truePatch",
        true,
        stringBinding,
        stringBinding
      )
    );
    final TupleBinding<Integer> integerBinding = TupleBinding.getPrimitiveBinding(Integer.class);
    final UpdatedPerPatchStore updatedPerPatchStore = new UpdatedPerPatchStore(
      dataStoreFactory.getDatabase(
        userId,
        dataSetId,
        "updatedPerPatch",
        true,
        integerBinding,
        stringBinding
      )
    );
    final BdbRmlDataSourceStore rmlDataSourceStore = new BdbRmlDataSourceStore(
      dataStoreFactory.getDatabase(
        userId,
        dataSetId,
        "rmlSource",
        true,
        stringBinding,
        stringBinding)
    );
    VersionStore versionStore = new VersionStore(dataStoreFactory.getDatabase(
      userId,
      dataSetId,
      "versions",
      false,
      stringBinding,
      integerBinding
    ));
    final StoreUpdater storeUpdater = new StoreUpdater(
      dataStoreFactory,
      quadStore,
      typeNameStore,
      truePatchStore,
      updatedPerPatchStore,
      Lists.newArrayList(schema, rmlDataSourceStore),
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
