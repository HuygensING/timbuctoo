package nl.knaw.huygens.timbuctoo.server.tasks;


import com.google.common.collect.ImmutableMultimap;
import io.dropwizard.servlets.tasks.Task;
import nl.knaw.huygens.timbuctoo.v5.dataset.DataSetRepository;
import nl.knaw.huygens.timbuctoo.v5.dataset.dto.DataSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;

public class BackupTask extends Task {
  public static final Logger LOG = LoggerFactory.getLogger(BackupTask.class);
  private DataSetRepository dataSetRepository;

  public BackupTask(DataSetRepository dataSetRepository) {
    super("backup");
    this.dataSetRepository = dataSetRepository;
  }

  @Override
  public void execute(ImmutableMultimap<String, String> immutableMultimap, PrintWriter output) throws Exception {
    for (DataSet dataSet : dataSetRepository.getDataSets()) {
      try {
        dataSet.backupDatabases();
        LOG.info("backup dataset: {}", dataSet.getMetadata().getCombinedId());
        output.println("backup dataset: " + dataSet.getMetadata().getCombinedId());
        output.flush();
      } catch (Throwable t) {
        LOG.error("sync of {} failed", dataSet.getMetadata().getCombinedId());
        output.println("sync of '" + dataSet.getMetadata().getCombinedId() + "' failed");
        output.flush();
        LOG.error("Exception thrown", t);
      }
    }
    output.println("backup complete");
    output.flush();
  }
}
