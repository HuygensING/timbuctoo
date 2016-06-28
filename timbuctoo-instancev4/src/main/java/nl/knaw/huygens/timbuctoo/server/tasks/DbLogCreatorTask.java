package nl.knaw.huygens.timbuctoo.server.tasks;

import com.google.common.base.Stopwatch;
import com.google.common.collect.ImmutableMultimap;
import io.dropwizard.servlets.tasks.Task;
import nl.knaw.huygens.timbuctoo.experimental.databaselog.DatabaseFixer;
import nl.knaw.huygens.timbuctoo.experimental.databaselog.DatabaseLog;
import nl.knaw.huygens.timbuctoo.experimental.databaselog.GraphLogValidator;
import nl.knaw.huygens.timbuctoo.server.TinkerpopGraphManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;

public class DbLogCreatorTask extends Task {
  public static final Logger LOG = LoggerFactory.getLogger(DbLogCreatorTask.class);
  private final TinkerpopGraphManager graphManager;
  private final DatabaseLog logGenerator;
  private final DatabaseFixer databaseFixer;
  private final GraphLogValidator graphLogValidator;

  public DbLogCreatorTask(TinkerpopGraphManager graphManager) {
    super("createlog");
    this.graphManager = graphManager;
    logGenerator = new DatabaseLog(graphManager);
    databaseFixer = new DatabaseFixer(graphManager);
    graphLogValidator = new GraphLogValidator(graphManager);
  }

  @Override
  public void execute(ImmutableMultimap<String, String> parameters, PrintWriter output) throws Exception {
    // Add the missing Vertices and Edges, before creating a log.
    Stopwatch fixStopwatch = Stopwatch.createStarted();
    databaseFixer.fix();
    LOG.info("Fixing the database took {}", fixStopwatch.stop());

    Stopwatch generateStopwatch = Stopwatch.createStarted();
    logGenerator.generate();
    LOG.info("Log creation took {}", generateStopwatch.stop());

    Stopwatch validateStopWatch = Stopwatch.createStarted();
    graphLogValidator.writeReport(output);
    LOG.info("Log validation took {}", validateStopWatch.stop());
    output.flush();
  }
}
