package nl.knaw.huygens.timbuctoo.server.tasks;

import io.dropwizard.servlets.tasks.Task;
import nl.knaw.huygens.timbuctoo.dataset.DataSetRepository;
import nl.knaw.huygens.timbuctoo.dataset.dto.DataSet;
import nl.knaw.huygens.timbuctoo.datastores.implementations.bdb.BdbQuadStore;
import nl.knaw.huygens.timbuctoo.datastores.implementations.bdb.BdbSchemaStore;

import java.io.PrintWriter;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class RebuildSchemaTask extends Task {
  private final DataSetRepository dataSetRepository;

  public RebuildSchemaTask(DataSetRepository dataSetRepository) {
    super("rebuildSchema");
    this.dataSetRepository = dataSetRepository;
  }

  @Override
  public void execute(Map<String, List<String>> parameters, PrintWriter output) throws Exception {
    if (parameters.isEmpty()) {
      for (DataSet dataSet : dataSetRepository.getDataSets()) {
        rebuildSchemaFor(dataSet, output);
      }
    } else {
      if (parameters.containsKey("userId") && parameters.containsKey("dataSetId")) {
        Optional<DataSet> dataSet = dataSetRepository.unsafeGetDataSetWithoutCheckingPermissions(
            parameters.get("userId").get(0), parameters.get("dataSetId").get(0));
        dataSet.ifPresent(set -> rebuildSchemaFor(set, output));
      }
    }
  }

  private void rebuildSchemaFor(DataSet dataSet, PrintWriter output) {
    output.println("Rebuilding schema for dataset: " + dataSet.getMetadata().getCombinedId());
    output.flush();

    BdbSchemaStore schemaStore = (BdbSchemaStore) dataSet.getSchemaStore();
    schemaStore.rebuildSchema((BdbQuadStore) dataSet.getQuadStore());

    output.println("Finished rebuilding schema of dataset: " + dataSet.getMetadata().getCombinedId());
    output.flush();
  }
}
