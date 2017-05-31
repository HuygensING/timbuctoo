package nl.knaw.huygens.timbuctoo.v5.dataset;

import com.fasterxml.jackson.core.type.TypeReference;
import nl.knaw.huygens.timbuctoo.security.VreAuthorizationCrud;
import nl.knaw.huygens.timbuctoo.security.exceptions.AuthorizationCreationException;
import nl.knaw.huygens.timbuctoo.v5.datastores.exceptions.DataStoreCreationException;
import nl.knaw.huygens.timbuctoo.v5.datastores.jsonfilebackeddata.JsonFileBackedData;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;

/**
 * - stores all configuration parameters so it can inject them in the dataset constructor
 * - makes DataSets a singleton
 * - keeps track of all created dataSets across restarts (stores them in a file)
 */
public class DataSetFactory {

  private final ExecutorService executorService;
  private final VreAuthorizationCrud vreAuthorizationCrud;
  private final DataSetConfiguration configuration;
  private final Map<String, Map<String, DataProvider>> dataSetMap;
  private final JsonFileBackedData<Map<String, List<String>>> storedDataSets;

  public DataSetFactory(ExecutorService executorService, VreAuthorizationCrud vreAuthorizationCrud,
                        DataSetConfiguration configuration) throws IOException {
    this.executorService = executorService;
    this.vreAuthorizationCrud = vreAuthorizationCrud;
    this.configuration = configuration;
    dataSetMap = new HashMap<>();
    new File(configuration.getDataSetMetadataLocation()).mkdirs();
    storedDataSets = JsonFileBackedData.getOrCreate(
      new File(configuration.getDataSetMetadataLocation(), "dataSets.json"),
      new HashMap<>(),
      new TypeReference<Map<String, List<String>>>() {}
    );
  }

  public DataSet getOrCreate(String userId, String dataSetId) throws DataStoreCreationException {
    String authorizationKey = userId + "_" + dataSetId;
    synchronized (dataSetMap) {
      Map<String, DataProvider> userDataSets = dataSetMap.computeIfAbsent(userId, key -> new HashMap<>());
      if (!userDataSets.containsKey(dataSetId)) {
        try {
          vreAuthorizationCrud.createAuthorization(authorizationKey, userId, "ADMIN");
          userDataSets.put(dataSetId, new DataSet(
            new File(configuration.getDataSetMetadataLocation(), userId + "_" + dataSetId + "-log.json"),
            configuration.getFileStorage().makeFileStorage(userId, dataSetId),
            configuration.getFileStorage().makeFileStorage(userId, dataSetId),
            configuration.getFileStorage().makeLogStorage(userId, dataSetId),
            executorService,
            configuration.getRdfIo()
          ));
          storedDataSets.updateData(dataSets -> {
            dataSets.computeIfAbsent(userId, key -> new ArrayList<>()).add(dataSetId);
            return dataSets;
          });
        } catch (AuthorizationCreationException | IOException e) {
          throw new DataStoreCreationException(e);
        }
      }
      return userDataSets.get(dataSetId);
    }
  }
}
