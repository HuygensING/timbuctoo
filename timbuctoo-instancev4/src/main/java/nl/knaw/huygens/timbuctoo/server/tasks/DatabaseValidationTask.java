package nl.knaw.huygens.timbuctoo.server.tasks;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.common.collect.ImmutableMultimap;
import io.dropwizard.servlets.tasks.Task;
import nl.knaw.huygens.timbuctoo.server.BackgroundRunner;
import nl.knaw.huygens.timbuctoo.server.TinkerpopGraphManager;
import nl.knaw.huygens.timbuctoo.server.healthchecks.DatabaseValidator;
import nl.knaw.huygens.timbuctoo.server.healthchecks.ValidationResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;
import java.util.Optional;

public class DatabaseValidationTask extends Task {

  private final DatabaseValidator databaseValidator;
  private final TinkerpopGraphManager graphManager;
  private BackgroundRunner<ValidationResult> validationRunner;
  private static final Logger LOG = LoggerFactory.getLogger(DatabaseValidationTask.class);

  public DatabaseValidationTask(BackgroundRunner<ValidationResult> validator, DatabaseValidator databaseValidator,
                                TinkerpopGraphManager graphManager) {
    super("databasevalidation");
    this.validationRunner = validator;
    this.databaseValidator = databaseValidator;
    this.graphManager = graphManager;
  }


  @Override
  public void execute(ImmutableMultimap<String, String> parameters, PrintWriter output) throws Exception {
    final Optional<ValidationResult> mostRecentResult = validationRunner.getMostRecentResult();
    if (parameters.containsKey("force")) {
      ValidationResult result = databaseValidator.check(graphManager.getGraph());
      writeResult(result, output);
    } else if (mostRecentResult.isPresent()) {
      writeResult(mostRecentResult.get(), output);
    } else {
      output.write("No database result");
    }
  }

  private void writeResult(ValidationResult result, PrintWriter output) {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    try {
      String content = objectMapper.writeValueAsString(result);
      output.write(content);
    } catch (JsonProcessingException e) {
      LOG.error("Failed to process ValidationResult as JSON", e);
    }
  }
}
