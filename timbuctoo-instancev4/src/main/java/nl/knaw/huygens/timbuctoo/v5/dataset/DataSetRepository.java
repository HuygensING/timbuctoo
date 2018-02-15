package nl.knaw.huygens.timbuctoo.v5.dataset;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.google.common.collect.Maps;
import nl.knaw.huygens.timbuctoo.v5.berkeleydb.BdbEnvironmentCreator;
import nl.knaw.huygens.timbuctoo.v5.dataset.dto.BasicDataSetMetaData;
import nl.knaw.huygens.timbuctoo.v5.dataset.dto.DataSet;
import nl.knaw.huygens.timbuctoo.v5.dataset.dto.DataSetMetaData;
import nl.knaw.huygens.timbuctoo.v5.dataset.exceptions.DataSetPublishException;
import nl.knaw.huygens.timbuctoo.v5.dataset.exceptions.DataStoreCreationException;
import nl.knaw.huygens.timbuctoo.v5.dataset.exceptions.IllegalDataSetNameException;
import nl.knaw.huygens.timbuctoo.v5.dataset.exceptions.NotEnoughPermissionsException;
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

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
  private final FileHelper fileHelper;
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
      try (Stream<Path> fileStream = Files.walk(directories[i].toPath())) {
        Set<Path> paths = fileStream
          .filter(current -> Files.isDirectory(current)).collect(Collectors.toSet());
        for (Path path : paths) {
          File tempFile = new File(path.toString() + "/metaData.json");
          if (tempFile.exists()) {
            JsonFileBackedData<BasicDataSetMetaData> metaDataFromFile = null;
            metaDataFromFile = JsonFileBackedData.getOrCreate(
              tempFile,
              null,
              new TypeReference<BasicDataSetMetaData>() {
              });
            tempMetaDataSet.add(metaDataFromFile.getData());
          }
        }
      }

      metaDataSet.put(currentOwnerId, tempMetaDataSet);
    }

    fileHelper = new FileHelper(configuration.getDataSetMetadataLocation());

    this.rdfIdHelper = rdfIdHelper;
    this.rdfBaseUri = rdfIdHelper.instanceBaseUri();
    this.publicByDefault = publicByDefault;
    dataSetMap = new HashMap<>();
    this.onUpdated = onUpdated;
  }

  private void loadDataSetsFromJson() throws IOException {
    synchronized (dataSetMap) {
      for (Map.Entry<String, Set<DataSetMetaData>> entry : metaDataSet.entrySet()) {
        String ownerId = entry.getKey();
        Set<DataSetMetaData> ownerMetaDatas = entry.getValue();
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
                () -> onUpdated.accept(dataSetMetaData.getCombinedId())
              )
            );
          } catch (DataStoreCreationException e) {
            throw new IOException(e);
          }
        }
      }
    }
  }

  /**
   * Gets the dataSet designated by <code>ownerId</code> and <code>dataSetId</code> but only if the given
   * <code>user</code> has read-access to the dataSet.
   * @param user the user that wants read-access, may be <code>null</code>
   * @param ownerId ownerId
   * @param dataSetId dataSetId
   * @return the dataSet designated by <code>ownerId</code> and <code>dataSetId</code>
   */
  public Optional<DataSet> getDataSet(User user, String ownerId, String dataSetId) {
    synchronized (dataSetMap) {
      if (dataSetMap.containsKey(ownerId) && dataSetMap.get(ownerId).containsKey(dataSetId)) {
        try {
          if (permissionFetcher.getPermissions(user, dataSetMap.get(ownerId).get(dataSetId).getMetadata()
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

  /**
   * Gets the description of the dataSet designated by <code>ownerId</code> and <code>dataSetId</code>
   * but only if the given <code>user</code> has read-access to the dataSet.
   * @param user the user that wants read-access, may be <code>null</code>
   * @param ownerId ownerId
   * @param dataSetId dataSetId
   * @return the description of the dataSet designated by <code>ownerId</code> and <code>dataSetId</code>
   */
  public Optional<File> getDataSetDescription(User user, String ownerId, String dataSetId) {
    synchronized (dataSetMap) {
      if (dataSetMap.containsKey(ownerId) && dataSetMap.get(ownerId).containsKey(dataSetId)) {
        try {
          if (permissionFetcher.getPermissions(user, dataSetMap.get(ownerId).get(dataSetId).getMetadata()
          ).contains(Permission.READ)) {
            File file = fileHelper.fileInDataSet(ownerId, dataSetId, "description.xml");
            return Optional.of(file);
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
      throw new DataStoreCreationException(e);
    }


    synchronized (dataSetMap) {
      Map<String, DataSet> userDataSets = dataSetMap.computeIfAbsent(ownerPrefix, key -> new HashMap<>());

      if (!userDataSets.containsKey(dataSetId)) {
        try {
          permissionFetcher.initializeOwnerAuthorization(user, dataSet.getOwnerId(), dataSet.getDataSetId());
          userDataSets.put(
            dataSetId,
            dataSet(
              dataSet,
              configuration,
              fileHelper,
              executorService,
              rdfBaseUri,
              dataStoreFactory,
              () -> onUpdated.accept(dataSet.getCombinedId())
            )
          );
        } catch (PermissionFetchingException | AuthorizationCreationException | IOException e) {
          throw new DataStoreCreationException(e);
        }
      }
      return userDataSets.get(dataSetId);
    }
  }

  boolean dataSetExists(String ownerId, String dataSet) {
    return unsafeGetDataSetWithoutCheckingPermissions(ownerId, dataSet).isPresent();
  }

  public void publishDataSet(User user, String ownerId, String dataSetName)
    throws DataSetPublishException {
    Optional<DataSet> dataSet = getDataSet(user,
      ownerId, dataSetName);
    try {
      if (dataSet.isPresent() &&
        permissionFetcher.getPermissions(user,dataSet.get().getMetadata()).contains(Permission.ADMIN)) {
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

  public Collection<DataSet> getDataSetsWithWriteAccess(User user) {
    List<DataSet> dataSetsWithWriteAccess = new ArrayList<>();

    for (Map<String, DataSet> userDataSets : dataSetMap.values()) {
      for (DataSet dataSet : userDataSets.values()) {
        try {
          boolean isAllowedToWrite = permissionFetcher.getPermissions(user, dataSet.getMetadata())
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

  /**
   * Gets all published dataSets and all dataSets the given <code>user</code> has read-access to.
   * @param user the user that wants read-access, may be <code>null</code>
   * @return all published dataSets + all dataSets the given <code>user</code> has read-access to
   */
  public Collection<DataSet> getDataSetsWithReadAccess(@Nullable User user) {
    List<DataSet> dataSetsWithReadAccess = new ArrayList<>();
    for (Map<String, DataSet> userDataSets : dataSetMap.values()) {
      for (DataSet dataSet : userDataSets.values()) {
        try {
          if (permissionFetcher.getPermissions(user, dataSet.getMetadata()).contains(Permission.READ)) {
            dataSetsWithReadAccess.add(dataSet);
          }
        } catch (PermissionFetchingException e) {
          LOG.error("Could not fetch read permission", e);
        }
      }
    }
    return dataSetsWithReadAccess;
  }

  public void removeDataSet(String ownerId, String dataSetName, User user)
    throws IOException, NotEnoughPermissionsException {
    try {
      DataSet dataSet = dataSetMap.get(ownerId).get(dataSetName);
      String combinedId = dataSet.getMetadata().getCombinedId();
      if (!permissionFetcher.getPermissions(user, dataSet.getMetadata()).contains(Permission.ADMIN)) {
        throw new NotEnoughPermissionsException(
          String.format(
            "User '%s' is not allowed to remove dataset '%s'",
            user.getDisplayName(),
            combinedId
          )
        );
      }
      dataSet.stop();
      dataSetMap.get(ownerId).remove(dataSetName);
      permissionFetcher.removeAuthorizations(combinedId);
    } catch (PermissionFetchingException e) {
      throw new IOException(e);
    }

    // remove folder
    deleteDirectoryAndRetryOnFailure(fileHelper.dataSetPath(ownerId, dataSetName), 5);
  }

  private void deleteDirectoryAndRetryOnFailure(File path, int retryCount) {
    try {
      FileUtils.deleteDirectory(path);
    } catch (IOException e) {
      if (retryCount > 0) {
        LOG.warn("Deleting directory {}, failed, {} tries remaining.", path.getAbsolutePath(), retryCount);
        try {
          Thread.sleep(500);
          deleteDirectoryAndRetryOnFailure(path, retryCount - 1);
        } catch (InterruptedException e1) {
          //ignore and stop trying
        }
      } else {
        LOG.error("Deleting directory {}, failed! Manual cleanup necessary", path.getAbsolutePath());
      }
    }
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
