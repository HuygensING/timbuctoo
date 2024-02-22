package nl.knaw.huygens.timbuctoo.dropwizard.tasks;

import io.dropwizard.servlets.tasks.Task;
import nl.knaw.huygens.timbuctoo.dataset.DataSetRepository;

import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

public class ReloadDataSet extends Task {
  private static final String DATA_SET_ID_PARAM = "dataSetId";
  private final DataSetRepository dataSetRepository;

  public ReloadDataSet(DataSetRepository dataSetRepository) {
    super("reloadDataSet");
    this.dataSetRepository = dataSetRepository;
  }

  @Override
  public void execute(Map<String, List<String>> immutableMultimap, PrintWriter printWriter) throws Exception {
    if (immutableMultimap.containsKey(DATA_SET_ID_PARAM)) {
      final String dataSetId = immutableMultimap.get(DATA_SET_ID_PARAM).getFirst();
      dataSetRepository.reloadDataSet(dataSetId);
    } else {
      printWriter.println(
          String.format("Make sure your request contains the param '%s'", DATA_SET_ID_PARAM)
      );
    }
  }
}
