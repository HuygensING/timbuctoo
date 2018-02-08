package nl.knaw.huygens.timbuctoo.v5.berkeleydb;

import com.sleepycat.bind.tuple.TupleBinding;
import nl.knaw.huygens.timbuctoo.v5.berkeleydb.exceptions.BdbDbCreationException;
import nl.knaw.huygens.timbuctoo.v5.berkeleydb.isclean.IsCleanHandler;
import nl.knaw.huygens.timbuctoo.v5.berkeleydb.isclean.StringStringIsCleanHandler;
import nl.knaw.huygens.timbuctoo.v5.dataset.ImportStatus;
import nl.knaw.huygens.timbuctoo.v5.dataset.exceptions.DataStoreCreationException;
import nl.knaw.huygens.timbuctoo.v5.datastores.implementations.bdb.BdbBackedData;
import nl.knaw.huygens.timbuctoo.v5.datastores.implementations.bdb.BdbRmlDataSourceStore;
import nl.knaw.huygens.timbuctoo.v5.datastores.implementations.bdb.BdbSchemaStore;
import nl.knaw.huygens.timbuctoo.v5.datastores.implementations.bdb.BdbTripleStore;
import nl.knaw.huygens.timbuctoo.v5.datastores.implementations.bdb.BdbTruePatchStore;
import nl.knaw.huygens.timbuctoo.v5.datastores.implementations.bdb.BdbTypeNameStore;
import nl.knaw.huygens.timbuctoo.v5.datastores.implementations.bdb.BdbUpdatedPerPatchStore;
import nl.knaw.huygens.timbuctoo.v5.datastores.implementations.bdb.BdbVersionStore;
import nl.knaw.huygens.timbuctoo.v5.datastores.updatedperpatchstore.UpdatedPerPatchStore;
import nl.knaw.huygens.timbuctoo.v5.datastores.versionstore.VersionStore;
import nl.knaw.huygens.timbuctoo.v5.datastores.truepatch.TruePatchStore;

import java.io.IOException;

public class BdbStoreProvider implements nl.knaw.huygens.timbuctoo.v5.dataset.StoreProvider {
  private static final StringStringIsCleanHandler stringStringIsCleanHandler = new StringStringIsCleanHandler();
  private static final TupleBinding<String> stringBinding = TupleBinding.getPrimitiveBinding(String.class);
  private static final TupleBinding<Integer> integerBinding = TupleBinding.getPrimitiveBinding(Integer.class);
  private final String userId;
  private final String dataSetId;
  private final BdbEnvironmentCreator dataStoreFactory;

  public BdbStoreProvider(String userId, String dataSetId, BdbEnvironmentCreator dataStoreFactory) {
    this.userId = userId;
    this.dataSetId = dataSetId;
    this.dataStoreFactory = dataStoreFactory;
  }

  @Override
  public BdbTripleStore createTripleStore()
    throws DataStoreCreationException {
    try {
      return new BdbTripleStore(dataStoreFactory.getDatabase(
        userId,
        dataSetId,
        "rdfData",
        true,
        stringBinding,
        stringBinding,
        stringStringIsCleanHandler
      ));
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
          stringBinding,
          stringBinding,
          stringStringIsCleanHandler
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
          stringBinding,
          stringBinding,
          stringStringIsCleanHandler
        )),
        importStatus
      );
    } catch (IOException | BdbDbCreationException e) {
      throw new DataStoreCreationException(e);
    }
  }

  @Override
  public TruePatchStore createTruePatchStore() throws DataStoreCreationException {
    try {
      return new BdbTruePatchStore(
        dataStoreFactory.getDatabase(
          userId,
          dataSetId,
          "truePatch",
          true,
          stringBinding,
          stringBinding,
          stringStringIsCleanHandler
        )
      );
    } catch (BdbDbCreationException e) {
      throw new DataStoreCreationException(e);
    }
  }

  @Override
  public UpdatedPerPatchStore createUpdatePerPatchStore()
    throws DataStoreCreationException {
    try {
      return new BdbUpdatedPerPatchStore(
        dataStoreFactory.getDatabase(
          userId,
          dataSetId,
          "updatedPerPatch",
          true,
          integerBinding,
          stringBinding,
          new IsCleanHandler<Integer, String>() {
            @Override
            public Integer getKey() {
              return Integer.MAX_VALUE;
            }

            @Override
            public String getValue() {
              return "isClean";
            }
          }
        )
      );
    } catch (BdbDbCreationException e) {
      throw new DataStoreCreationException(e);
    }
  }

  @Override
  public BdbRmlDataSourceStore createRmlDataSourceStore(ImportStatus importStatus)
    throws DataStoreCreationException {
    try {
      return new BdbRmlDataSourceStore(
        dataStoreFactory.getDatabase(
          userId,
          dataSetId,
          "rmlSource",
          true,
          stringBinding,
          stringBinding,
          stringStringIsCleanHandler
        ),
        importStatus
      );
    } catch (BdbDbCreationException e) {
      throw new DataStoreCreationException(e);
    }
  }


  @Override
  public VersionStore createVersionStore() throws DataStoreCreationException {
    try {
      return new BdbVersionStore(dataStoreFactory.getDatabase(
        userId,
        dataSetId,
        "versions",
        false,
        stringBinding,
        integerBinding,
        new IsCleanHandler<String, Integer>() {
          @Override
          public String getKey() {
            return "isClean";
          }

          @Override
          public Integer getValue() {
            return Integer.MAX_VALUE;
          }
        }
      ));
    } catch (BdbDbCreationException e) {
      throw new DataStoreCreationException(e);
    }
  }
}
