package nl.knaw.huygens.timbuctoo.v5.dataset;

import com.fasterxml.jackson.core.type.TypeReference;
import nl.knaw.huygens.timbuctoo.security.VreAuthorizationCrud;
import nl.knaw.huygens.timbuctoo.security.dto.VreAuthorization;
import nl.knaw.huygens.timbuctoo.security.exceptions.AuthorizationCreationException;
import nl.knaw.huygens.timbuctoo.security.exceptions.AuthorizationUnavailableException;
import nl.knaw.huygens.timbuctoo.util.Tuple;
import nl.knaw.huygens.timbuctoo.v5.dataset.dto.DataSet;
import nl.knaw.huygens.timbuctoo.v5.datastores.exceptions.DataStoreCreationException;
import nl.knaw.huygens.timbuctoo.v5.datastores.jsonfilebackeddata.JsonFileBackedData;
import nl.knaw.huygens.timbuctoo.v5.datastores.resourcesync.ResourceSync;
import nl.knaw.huygens.timbuctoo.v5.datastores.resourcesync.ResourceSyncException;
import nl.knaw.huygens.timbuctoo.v5.filestorage.implementations.filesystem.FileHelper;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import static nl.knaw.huygens.timbuctoo.v5.dataset.PromotedDataSet.promotedDataSet;
import static nl.knaw.huygens.timbuctoo.v5.dataset.dto.DataSet.dataSet;

/**
 * - stores all configuration parameters so it can inject them in the dataset constructor
 * - makes CreateDataSet a singleton
 * - keeps track of all created dataSets across restarts (stores them in a file)
 */
public class DataSetRepository {

  private final ExecutorService executorService;
  private final VreAuthorizationCrud vreAuthorizationCrud;
  private final DataSetConfiguration configuration;
  private final DataStoreFactory dataStoreFactory;
  private final Map<String, Map<String, DataSet>> dataSetMap;
  private final JsonFileBackedData<Map<String, Set<PromotedDataSet>>> storedDataSets;
  private final HashMap<UUID, StringBuffer> statusMap;
  private final FileHelper fileHelper;
  private final ResourceSync resourceSync;


  public DataSetRepository(ExecutorService executorService, VreAuthorizationCrud vreAuthorizationCrud,
                           DataSetConfiguration configuration,
                           DataStoreFactory dataStoreFactory) throws IOException {
    this.executorService = executorService;
    this.vreAuthorizationCrud = vreAuthorizationCrud;
    this.configuration = configuration;
    this.dataStoreFactory = dataStoreFactory;

    fileHelper = new FileHelper(configuration.getDataSetMetadataLocation());
    storedDataSets = JsonFileBackedData.getOrCreate(
      new File(configuration.getDataSetMetadataLocation(), "dataSets.json"),
      HashMap::new,
      new TypeReference<Map<String, Set<PromotedDataSet>>>() {
      }
    );
    statusMap = new HashMap<>();
    resourceSync = configuration.getResourceSync();

    dataSetMap = new HashMap<>();
  }

  private void loadDataSetsFromJson() throws IOException {
    synchronized (dataSetMap) {
      for (Map.Entry<String, Set<PromotedDataSet>> entry : storedDataSets.getData().entrySet()) {
        String ownerId = entry.getKey();
        HashMap<String, DataSet> ownersSets = new HashMap<>();
        dataSetMap.put(ownerId, ownersSets);
        for (PromotedDataSet promotedDataSet : entry.getValue()) {
          String dataSetName = promotedDataSet.getName();
          try {
            ownersSets.put(
              dataSetName,
              dataSet(ownerId, dataSetName, configuration, fileHelper, executorService, dataStoreFactory, resourceSync)
            );
          } catch (IOException | DataStoreCreationException | ResourceSyncException e) {
            throw new IOException(e);
          }
        }
      }
    }
  }

  public Optional<DataSet> getDataSet(String ownerId, String dataSetId) {
    synchronized (dataSetMap) {
      if (dataSetMap.containsKey(ownerId) && dataSetMap.get(ownerId).containsKey(dataSetId)) {
        return Optional.ofNullable(dataSetMap.get(ownerId).get(dataSetId));
      } else {
        return Optional.empty();
      }
    }
  }

  public DataSet createDataSet(String ownerId, String dataSetId) throws DataStoreCreationException {
    String authorizationKey = ownerId + "_" + dataSetId;
    synchronized (dataSetMap) {
      Map<String, DataSet> userDataSets = dataSetMap.computeIfAbsent(ownerId, key -> new HashMap<>());

      if (!userDataSets.containsKey(dataSetId)) {
        try {
          vreAuthorizationCrud.createAuthorization(authorizationKey, ownerId, "ADMIN");
          userDataSets.put(
            dataSetId,
            dataSet(ownerId, dataSetId, configuration, fileHelper, executorService, dataStoreFactory, resourceSync)
          );
          storedDataSets.updateData(dataSets -> {
            dataSets
              .computeIfAbsent(ownerId, key -> new HashSet<>())
              .add(promotedDataSet(dataSetId, false));
            return dataSets;
          });
        } catch (AuthorizationCreationException | IOException | ResourceSyncException e1) {
          throw new DataStoreCreationException(e1);
        }
      }
      return userDataSets.get(dataSetId);
    }
  }

  public boolean dataSetExists(String ownerId, String dataSet) {
    return getDataSet(ownerId, dataSet).isPresent();
  }

  public Map<String, Set<PromotedDataSet>> getDataSets() {
    return storedDataSets.getData();
  }

  public Map<String, Set<PromotedDataSet>> getPromotedDataSets() {
    Map<String, Set<PromotedDataSet>> dataSets = storedDataSets.getData();
    Map<String, Set<PromotedDataSet>> promotedDataSets = new HashMap<>();

    for (Map.Entry<String, Set<PromotedDataSet>> userDataSets : dataSets.entrySet()) {
      Set<PromotedDataSet> mappedUserSets = userDataSets.getValue()
                                                        .stream()
                                                        .filter(dataSet -> dataSet.isPromoted())
                                                        .collect(Collectors.toSet());
      promotedDataSets.put(userDataSets.getKey(), mappedUserSets);
    }
    return promotedDataSets;
  }

  public Map<String, Set<DataSetWithRoles>> getDataSetsWithWriteAccess(String userId) {
    Map<String, Set<PromotedDataSet>> dataSets = storedDataSets.getData();
    Map<String, Set<PromotedDataSet>> promotedDataSets = new HashMap<>();
    Map<String, Set<DataSetWithRoles>> dataSetsWithWriteAccess = new HashMap<>();

    for (Map.Entry<String, Set<PromotedDataSet>> userDataSets : dataSets.entrySet()) {
      Set<DataSetWithRoles> dataSetWithRoles = new HashSet<>();

      userDataSets.getValue().forEach((dataSet) -> {
        List<String> roles;
        Optional<VreAuthorization> vre;
        try {
          vre = vreAuthorizationCrud
            .getAuthorization(
              userDataSets.getKey() + "_" + dataSet.getName(),
              userId);
          if (vre.isPresent()) {
            roles = vre
              .get().getRoles();
          } else {
            roles = Collections.emptyList();
          }
        } catch (AuthorizationUnavailableException e) {
          roles = Collections.emptyList();
        }
        DataSetWithRoles dataSetWithWriteAccess = new DataSetWithRoles(
          dataSet.getName(),
          dataSet.isPromoted(),
          roles, null
        );

        dataSetWithRoles.add(dataSetWithWriteAccess);
      });

      dataSetsWithWriteAccess.put(userDataSets.getKey(), dataSetWithRoles);
    }
    return dataSetsWithWriteAccess;
  }

  public Optional<String> getStatus(UUID uuid) {
    return statusMap.containsKey(uuid) ? Optional.of(statusMap.get(uuid).toString()) : Optional.empty();
  }

  public Tuple<UUID, PlainRdfCreator> registerRdfCreator(
    Function<Consumer<String>, PlainRdfCreator> rdfCreatorBuilder) {
    StringBuffer stringBuffer = new StringBuffer();
    UUID uuid = UUID.randomUUID();
    statusMap.put(uuid, stringBuffer);

    PlainRdfCreator rdfCreator = rdfCreatorBuilder.apply((str) -> {
      stringBuffer.setLength(0);
      stringBuffer.append(str);
    });

    return Tuple.tuple(uuid, rdfCreator);
  }

  public void removeDataSet(String ownerId, String dataSetName) throws IOException {
    dataStoreFactory.removeDataStoresFor(ownerId, dataSetName);
    // remove from datasets.json
    storedDataSets.updateData(dataSets -> {
      Set<PromotedDataSet>
        dataSetsToKeep = dataSets.get(ownerId).stream().filter(dataSet -> !dataSet.getName().equals(dataSetName))
                                 .collect(Collectors.toSet());
      dataSets.put(ownerId, dataSetsToKeep);
      return dataSets;
    });
    dataSetMap.get(ownerId).remove(dataSetName);

    try {
      resourceSync.removeDataSet(ownerId, dataSetName);
    } catch (ResourceSyncException e) {
      throw new IOException(e);
    }

    // remove folder
    FileUtils.deleteDirectory(fileHelper.dataSetPath(ownerId, dataSetName));
  }

  public void stop() {
    // TODO let data set close all its stores
    dataStoreFactory.stop();
  }

  public void start() throws IOException {
    dataStoreFactory.start();
    loadDataSetsFromJson();
  }

}
