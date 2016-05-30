package nl.knaw.huygens.timbuctoo.server.tasks;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMultimap;
import io.dropwizard.servlets.tasks.Task;
import nl.knaw.huygens.timbuctoo.experimental.databaselog.DatabaseLogGenerator;
import nl.knaw.huygens.timbuctoo.server.TinkerpopGraphManager;

import java.io.PrintWriter;

public class DbLogCreatorTask extends Task {
  private final TinkerpopGraphManager graphManager;
  private final ObjectMapper objectMapper;
  private final DatabaseLogGenerator logGenerator;

  public DbLogCreatorTask(TinkerpopGraphManager graphManager) {
    super("createlog");
    this.graphManager = graphManager;
    objectMapper = new ObjectMapper();
    logGenerator = new DatabaseLogGenerator(graphManager);
  }

  @Override
  public void execute(ImmutableMultimap<String, String> parameters, PrintWriter output) throws Exception {
    logGenerator.generate();
  }
}
