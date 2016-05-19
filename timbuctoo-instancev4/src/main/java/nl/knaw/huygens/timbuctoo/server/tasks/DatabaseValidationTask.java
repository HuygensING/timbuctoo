package nl.knaw.huygens.timbuctoo.server.tasks;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.common.collect.ImmutableMultimap;
import io.dropwizard.servlets.tasks.Task;
import nl.knaw.huygens.timbuctoo.server.BackgroundRunner;
import nl.knaw.huygens.timbuctoo.server.healthchecks.ValidationResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;
import java.util.Optional;

public class DatabaseValidationTask extends Task {

  private BackgroundRunner<ValidationResult> validator;
  private static final Logger LOG = LoggerFactory.getLogger(DatabaseValidationTask.class);

  public DatabaseValidationTask(BackgroundRunner<ValidationResult> validator) {
    super("databasevalidation");
    this.validator = validator;
  }


  @Override
  public void execute(ImmutableMultimap<String, String> parameters, PrintWriter output) throws Exception {
    final Optional<ValidationResult> mostRecentResult = validator.getMostRecentResult();
    if (mostRecentResult.isPresent()) {
      writeResult(mostRecentResult, output);
    } else if (parameters.containsKey("force")) {
      validator.start(() -> {
        final Optional<ValidationResult> result = validator.getMostRecentResult();
        writeResult(result, output);
        return result.isPresent() ? result.get() : null;
      });
    } else {
      output.write("No database result");
    }
  }

  private void writeResult(Optional<ValidationResult> result, PrintWriter output) {
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
