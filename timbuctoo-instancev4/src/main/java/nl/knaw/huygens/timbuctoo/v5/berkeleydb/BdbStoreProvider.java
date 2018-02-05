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

public class BdbStoreProvider {
  private static final StringStringIsCleanHandler stringStringIsCleanHandler = new StringStringIsCleanHandler();
  private static final TupleBinding<String> stringBinding = TupleBinding.getPrimitiveBinding(String.class);
  private static final TupleBinding<Integer> integerBinding = TupleBinding.getPrimitiveBinding(Integer.class);
  private final BdbEnvironmentCreator dataStoreFactory;

  public BdbStoreProvider(BdbEnvironmentCreator dataStoreFactory) {
    this.dataStoreFactory = dataStoreFactory;
  }

  public BdbTripleStore createTripleStore(String userId, String dataSetId)
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

  public BdbTypeNameStore createTypeNameStore(String userId, String dataSetId, String rdfPrefix)
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

  public BdbSchemaStore createSchemaStore(String userId, String dataSetId, ImportStatus importStatus)
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

  public TruePatchStore createTruePatchStore(String userId, String dataSetId) throws DataStoreCreationException {
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

  public UpdatedPerPatchStore createUpdatePerPatchStore(String userId, String dataSetId)
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

  public BdbRmlDataSourceStore createRmlDataSourceStore(String userId, String dataSetId,
                                                        ImportStatus importStatus)
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


  public VersionStore createVersionStore(String userId, String dataSetId) throws DataStoreCreationException {
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
