package nl.knaw.huygens.timbuctoo.server.tasks;

import io.dropwizard.servlets.tasks.Task;
import nl.knaw.huygens.timbuctoo.dataset.DataSetRepository;
import nl.knaw.huygens.timbuctoo.dataset.ImportStatus;
import nl.knaw.huygens.timbuctoo.dataset.dto.DataSet;
import nl.knaw.huygens.timbuctoo.datastores.implementations.bdb.BdbQuadStore;
import nl.knaw.huygens.timbuctoo.datastores.implementations.bdb.BdbSchemaStore;
import nl.knaw.huygens.timbuctoo.datastores.implementations.bdb.BdbTypeNameStore;

import java.io.PrintWriter;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Future;

public class ReimportDatasetsTask extends Task {
  private final DataSetRepository dataSetRepository;

  public ReimportDatasetsTask(DataSetRepository dataSetRepository) {
    super("reimportDatasets");
    this.dataSetRepository = dataSetRepository;
  }

  @Override
  public void execute(Map<String, List<String>> parameters, PrintWriter output) throws Exception {
    if (parameters.isEmpty()) {
      for (DataSet dataSet : dataSetRepository.getDataSets()) {
        reimportDataset(dataSet, output);
      }
    } else {
      if (parameters.containsKey("userId") && parameters.containsKey("dataSetId")) {
        Optional<DataSet> dataSet = dataSetRepository.unsafeGetDataSetWithoutCheckingPermissions(
            parameters.get("userId").get(0), parameters.get("dataSetId").get(0));
        if (dataSet.isPresent()) {
          reimportDataset(dataSet.get(), output);
        }
      }
    }
  }

  private void reimportDataset(DataSet dataSet, PrintWriter output) throws Exception {
    output.println("Reimport dataset: " + dataSet.getMetadata().getCombinedId());
    output.println("Start with emptying stores");
    output.flush();

    ((BdbQuadStore) dataSet.getQuadStore()).empty();
    dataSet.getGraphStore().empty();
    dataSet.getDefaultResourcesStore().empty();
    ((BdbTypeNameStore) dataSet.getTypeNameStore()).empty();
    ((BdbSchemaStore) dataSet.getSchemaStore()).empty();
    dataSet.getUpdatedPerPatchStore().empty();
    dataSet.getOldSubjectTypesStore().empty();
    dataSet.getRmlDataSourceStore().empty();

    output.println("Stores emptied; now start importing");
    output.flush();

    Future<ImportStatus> importStatusFuture = dataSet.getImportManager().reprocessLogs();
    importStatusFuture.get();

    output.println("Finished reimport of dataset: " + dataSet.getMetadata().getCombinedId());
    output.flush();
  }
}
