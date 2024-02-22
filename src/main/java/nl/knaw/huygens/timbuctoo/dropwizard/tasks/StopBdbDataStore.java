package nl.knaw.huygens.timbuctoo.dropwizard.tasks;

import io.dropwizard.servlets.tasks.Task;
import nl.knaw.huygens.timbuctoo.util.Tuple;
import nl.knaw.huygens.timbuctoo.berkeleydb.BdbEnvironmentCreator;
import nl.knaw.huygens.timbuctoo.dataset.dto.DataSetMetaData;

import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

public class StopBdbDataStore extends Task {
  private static final String DATA_SET_ID_PARAM = "dataSetId";
  private static final String DATA_STORE_PARAM = "dataStore";
  private final BdbEnvironmentCreator environmentCreator;

  public StopBdbDataStore(BdbEnvironmentCreator environmentCreator) {
    super("stopDataStore");
    this.environmentCreator = environmentCreator;
  }

  @Override
  public void execute(Map<String, List<String>> immutableMultimap, PrintWriter printWriter) throws Exception {
    if (immutableMultimap.containsKey(DATA_SET_ID_PARAM) && immutableMultimap.containsKey(DATA_STORE_PARAM)) {
      final String dataSetId = immutableMultimap.get(DATA_SET_ID_PARAM).getFirst();
      final String dataStore = immutableMultimap.get(DATA_STORE_PARAM).getFirst();

      final Tuple<String, String> ownerDataSet = DataSetMetaData.splitCombinedId(dataSetId);

      environmentCreator.closeDatabase(ownerDataSet.left(), ownerDataSet.right(), dataStore);
    } else {
      printWriter.println(
          String.format("Make sure your request contains the params '%s' and '%s'", DATA_SET_ID_PARAM, DATA_STORE_PARAM)
      );
    }
  }
}
