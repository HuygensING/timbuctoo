package nl.knaw.huygens.timbuctoo.server.tasks;

import io.dropwizard.servlets.tasks.Task;
import nl.knaw.huygens.timbuctoo.v5.dataset.DataSetRepository;
import nl.knaw.huygens.timbuctoo.v5.dataset.ImportStatus;
import nl.knaw.huygens.timbuctoo.v5.dataset.dto.DataSet;
import nl.knaw.huygens.timbuctoo.v5.datastores.implementations.bdb.BdbQuadStore;
import nl.knaw.huygens.timbuctoo.v5.datastores.implementations.bdb.BdbSchemaStore;
import nl.knaw.huygens.timbuctoo.v5.datastores.implementations.bdb.BdbTypeNameStore;

import java.io.PrintWriter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

public class ReimportAllDatasetsTask extends Task {
  private final DataSetRepository dataSetRepository;

  public ReimportAllDatasetsTask(DataSetRepository dataSetRepository) {
    super("reimportAllDatasets");
    this.dataSetRepository = dataSetRepository;
  }

  @Override
  public void execute(Map<String, List<String>> parameters, PrintWriter output) throws Exception {
    for (DataSet dataSet : dataSetRepository.getDataSets()) {
      output.println("Reimport dataset: " + dataSet.getMetadata().getCombinedId());
      output.println("Start with emptying stores");
      output.flush();

      ((BdbQuadStore) dataSet.getQuadStore()).empty();
      dataSet.getGraphStore().empty();
      dataSet.getDefaultResourcesStore().empty();
      ((BdbTypeNameStore) dataSet.getTypeNameStore()).empty();
      ((BdbSchemaStore) dataSet.getSchemaStore()).empty();
      dataSet.getTruePatchStore().empty();
      dataSet.getUpdatedPerPatchStore().empty();
      dataSet.getOldSubjectTypesStore().empty();
      dataSet.getRmlDataSourceStore().empty();

      output.println("Stores emptied; now start importing");
      output.flush();

      Future<ImportStatus> importStatusFuture = dataSet.getImportManager().reprocessLogs();
      while (!importStatusFuture.isDone())
        Thread.sleep(1000);

      output.println("Finished reimport of dataset: " + dataSet.getMetadata().getCombinedId());
      output.flush();
    }
  }
}
