package nl.knaw.huygens.timbuctoo.server.tasks;

import com.google.common.collect.ImmutableMultimap;
import com.sleepycat.je.DatabaseNotFoundException;
import io.dropwizard.servlets.tasks.Task;
import nl.knaw.huygens.timbuctoo.v5.berkeleydb.BdbEnvironmentCreator;
import nl.knaw.huygens.timbuctoo.v5.dataset.DataSetRepository;
import nl.knaw.huygens.timbuctoo.v5.dataset.dto.DataSet;

import java.io.PrintWriter;
import java.util.Set;

public class RemoveOldStores extends Task {
  private final BdbEnvironmentCreator dataStoreFactory;
  private final DataSetRepository dataSetRepository;

  public RemoveOldStores(BdbEnvironmentCreator dataStoreFactory, DataSetRepository dataSetRepository) {
    super("removeOldStores");
    this.dataStoreFactory = dataStoreFactory;
    this.dataSetRepository = dataSetRepository;
  }

  @Override
  public void execute(ImmutableMultimap<String, String> parameters, PrintWriter output) throws Exception {
    for (DataSet dataSet : dataSetRepository.getDataSets()) {
      output.println("Remove old stores from dataset: " + dataSet.getMetadata().getCombinedId());
      output.flush();

      Set.of("truePatch", "updatedPerPatchOld", "versions").forEach(databaseName -> {
        try {
          dataStoreFactory.removeDatabase(
              dataSet.getMetadata().getOwnerId(),
              dataSet.getMetadata().getDataSetId(),
              databaseName);
        } catch (DatabaseNotFoundException e) {
          e.printStackTrace();
        }
      });

      output.println("Removed old stores from dataset: " + dataSet.getMetadata().getCombinedId());
      output.flush();
    }
  }
}
