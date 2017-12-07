package nl.knaw.huygens.timbuctoo.v5.dataset;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.google.common.collect.Maps;
import nl.knaw.huygens.timbuctoo.util.Tuple;
import nl.knaw.huygens.timbuctoo.v5.berkeleydb.BdbEnvironmentCreator;
import nl.knaw.huygens.timbuctoo.v5.dataset.dto.BasicDataSetMetaData;
import nl.knaw.huygens.timbuctoo.v5.dataset.dto.DataSet;
import nl.knaw.huygens.timbuctoo.v5.dataset.dto.DataSetMetaData;
import nl.knaw.huygens.timbuctoo.v5.dataset.exceptions.DataSetPublishException;
import nl.knaw.huygens.timbuctoo.v5.dataset.exceptions.DataStoreCreationException;
import nl.knaw.huygens.timbuctoo.v5.dataset.exceptions.IllegalDataSetNameException;
import nl.knaw.huygens.timbuctoo.v5.datastores.resourcesync.ResourceSync;
import nl.knaw.huygens.timbuctoo.v5.datastores.resourcesync.ResourceSyncException;
import nl.knaw.huygens.timbuctoo.v5.filehelper.FileHelper;
import nl.knaw.huygens.timbuctoo.v5.jacksonserializers.TimbuctooCustomSerializers;
import nl.knaw.huygens.timbuctoo.v5.jsonfilebackeddata.JsonFileBackedData;
import nl.knaw.huygens.timbuctoo.v5.security.PermissionFetcher;
import nl.knaw.huygens.timbuctoo.v5.security.dto.Permission;
import nl.knaw.huygens.timbuctoo.v5.security.dto.User;
import nl.knaw.huygens.timbuctoo.v5.security.exceptions.AuthorizationCreationException;
import nl.knaw.huygens.timbuctoo.v5.security.exceptions.PermissionFetchingException;
import nl.knaw.huygens.timbuctoo.v5.util.TimbuctooRdfIdHelper;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
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

/**
 * - stores all configuration parameters so it can inject them in the dataset constructor
 * - makes CreateDataSet a singleton
 * - keeps track of all created dataSets across restarts (stores them in a file)
 */
public class DataSetRepository {

  private static final Logger LOG = LoggerFactory.getLogger(DataSetRepository.class);

  private final ExecutorService executorService;
  private final PermissionFetcher permissionFetcher;
  private final DataSetConfiguration configuration;
  private final BdbEnvironmentCreator dataStoreFactory;
  private final Map<String, Map<String, DataSet>> dataSetMap;
  private final Map<String, Set<DataSetMetaData>> metaDataSet;
  private final TimbuctooRdfIdHelper rdfIdHelper;
  private final String rdfBaseUri;
  private final boolean publicByDefault;
  private final HashMap<UUID, StringBuffer> statusMap;
  private final FileHelper fileHelper;
  private final ResourceSync resourceSync;
  private Consumer<String> onUpdated;


  public DataSetRepository(ExecutorService executorService, PermissionFetcher permissionFetcher,
                           DataSetConfiguration configuration, BdbEnvironmentCreator dataStoreFactory,
                           TimbuctooRdfIdHelper rdfIdHelper, Consumer<String> onUpdated,
                           boolean publicByDefault) throws IOException {
    this.executorService = executorService;
    this.permissionFetcher = permissionFetcher;
    this.configuration = configuration;
    this.dataStoreFactory = dataStoreFactory;

    metaDataSet = Maps.newHashMap();

    File[] directories = new File(configuration.getDataSetMetadataLocation()).listFiles(File::isDirectory);

    for (int i = 0; i < directories.length; i++) {
      String dirName = directories[i].toString();
      String currentOwnerId = dirName.substring(dirName.lastIndexOf("/") + 1, dirName.length());
      Set<DataSetMetaData> tempMetaDataSet = new HashSet<>();
      Files.walk(directories[i].toPath())
        .filter(current -> Files.isDirectory(current))
        .forEach(
          path -> {
            File tempFile = new File(path.toString() + "/metaData.json");
            if (tempFile.exists()) {
              JsonFileBackedData<BasicDataSetMetaData> metaDataFromFile = null;
              try {
                metaDataFromFile = JsonFileBackedData.getOrCreate(
                  tempFile,
                  null,
                  new TypeReference<BasicDataSetMetaData>() {
                  });
                tempMetaDataSet.add(metaDataFromFile.getData());
              } catch (IOException e) {
                e.printStackTrace();
              }
            }
          }
        );
      metaDataSet.put(currentOwnerId, tempMetaDataSet);
    }

    fileHelper = new FileHelper(configuration.getDataSetMetadataLocation());

    this.rdfIdHelper = rdfIdHelper;
    this.rdfBaseUri = rdfIdHelper.instanceBaseUri();
    this.publicByDefault = publicByDefault;
    statusMap = new HashMap<>();
    resourceSync = configuration.getResourceSync();

    dataSetMap = new HashMap<>();
    this.onUpdated = onUpdated;
  }

  private void loadDataSetsFromJson() throws IOException {
    synchronized (dataSetMap) {
      metaDataSet.forEach((ownerId, ownerMetaDatas) -> {
        HashMap<String, DataSet> ownersSets = new HashMap<>();
        dataSetMap.put(ownerId, ownersSets);
        for (DataSetMetaData dataSetMetaData : ownerMetaDatas) {
          String dataSetName = dataSetMetaData.getDataSetId();
          try {
            ownersSets.put(
              dataSetName,
              dataSet(
                dataSetMetaData,
                configuration,
                fileHelper,
                executorService,
                rdfBaseUri,
                dataStoreFactory,
                resourceSync,
                () -> onUpdated.accept(dataSetMetaData.getCombinedId())
              )
            );
          } catch (IOException | DataStoreCreationException | ResourceSyncException e) {
            e.printStackTrace();
          }
        }
      });
    }
  }

  public Optional<DataSet> getDataSet(String userId, String ownerId, String dataSetId) {
    synchronized (dataSetMap) {
      if (dataSetMap.containsKey(ownerId) && dataSetMap.get(ownerId).containsKey(dataSetId)) {
        try {
          if (permissionFetcher.getPermissions(userId, dataSetMap.get(ownerId).get(dataSetId).getMetadata()
          ).contains(Permission.READ)) {
            return Optional.ofNullable(dataSetMap.get(ownerId).get(dataSetId));
          }
        } catch (PermissionFetchingException e) {
          return Optional.empty();
        }
      }
      return Optional.empty();
    }
  }

  public Optional<DataSet> unsafeGetDataSetWithoutCheckingPermissions(String ownerId, String dataSetId) {
    synchronized (dataSetMap) {
      if (dataSetMap.containsKey(ownerId) && dataSetMap.get(ownerId).containsKey(dataSetId)) {
        return Optional.ofNullable(dataSetMap.get(ownerId).get(dataSetId));
      } else {
        return Optional.empty();
      }
    }
  }

  public boolean userMatchesPrefix(User user, String prefix) {
    return user != null && user.getPersistentId() != null && ("u" + user.getPersistentId()).equals(prefix);
  }

  public DataSet createDataSet(User user, String dataSetId) throws DataStoreCreationException,
    IllegalDataSetNameException {
    //The ownerId might not be valid (i.e. a safe string). We make it safe here:
    //dataSetId is under the control of the user so we simply throw if it's not valid
    String ownerPrefix = "u" + user.getPersistentId();
    final String baseUri = rdfIdHelper.dataSetBaseUri(ownerPrefix, dataSetId);
    String uriPrefix;
    if (!baseUri.endsWith("/") && !baseUri.endsWith("#") && !baseUri.endsWith("?")) {
      //it might have some parts

      //?foo
      //?foo=bar
      //?boo&foo
      //?boo&foo=bar
      //#foo
      //#foo=bar
      //#boo&foo
      //#boo&foo=bar
      if (baseUri.contains("#") || baseUri.contains("?")) {
        if (baseUri.endsWith("&")) {
          uriPrefix = baseUri;
        } else {
          uriPrefix = baseUri + "&";
        }
      } else {
        uriPrefix = baseUri + "/";
      }
    } else {
      uriPrefix = baseUri;
    }

    final DataSetMetaData dataSet = new BasicDataSetMetaData(
      ownerPrefix,
      dataSetId,
      baseUri,
      uriPrefix,
      false,
      publicByDefault
    );

    ObjectMapper objectMapper = new ObjectMapper()
      .registerModule(new Jdk8Module())
      .registerModule(new GuavaModule())
      .registerModule(new TimbuctooCustomSerializers())
      .enable(SerializationFeature.INDENT_OUTPUT);

    File metaDataFile = fileHelper.fileInDataSet(ownerPrefix, dataSetId, "metaData.json");


    try {
      objectMapper.writeValue(metaDataFile, dataSet);
    } catch (IOException e) {
      e.printStackTrace();
    }


    synchronized (dataSetMap) {
      Map<String, DataSet> userDataSets = dataSetMap.computeIfAbsent(ownerPrefix, key -> new HashMap<>());

      if (!userDataSets.containsKey(dataSetId)) {
        try {
          permissionFetcher.initializeOwnerAuthorization(user.getPersistentId(),
            dataSet.getOwnerId(), dataSet.getDataSetId());
          userDataSets.put(
            dataSetId,
            dataSet(
              dataSet,
              configuration,
              fileHelper,
              executorService,
              rdfBaseUri,
              dataStoreFactory,
              resourceSync,
              () -> onUpdated.accept(dataSet.getCombinedId())
            )
          );
        } catch (
          PermissionFetchingException | AuthorizationCreationException | IOException | ResourceSyncException e1) {
          throw new DataStoreCreationException(e1);
        }
      }
      return userDataSets.get(dataSetId);
    }
  }

  boolean dataSetExists(String ownerId, String dataSet) {
    return unsafeGetDataSetWithoutCheckingPermissions(ownerId, dataSet).isPresent();
  }

  public void publishDataSet(String userId,String ownerId, String dataSetName)
    throws DataSetPublishException {
    Optional<DataSet> dataSet = getDataSet(userId,
      ownerId, dataSetName);
    try {
      if (dataSet.isPresent() &&
        permissionFetcher.getPermissions(userId,dataSet.get().getMetadata()).contains(Permission.ADMIN)) {
        DataSetMetaData dataSetMetaData = dataSet.get().getMetadata();

        dataSetMetaData.publish();

        ObjectMapper objectMapper = new ObjectMapper()
          .registerModule(new Jdk8Module())
          .registerModule(new GuavaModule())
          .registerModule(new TimbuctooCustomSerializers())
          .enable(SerializationFeature.INDENT_OUTPUT);

        File metaDataFile = fileHelper.fileInDataSet(ownerId, dataSetName, "metaData.json");

        try {
          objectMapper.writeValue(metaDataFile, dataSetMetaData);
        } catch (IOException e) {
          throw new DataSetPublishException(e);
        }
      }
    } catch (PermissionFetchingException e) {
      throw new DataSetPublishException(e);
    }
  }

  public Collection<DataSet> getDataSets() {
    return dataSetMap.values().stream().flatMap(x -> x.values().stream())
      .collect(Collectors.toList());
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
          boolean isAllowedToWrite = permissionFetcher.getOldPermissions(userId, dataSet.getMetadata().getCombinedId())
            .contains(Permission.WRITE);
          if (isAllowedToWrite) {
            dataSetsWithWriteAccess.add(dataSet);
          }
        } catch (PermissionFetchingException e) {
          LOG.error("Could not fetch write permission", e);
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

  public void removeDataSet(String combinedId) throws IOException {
    Tuple<String, String> ownerIdDataSetName = DataSetMetaData.splitCombinedId(combinedId);

    this.removeDataSet(ownerIdDataSetName.getLeft(), ownerIdDataSetName.getRight());
  }

  public void removeDataSet(String ownerId, String dataSetName) throws IOException {
    dataStoreFactory.removeDatabasesFor(ownerId, dataSetName);

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
    for (DataSet dataSet : getDataSets()) {
      dataSet.stop();
    }

    dataStoreFactory.stop();
  }

  public void start() throws IOException {
    dataStoreFactory.start();
    loadDataSetsFromJson();
  }

}
