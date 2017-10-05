package nl.knaw.huygens.timbuctoo.v5.dataset;

import com.fasterxml.jackson.core.type.TypeReference;
import nl.knaw.huygens.timbuctoo.security.VreAuthorizationCrud;
import nl.knaw.huygens.timbuctoo.security.dto.VreAuthorization;
import nl.knaw.huygens.timbuctoo.security.exceptions.AuthorizationCreationException;
import nl.knaw.huygens.timbuctoo.security.exceptions.AuthorizationUnavailableException;
import nl.knaw.huygens.timbuctoo.util.Tuple;
import nl.knaw.huygens.timbuctoo.v5.dataset.dto.DataSet;
import nl.knaw.huygens.timbuctoo.v5.dataset.dto.PromotedDataSet;
import nl.knaw.huygens.timbuctoo.v5.dataset.exceptions.DataStoreCreationException;
import nl.knaw.huygens.timbuctoo.v5.datastores.resourcesync.ResourceSync;
import nl.knaw.huygens.timbuctoo.v5.datastores.resourcesync.ResourceSyncException;
import nl.knaw.huygens.timbuctoo.v5.filestorage.implementations.filesystem.FileHelper;
import nl.knaw.huygens.timbuctoo.v5.jsonfilebackeddata.JsonFileBackedData;
import nl.knaw.huygens.timbuctoo.v5.util.TimbuctooRdfIdHelper;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
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

import static nl.knaw.huygens.timbuctoo.v5.dataset.dto.DataSet.dataSet;
import static nl.knaw.huygens.timbuctoo.v5.dataset.dto.PromotedDataSet.promotedDataSet;

/**
 * - stores all configuration parameters so it can inject them in the dataset constructor
 * - makes CreateDataSet a singleton
 * - keeps track of all created dataSets across restarts (stores them in a file)
 */
public class DataSetRepository {

  private static final Logger LOG = LoggerFactory.getLogger(DataSetRepository.class);

  private final ExecutorService executorService;
  private final VreAuthorizationCrud vreAuthorizationCrud;
  private final DataSetConfiguration configuration;
  private final DataStoreFactory dataStoreFactory;
  private final Map<String, Map<String, DataSet>> dataSetMap;
  private final JsonFileBackedData<Map<String, Set<PromotedDataSet>>> storedDataSets;
  private final TimbuctooRdfIdHelper rdfIdHelper;
  private final HashMap<UUID, StringBuffer> statusMap;
  private final FileHelper fileHelper;
  private final ResourceSync resourceSync;
  private Consumer<String> onUpdated;


  public DataSetRepository(ExecutorService executorService, VreAuthorizationCrud vreAuthorizationCrud,
                           DataSetConfiguration configuration, DataStoreFactory dataStoreFactory,
                           TimbuctooRdfIdHelper rdfIdHelper, Consumer<String> onUpdated)
    throws IOException {
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
    this.rdfIdHelper = rdfIdHelper;
    statusMap = new HashMap<>();
    resourceSync = configuration.getResourceSync();

    dataSetMap = new HashMap<>();
    this.onUpdated = onUpdated;
  }

  private void loadDataSetsFromJson() throws IOException {
    synchronized (dataSetMap) {
      for (Map.Entry<String, Set<PromotedDataSet>> entry : storedDataSets.getData().entrySet()) {
        String ownerId = entry.getKey();
        HashMap<String, DataSet> ownersSets = new HashMap<>();
        dataSetMap.put(ownerId, ownersSets);
        for (PromotedDataSet promotedDataSet : entry.getValue()) {
          String dataSetName = promotedDataSet.getDataSetId();
          try {
            ownersSets.put(
              dataSetName,
              dataSet(
                promotedDataSet,
                configuration,
                fileHelper,
                executorService,
                dataStoreFactory,
                resourceSync,
                () -> onUpdated.accept(promotedDataSet.getCombinedId())
              )
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

  public Optional<DataSet> getDataSet(String combinedId) {
    final Tuple<String, String> splitId = PromotedDataSet.splitCombinedId(combinedId);
    return Optional.ofNullable(dataSetMap.get(splitId.getLeft()))
      .map(userDataSets -> userDataSets.get(splitId.getRight()));
  }

  public DataSet createDataSet(String ownerId, String dataSetId) throws DataStoreCreationException {
    final PromotedDataSet dataSet = promotedDataSet(ownerId, dataSetId, rdfIdHelper.dataSet(ownerId, dataSetId), false);
    synchronized (dataSetMap) {
      Map<String, DataSet> userDataSets = dataSetMap.computeIfAbsent(ownerId, key -> new HashMap<>());

      if (!userDataSets.containsKey(dataSetId)) {
        try {
          vreAuthorizationCrud.createAuthorization(dataSet.getCombinedId(), ownerId, "ADMIN");
          userDataSets.put(
            dataSetId,
            dataSet(
              dataSet,
              configuration,
              fileHelper,
              executorService,
              dataStoreFactory,
              resourceSync,
              () -> onUpdated.accept(dataSet.getCombinedId())
            )
          );
          storedDataSets.updateData(dataSets -> {
            dataSets
              .computeIfAbsent(ownerId, key -> new HashSet<>())
              .add(dataSet);
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

  public Collection<DataSet> getDataSets() {
    return dataSetMap.values().stream().flatMap(x -> x.values().stream()).collect(Collectors.toList());
  }

  public Collection<DataSet> getPromotedDataSets() {
    return dataSetMap.values().stream().flatMap(x -> x.values().stream())
      .filter(x -> x.getMetadata().isPromoted())
      .collect(Collectors.toList());
  }

  public Collection<DataSet> getDataSetsWithWriteAccess(String userId) {
    List<DataSet> dataSetsWithWriteAccess = new ArrayList<>();

    for (Map<String, DataSet> userDataSets : dataSetMap.values()) {
      for (DataSet dataSet : userDataSets.values()) {
        try {
          boolean isAllowedToWrite = vreAuthorizationCrud
            .getAuthorization(dataSet.getMetadata().getCombinedId(), userId)
            .map(VreAuthorization::isAllowedToWrite)
            .orElse(false);
          if (isAllowedToWrite) {
            dataSetsWithWriteAccess.add(dataSet);
          }
        } catch (AuthorizationUnavailableException e) {
          LOG.error("Could not fetch authorization", e);
        }
      }
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
        dataSetsToKeep = dataSets.get(ownerId).stream().filter(dataSet -> !dataSet.getDataSetId().equals(dataSetName))
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
