package nl.knaw.huygens.timbuctoo.server.tasks;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Stopwatch;
import com.google.common.collect.ImmutableMultimap;
import io.dropwizard.servlets.tasks.Task;
import nl.knaw.huygens.timbuctoo.experimental.databaselog.DatabaseLog;
import nl.knaw.huygens.timbuctoo.server.TinkerpopGraphManager;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;

public class DbLogCreatorTask extends Task {
  private final TinkerpopGraphManager graphManager;
  private final ObjectMapper objectMapper;
  private final DatabaseLog logGenerator;

  public DbLogCreatorTask(TinkerpopGraphManager graphManager) {
    super("createlog");
    this.graphManager = graphManager;
    objectMapper = new ObjectMapper();
    logGenerator = new DatabaseLog(graphManager);
  }

  @Override
  public void execute(ImmutableMultimap<String, String> parameters, PrintWriter output) throws Exception {
    Stopwatch stopwatch = Stopwatch.createStarted();
    logGenerator.generate();
    LoggerFactory.getLogger(DbLogCreatorTask.class).info("Log creation took {}", stopwatch.stop());
  }
}
