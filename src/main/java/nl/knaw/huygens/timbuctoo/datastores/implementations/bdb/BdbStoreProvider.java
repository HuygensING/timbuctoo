package nl.knaw.huygens.timbuctoo.datastores.implementations.bdb;

import com.sleepycat.bind.tuple.TupleBinding;
import nl.knaw.huygens.timbuctoo.berkeleydb.BdbEnvironmentCreator;
import nl.knaw.huygens.timbuctoo.berkeleydb.exceptions.BdbDbCreationException;
import nl.knaw.huygens.timbuctoo.berkeleydb.isclean.StringIntegerIsCleanHandler;
import nl.knaw.huygens.timbuctoo.berkeleydb.isclean.StringStringIsCleanHandler;
import nl.knaw.huygens.timbuctoo.dataset.ImportStatus;
import nl.knaw.huygens.timbuctoo.dataset.StoreProvider;
import nl.knaw.huygens.timbuctoo.dataset.exceptions.DataStoreCreationException;

import java.io.IOException;

public class BdbStoreProvider implements StoreProvider {
  private static final TupleBinding<String> STRING_BINDING = TupleBinding.getPrimitiveBinding(String.class);
  private static final TupleBinding<Integer> INTEGER_BINDING = TupleBinding.getPrimitiveBinding(Integer.class);
  private static final StringStringIsCleanHandler STRING_STRING_IS_CLEAN_HANDLER = new StringStringIsCleanHandler();
  private static final StringIntegerIsCleanHandler STRING_INTEGER_IS_CLEAN_HANDLER = new StringIntegerIsCleanHandler();

  private final String userId;
  private final String dataSetId;
  private final BdbEnvironmentCreator dataStoreFactory;

  public BdbStoreProvider(String userId, String dataSetId, BdbEnvironmentCreator dataStoreFactory) {
    this.userId = userId;
    this.dataSetId = dataSetId;
    this.dataStoreFactory = dataStoreFactory;
  }

  @Override
  public BdbQuadStore createQuadStore()
      throws DataStoreCreationException {
    try {
      return new BdbQuadStore(dataStoreFactory.getDatabase(
          userId,
          dataSetId,
          "rdfData",
          true,
          STRING_BINDING,
          STRING_BINDING,
          STRING_STRING_IS_CLEAN_HANDLER
      ));
    } catch (BdbDbCreationException e) {
      throw new DataStoreCreationException(e);
    }
  }

  @Override
  public BdbGraphStore createGraphStore()
      throws DataStoreCreationException {
    try {
      return new BdbGraphStore(dataStoreFactory.getDatabase(
          userId,
          dataSetId,
          "graphStore",
          true,
          STRING_BINDING,
          STRING_BINDING,
          STRING_STRING_IS_CLEAN_HANDLER
      ));
    } catch (BdbDbCreationException e) {
      throw new DataStoreCreationException(e);
    }
  }

  @Override
  public BdbDefaultResourcesStore createDefaultResourcesStore(ImportStatus importStatus)
      throws DataStoreCreationException {
    try {
      return new BdbDefaultResourcesStore(dataStoreFactory.getDatabase(
          userId,
          dataSetId,
          "defaultResourcesStore",
          true,
          STRING_BINDING,
          STRING_BINDING,
          STRING_STRING_IS_CLEAN_HANDLER
      ), importStatus);
    } catch (BdbDbCreationException e) {
      throw new DataStoreCreationException(e);
    }
  }

  @Override
  public BdbTypeNameStore createTypeNameStore(String rdfPrefix)
      throws DataStoreCreationException {
    try {
      return new BdbTypeNameStore(
          new BdbBackedData(dataStoreFactory.getDatabase(
              userId,
              dataSetId,
              "typenames",
              false,
              STRING_BINDING,
              STRING_BINDING,
              STRING_STRING_IS_CLEAN_HANDLER
          )),
          rdfPrefix
      );
    } catch (IOException | BdbDbCreationException e) {
      throw new DataStoreCreationException(e);
    }
  }

  @Override
  public BdbSchemaStore createSchemaStore(ImportStatus importStatus)
      throws DataStoreCreationException {
    try {
      return new BdbSchemaStore(
          new BdbBackedData(dataStoreFactory.getDatabase(
              userId,
              dataSetId,
              "schema",
              false,
              STRING_BINDING,
              STRING_BINDING,
              STRING_STRING_IS_CLEAN_HANDLER
          )),
          importStatus
      );
    } catch (IOException | BdbDbCreationException e) {
      throw new DataStoreCreationException(e);
    }
  }

  @Override
  public BdbUpdatedPerPatchStore createUpdatePerPatchStore()
      throws DataStoreCreationException {
    try {
      return new BdbUpdatedPerPatchStore(dataStoreFactory.getDatabase(
          userId,
          dataSetId,
          "updatedPerPatch",
          true,
          STRING_BINDING,
          INTEGER_BINDING,
          STRING_INTEGER_IS_CLEAN_HANDLER
      ));
    } catch (BdbDbCreationException e) {
      throw new DataStoreCreationException(e);
    }
  }

  @Override
  public BdbPatchVersionStore createPatchVersionStore()
      throws DataStoreCreationException {
    try {
      return new BdbPatchVersionStore(dataStoreFactory.getDatabase(
          userId,
          dataSetId,
          "patchVersion",
          true,
          STRING_BINDING,
          STRING_BINDING,
          STRING_STRING_IS_CLEAN_HANDLER
      ));
    } catch (BdbDbCreationException e) {
      throw new DataStoreCreationException(e);
    }
  }

  @Override
  public BdbOldSubjectTypesStore createOldSubjectTypesStore() throws DataStoreCreationException {
    try {
      return new BdbOldSubjectTypesStore(dataStoreFactory.getDatabase(
          userId,
          dataSetId,
          "oldSubjectTypes",
          true,
          STRING_BINDING,
          STRING_BINDING,
          STRING_STRING_IS_CLEAN_HANDLER
      ));
    } catch (BdbDbCreationException e) {
      throw new DataStoreCreationException(e);
    }
  }
}
