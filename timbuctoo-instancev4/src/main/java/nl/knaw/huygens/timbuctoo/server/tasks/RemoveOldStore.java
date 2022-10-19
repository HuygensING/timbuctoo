package nl.knaw.huygens.timbuctoo.server.tasks;

import com.sleepycat.je.DatabaseNotFoundException;
import io.dropwizard.servlets.tasks.Task;
import nl.knaw.huygens.timbuctoo.v5.berkeleydb.BdbEnvironmentCreator;
import nl.knaw.huygens.timbuctoo.v5.dataset.DataSetRepository;
import nl.knaw.huygens.timbuctoo.v5.dataset.dto.DataSet;

import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

public class RemoveOldStore extends Task {
  private final BdbEnvironmentCreator dataStoreFactory;
  private final DataSetRepository dataSetRepository;

  public RemoveOldStore(BdbEnvironmentCreator dataStoreFactory, DataSetRepository dataSetRepository) {
    super("removeOldStore");
    this.dataStoreFactory = dataStoreFactory;
    this.dataSetRepository = dataSetRepository;
  }

  @Override
  public void execute(Map<String, List<String>> parameters, PrintWriter output) throws Exception {
    for (DataSet dataSet : dataSetRepository.getDataSets()) {
      output.println("Remove old store from dataset: " + dataSet.getMetadata().getCombinedId());
      output.flush();

      try {
        dataStoreFactory.removeDatabase(
            dataSet.getMetadata().getOwnerId(),
            dataSet.getMetadata().getDataSetId(),
            "rdfDataOld"
        );
      } catch (DatabaseNotFoundException e) {
        e.printStackTrace(output);
      }

      output.println("Removed old store from dataset: " + dataSet.getMetadata().getCombinedId());
      output.flush();
    }
  }
}
