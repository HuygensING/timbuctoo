package nl.knaw.huygens.timbuctoo.v5.logprocessing;

import com.google.common.collect.Maps;

import java.io.File;
import java.util.Map;

public class FileSystemBasedDataSetManager implements DataSetManager {

  private File dataDir;
  private Map<String, DataSet> dataSetMap;

  public FileSystemBasedDataSetManager(File dataDir) {
    this.dataDir = dataDir;
    this.dataSetMap = Maps.newHashMap();
    dataDir.mkdirs();
  }

  @Override
  public DataSet getsert(String userPersistentId, String dataSetId) {
    File userDir = new File(dataDir, userPersistentId);
    File dataSetDir = new File(userDir, userPersistentId);

    DataSet dataSet = dataSetMap.computeIfAbsent(userPersistentId + dataSetId, (key) -> new DataSet());

    userDir.mkdir();
    dataSetDir.mkdir();
    return dataSet;
  }
}
