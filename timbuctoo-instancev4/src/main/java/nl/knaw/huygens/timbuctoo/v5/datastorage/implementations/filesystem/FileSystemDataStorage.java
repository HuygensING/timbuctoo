package nl.knaw.huygens.timbuctoo.v5.datastorage.implementations.filesystem;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.Maps;
import nl.knaw.huygens.timbuctoo.v5.dataset.DataSetConfiguration;
import nl.knaw.huygens.timbuctoo.v5.dataset.dto.BasicDataSetMetaData;
import nl.knaw.huygens.timbuctoo.v5.dataset.dto.DataSetMetaData;
import nl.knaw.huygens.timbuctoo.v5.datastorage.DataSetStorage;
import nl.knaw.huygens.timbuctoo.v5.datastorage.DataStorage;
import nl.knaw.huygens.timbuctoo.v5.filehelper.FileHelper;
import nl.knaw.huygens.timbuctoo.v5.jsonfilebackeddata.JsonFileBackedData;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FileSystemDataStorage implements DataStorage {

  private static final Logger LOG = LoggerFactory.getLogger(FileSystemDataStorage.class);
  private final DataSetConfiguration configuration;
  private final FileHelper fileHelper;

  public FileSystemDataStorage(DataSetConfiguration configuration) {
    this.configuration = configuration;
    fileHelper = new FileHelper(configuration.getDataSetMetadataLocation());
  }

  @Override
  public DataSetStorage getDataSetStorage(String ownerId, String dataSetName) {
    return new FileSystemDataSetStorage(ownerId, dataSetName, fileHelper);
  }

  @Override
  public Map<String, Set<DataSetMetaData>> loadDataSetMetaData() throws IOException {
    Map<String, Set<DataSetMetaData>> metaDataSet = Maps.newHashMap();

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
    return metaDataSet;
  }

  private void deleteDataSetData(File path, int retryCount) {
    try {
      FileUtils.deleteDirectory(path);
    } catch (IOException e) {
      if (retryCount > 0) {
        LOG.warn("Deleting directory {}, failed, {} tries remaining.", path.getAbsolutePath(), retryCount);
        try {
          Thread.sleep(500);
          deleteDataSetData(path, retryCount - 1);
        } catch (InterruptedException e1) {
          //ignore and stop trying
        }
      } else {
        LOG.error("Deleting directory {}, failed! Manual cleanup necessary", path.getAbsolutePath());
      }
    }
  }

  @Override
  public void deleteDataSetData(String ownerId, String dataSetName, int retryCount) {
    this.deleteDataSetData(fileHelper.dataSetPath(ownerId, dataSetName), retryCount);
  }
}
